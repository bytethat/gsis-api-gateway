package gr.bytethat.gsis.gsis39a.abstractions;

import java.time.Instant;
import java.util.List;

public record Representative(
        String id,
        String title,
        List<Range> ranges
) {

    public record Range(
            String id,
            Identity identity,
            Instant start,
            Instant end,

            String email,
            String mobile,
            Boolean otp
    ) {

    }

    public record Identity(
            String type,
            String value) {
    }
}
