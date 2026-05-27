package gr.bytethat.gsis;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import gr.bytethat.gsis.common.abstractions.exception.GsisException;
import gr.bytethat.gsis.controller.GsisErrorResponse;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;

@Component
@Slf4j
public class RateLimitingFilter extends OncePerRequestFilter {
    private final ObjectMapper objectMapper = JsonMapper.builder()
            .addModule(new JavaTimeModule())
            .build()
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    private final Cache<String, Bucket> cache = Caffeine.newBuilder()
            .expireAfterAccess(Duration.ofMinutes(5))
            .maximumSize(100_000)
            .build();

    @Override
    protected void doFilterInternal(HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain chain) throws ServletException, IOException {
        var ip = request.getRemoteAddr();

        var bucket = cache.get(ip, _ -> createBucket());

        if (!bucket.tryConsume(1)) {
            log.warn("Too many requests from IP: {}", ip);

            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write(objectMapper.writeValueAsString(new GsisErrorResponse(
                    HttpStatus.TOO_MANY_REQUESTS.value(),
                    "Internal Server Error",
                    "Too Many Requests",
                    GsisException.ErrorCodes.TOO_MANY_REQUESTS
            )));
            response.getWriter().flush();

            return;
        }

        chain.doFilter(request, response);
    }

    private Bucket createBucket() {
        return Bucket.builder()
                .addLimit(Bandwidth.builder()
                        .capacity(30)
                        .refillGreedy(30, Duration.ofMinutes(1))
                        .build())
                .build();
    }
}
