package gr.bytethat.gsis.controller;

import gr.bytethat.gsis.common.abstractions.exception.GsisException;
import gr.bytethat.gsis.common.abstractions.exception.GsisRemoteException;
import gr.bytethat.gsis.gsis39a.abstractions.Buyer;
import gr.bytethat.gsis.gsis39a.abstractions.Gsis39a;
import gr.bytethat.gsis.gsis39a.abstractions.Otp;
import gr.bytethat.gsis.gsis39a.abstractions.Representative;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/gsis/39a")
@RequiredArgsConstructor
@Slf4j
public class Gsis39aController {
    private final Gsis39a gsis39a;

    @GetMapping("/buyer/{vat}")
    public ResponseEntity<Buyer> getBuyer(@PathVariable String vat) {
        return ResponseEntity.ok(gsis39a.getBuyer(vat));
    }

    @PostMapping("/buyer/{vat}")
    public ResponseEntity<Void> setBuyer(@PathVariable String vat, @RequestBody BuyerRequest request) {
        gsis39a.setBuyer(vat, request.email, request.mobile);

        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/buyer/{vat}")
    public ResponseEntity<Void> deleteBuyer(@PathVariable String vat) {
        gsis39a.deleteBuyer(vat);

        return ResponseEntity.ok().build();
    }

    @GetMapping("/buyer/{vat}/representatives")
    public ResponseEntity<List<Representative>> getRepresentatives(@PathVariable String vat) {
        return ResponseEntity.ok(gsis39a.getBuyerRepresentatives(vat));
    }

    @GetMapping("/buyer/{vat}/representatives/{representativeId}")
    public ResponseEntity<Representative> getRepresentative(@PathVariable String vat, @PathVariable String representativeId) {
        return gsis39a.getBuyerRepresentative(vat, representativeId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/buyer/{vat}/representatives/{representativeId}")
    public ResponseEntity<Void> createRepresentativeRange(@PathVariable String vat, @PathVariable String representativeId, @RequestBody Representative.Range range) {
        gsis39a.createRange(vat, representativeId, range);

        return ResponseEntity.ok().build();
    }

    @PutMapping("/buyer/{vat}/representatives/{representativeId}")
    public ResponseEntity<Void> updateRepresentativeRange(@PathVariable String vat, @PathVariable String representativeId, @RequestBody Representative.Range range) {
        gsis39a.updateRange(vat, representativeId, range);

        return ResponseEntity.ok().build();
    }

    @PutMapping("/buyer/{vat}/representatives/{representativeId}/range/{rangeId}")
    public ResponseEntity<Void> deleteRepresentativeRange(@PathVariable String vat, @PathVariable String representativeId, @PathVariable String rangeId) {
        gsis39a.deleteRange(vat, representativeId, rangeId);

        return ResponseEntity.ok().build();
    }

    @GetMapping("/buyer/{vat}/representatives/{representativeId}/otp")
    public ResponseEntity<Otp> getOtp(
            @PathVariable String vat,
            @PathVariable String representativeId) {
        return ResponseEntity.ok(gsis39a.getOtp(vat, representativeId));
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
        if (e.getCode().equalsIgnoreCase(GsisException.ErrorCodes.NOT_FOUND)) {
            return ResponseEntity
                    .notFound()
                    .build();
        }

        if (e.getCode().equalsIgnoreCase(GsisException.ErrorCodes.OVERLAPPING_RANGE)) {
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body(new GsisErrorResponse(
                            HttpStatus.CONFLICT.value(),
                            "Conflict",
                            e.getMessage(),
                            e.getCode()
                    ));
        }

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

    public record BuyerRequest(
            String email,
            String mobile
    ) {

    }
}
