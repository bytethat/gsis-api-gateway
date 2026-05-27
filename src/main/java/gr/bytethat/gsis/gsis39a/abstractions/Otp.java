package gr.bytethat.gsis.gsis39a.abstractions;

import java.time.Instant;

public record Otp(
        String id,
        String usageFlag,
        Instant validStart,
        Instant validEnd
) {
}
