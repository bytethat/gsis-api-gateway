package gr.bytethat.gsis.controller;

import gr.bytethat.gsis.registry.abstractions.BusinessDetails;
import gr.bytethat.gsis.registry.abstractions.GsisRegistry;
import gr.bytethat.gsis.common.abstractions.exception.GsisException;
import gr.bytethat.gsis.common.abstractions.exception.GsisRemoteException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/gsis/registry")
@RequiredArgsConstructor
@Slf4j
public class GsisRegistryController {
    private final GsisRegistry gsisRegistry;

    @GetMapping("/lookup/{vat}")
    public ResponseEntity<BusinessDetails> registryLookup(@PathVariable String vat) {
        return ResponseEntity.ok(gsisRegistry.lookup(vat));
    }

    @ExceptionHandler(GsisRemoteException.class)
    public ResponseEntity<GsisErrorResponse> handleBusinessException(GsisRemoteException e) {
        return ResponseEntity
                .status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(new GsisErrorResponse(
                        HttpStatus.UNPROCESSABLE_ENTITY.value(),
                        "GSIS Remote Error",
                        e.getDescription(),
                        e.getCode()
                ));
    }

    @ExceptionHandler(GsisException.class)
    public ResponseEntity<GsisErrorResponse> handleSoapException(GsisException e) {
        if (e.getCode().equalsIgnoreCase(GsisException.ErrorCodes.INVALID_VAT_FORMAT)) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new GsisErrorResponse(
                            HttpStatus.BAD_REQUEST.value(),
                            "Bad Request",
                            e.getMessage(),
                            e.getCode()
                    ));
        }

        if (e.getCode().equalsIgnoreCase(GsisException.ErrorCodes.GSIS_COMMUNICATION_ERROR)) {
            return ResponseEntity
                    .status(HttpStatus.BAD_GATEWAY)
                    .body(new GsisErrorResponse(
                            HttpStatus.BAD_GATEWAY.value(),
                            "Bad Gateway",
                            e.getMessage(),
                            e.getCode()
                    ));
        }

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new GsisErrorResponse(
                        HttpStatus.INTERNAL_SERVER_ERROR.value(),
                        "Internal Server Error",
                        e.getMessage(),
                        e.getCode()
                ));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<GsisErrorResponse> handleGenericException(Exception e) {
        log.error("Unhandled exception occurred in GSIS Controller", e);

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new GsisErrorResponse(
                        HttpStatus.INTERNAL_SERVER_ERROR.value(),
                        "Internal Server Error",
                        e.getMessage(),
                        GsisException.ErrorCodes.GSIS_GENERIC_ERROR
                ));
    }
}
