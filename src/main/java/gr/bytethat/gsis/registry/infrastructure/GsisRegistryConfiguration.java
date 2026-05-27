package gr.bytethat.gsis.registry.infrastructure;

import gr.bytethat.gsis.registry.abstractions.GsisRegistry;
import gr.bytethat.gsis.registry.core.GsisRegistryOptions;
import gr.bytethat.gsis.registry.core.GsisRegistryImpl;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.web.context.WebApplicationContext;

@Configuration
@RequiredArgsConstructor
public class GsisRegistryConfiguration {

    private final HttpServletRequest request;

    @Bean
    @Scope(value = WebApplicationContext.SCOPE_REQUEST, proxyMode = ScopedProxyMode.INTERFACES)
    @ConditionalOnMissingBean(GsisRegistry.class)
    public GsisRegistry gsisService() {
        String username = request.getHeader("X-GSIS-Username");
        String password = request.getHeader("X-GSIS-Password");
        String calledBy = request.getHeader("X-GSIS-CalledBy");

        if (username == null || username.trim().isEmpty() || password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("Missing required GSIS credentials in headers (X-GSIS-Username, X-GSIS-Password)");
        }

        return new GsisRegistryImpl(new GsisRegistryOptions(
                username,
                password,
                calledBy
        ));
    }
}
