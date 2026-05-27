package gr.bytethat.gsis.controller;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class GsisErrorResponse {
    private final LocalDateTime timestamp;
    private final int status;
    private final String error;
    private final String message;
    private final String code;

    public GsisErrorResponse(int status, String error, String message, String code) {
        this.timestamp = LocalDateTime.now();
        this.status = status;
        this.error = error;
        this.message = message;
        this.code = code;
    }
}
