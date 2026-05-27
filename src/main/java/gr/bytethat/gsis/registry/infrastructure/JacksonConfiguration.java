package gr.bytethat.gsis.registry.infrastructure;

import tools.jackson.core.JsonGenerator;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ValueSerializer;
import tools.jackson.databind.JacksonModule;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.module.SimpleModule;
import gr.bytethat.gsis.registry.abstractions.BusinessDetails;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration("registryJacksonConfiguration")
public class JacksonConfiguration {

    @Bean
    public JacksonModule gsisRegistryJacksonModule() {
        SimpleModule module = new SimpleModule("GsisRegistryJacksonModule");
        module.addSerializer(BusinessDetails.Type.class, new GsisTypeSerializer());
        module.addSerializer(BusinessDetails.Status.class, new GsisStatusSerializer());
        module.addSerializer(BusinessDetails.Activity.Type.class, new GsisActivityTypeSerializer());
        return module;
    }

    private static class GsisTypeSerializer extends ValueSerializer<BusinessDetails.Type> {
        @Override
        public void serialize(BusinessDetails.Type value, JsonGenerator gen, SerializationContext serializers) throws JacksonException {
            if (value != null) {
                gen.writeString(value.getValue());
            } else {
                gen.writeNull();
            }
        }
    }

    private static class GsisStatusSerializer extends ValueSerializer<BusinessDetails.Status> {
        @Override
        public void serialize(BusinessDetails.Status value, JsonGenerator gen, SerializationContext serializers) throws JacksonException {
            if (value != null) {
                gen.writeString(value.getValue());
            } else {
                gen.writeNull();
            }
        }
    }

    private static class GsisActivityTypeSerializer extends ValueSerializer<BusinessDetails.Activity.Type> {
        @Override
        public void serialize(BusinessDetails.Activity.Type value, JsonGenerator gen, SerializationContext serializers) throws JacksonException {
            if (value != null) {
                gen.writeString(value.getValue());
            } else {
                gen.writeNull();
            }
        }
    }
}
