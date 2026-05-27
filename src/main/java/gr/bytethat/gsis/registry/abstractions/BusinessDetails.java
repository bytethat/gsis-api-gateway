package gr.bytethat.gsis.registry.abstractions;

import lombok.Getter;

import java.time.Instant;
import java.util.List;

public record BusinessDetails(
        String id,
        String taxAgency,
        Boolean active,
        Type type,
        Status status,
        Boolean NormalVat,
        String title,
        String commercialTitle,
        Instant registrationDate,
        Instant stopDate,
        Address address,
        List<Activity> Activities
) {
    @Getter
    public enum Type {
        LEGAL_ENTITY("Entity"),
        NATURAL_PERSON("Person");

        private final String value;

        Type(String value) {
            this.value = value;
        }

    }

    @Getter
    public enum Status {
        BUSINESS("Business"),
        INDIVIDUAL("Individual"),
        INACTIVE("Dissolved/Inactive");

        private final String value;

        Status(String value) {
            this.value = value;
        }

    }

    public record Address(
            String street,
            String number,
            String area,
            String postalCode
    ) {
    }

    public record Activity(
            String code,
            String description,
            Type type
    ) {

        @Getter
        public enum Type {
            PRIMARY("Primary"),
            SECONDARY("Secondary"),
            OTHER("Other"),
            ANCILLARY("Ancillary");

            private final String value;

            Type(String value) {
                this.value = value;
            }

        }
    }
}
