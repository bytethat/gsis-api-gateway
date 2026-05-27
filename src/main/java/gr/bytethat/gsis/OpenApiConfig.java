package gr.bytethat.gsis;

import io.swagger.v3.oas.models.parameters.Parameter;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
    @Bean
    public OperationCustomizer addGlobalHeaders() {
        return (operation, handlerMethod) -> {

            if (handlerMethod.getMethod().getDeclaringClass().getName().contains("gr.bytethat.gsis")) {

                operation.addParametersItem(new Parameter()
                        .in("header")
                        .name("X-GSIS-Username")
                        .description("The GSIS/AADE API username credential")
                        .required(true)
                        .schema(new io.swagger.v3.oas.models.media.StringSchema()));

                operation.addParametersItem(new Parameter()
                        .in("header")
                        .name("X-GSIS-Password")
                        .description("The GSIS/AADE API password credential")
                        .required(true)
                        .schema(new io.swagger.v3.oas.models.media.StringSchema()));

                operation.addParametersItem(new Parameter()
                        .in("header")
                        .name("X-GSIS-CalledBy")
                        .description("The VAT of the entity invoking the proxy")
                        .required(false)
                        .schema(new io.swagger.v3.oas.models.media.StringSchema()));
            }

            return operation;
        };
    }
}
