package gr.bytethat.gsis.common.abstractions.exception;

import lombok.Getter;

@Getter
public class GsisException extends RuntimeException {
    private final String code;

    public GsisException(String message) {
        this(ErrorCodes.GSIS_GENERIC_ERROR, message);
    }

    public GsisException(String message, Throwable cause) {
        this(ErrorCodes.GSIS_GENERIC_ERROR, message, cause);
    }

    public GsisException(String code, String message) {
        super(message);

        this.code = code;
    }

    public GsisException(String code, String message, Throwable cause) {
        super(message, cause);

        this.code = code;
    }

    public static class ErrorCodes {
        public static String GSIS_GENERIC_ERROR = "GSIS_GENERIC_ERROR";
        public static String GSIS_COMMUNICATION_ERROR = "GSIS_COMMUNICATION_ERROR";
        public static String INVALID_VAT_FORMAT = "INVALID_VAT_FORMAT";
        public static String NOT_FOUND = "NOT_FOUND";
        public static String OVERLAPPING_RANGE = "OVERLAPPING_RANGE";
    }
}
