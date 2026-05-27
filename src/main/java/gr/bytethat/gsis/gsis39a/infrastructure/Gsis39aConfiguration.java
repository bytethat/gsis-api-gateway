package gr.bytethat.gsis.gsis39a.infrastructure;

import gr.bytethat.gsis.gsis39a.abstractions.Gsis39a;
import gr.bytethat.gsis.gsis39a.core.Gsis39aImpl;
import gr.bytethat.gsis.gsis39a.core.Gsis39aOptions;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.web.context.WebApplicationContext;

@Configuration
@RequiredArgsConstructor
public class Gsis39aConfiguration {

    private final HttpServletRequest request;

    @Bean
    @Scope(value = WebApplicationContext.SCOPE_REQUEST, proxyMode = ScopedProxyMode.INTERFACES)
    @ConditionalOnMissingBean(Gsis39a.class)
    public Gsis39a gsis39a() {
        String username = request.getHeader("X-GSIS-Username");
        String password = request.getHeader("X-GSIS-Password");
        String calledBy = request.getHeader("X-GSIS-CalledBy");

        if (username == null || username.trim().isEmpty() || password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("Missing required GSIS credentials in headers (X-GSIS-Username, X-GSIS-Password)");
        }

        return new Gsis39aImpl(new Gsis39aOptions(
                username,
                password,
                calledBy
        ));
    }
}
