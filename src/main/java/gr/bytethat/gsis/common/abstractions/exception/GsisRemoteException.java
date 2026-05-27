package gr.bytethat.gsis.common.abstractions.exception;

import lombok.Getter;

@Getter
public class GsisRemoteException extends RuntimeException {
    private final String code;
    private final String description;

    public GsisRemoteException(String code, String description) {
        super(String.format("GSIS Remote Exception [%s]: %s", code, description));

        this.code = code;
        this.description = description;
    }
}
