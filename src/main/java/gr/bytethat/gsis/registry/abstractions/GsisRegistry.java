package gr.bytethat.gsis.registry.abstractions;

public interface GsisRegistry {
    BusinessDetails lookup(String vat);
}
