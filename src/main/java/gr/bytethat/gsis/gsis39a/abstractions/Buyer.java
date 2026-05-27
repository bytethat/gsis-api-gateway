package gr.bytethat.gsis.gsis39a.abstractions;

import lombok.Getter;

public record Buyer(
        String id,
        String title,
        String email,
        String mobile,
        Type type
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

}
