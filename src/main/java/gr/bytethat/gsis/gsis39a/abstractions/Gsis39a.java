package gr.bytethat.gsis.gsis39a.abstractions;

import java.util.List;
import java.util.Optional;

public interface Gsis39a {
    Buyer getBuyer(String vat);
    void setBuyer(String vat, String email, String mobile);
    void deleteBuyer(String vat);

    public List<IdentityType> getIdentityTypes();

    List<Representative> getBuyerRepresentatives(String vat);
    Optional<Representative> getBuyerRepresentative(String vat, String representativeVat);

    void createRange(String vat, String representativeVat, Representative.Range range);
    void updateRange(String vat, String representativeVat, Representative.Range range);
    void deleteRange(String vat, String representativeVat, String rangeId);

    Otp getOtp(String vat, String representativeId);
}

