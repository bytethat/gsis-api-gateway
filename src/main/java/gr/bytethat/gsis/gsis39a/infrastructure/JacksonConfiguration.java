package gr.bytethat.gsis.gsis39a.infrastructure;

import gr.bytethat.gsis.gsis39a.abstractions.Buyer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.JacksonModule;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ValueSerializer;
import tools.jackson.databind.module.SimpleModule;

@Configuration("gsis39aJacksonConfiguration")
public class JacksonConfiguration {

    @Bean
    public JacksonModule gsis39aJacksonModule() {
        SimpleModule module = new SimpleModule("Gsis39aJacksonModule");
        module.addSerializer(Buyer.Type.class, new TypeSerializer());
        return module;
    }

    private static class TypeSerializer extends ValueSerializer<Buyer.Type> {
        @Override
        public void serialize(Buyer.Type value, JsonGenerator gen, SerializationContext serializers) throws JacksonException {
            if (value != null) {
                gen.writeString(value.getValue());
            } else {
                gen.writeNull();
            }
        }
    }
}
