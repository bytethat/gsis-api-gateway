package gr.bytethat.gsis.gsis39a.core;

import gr.bytethat.gsis.common.abstractions.exception.GsisException;
import gr.bytethat.gsis.common.abstractions.exception.GsisRemoteException;
import gr.bytethat.gsis.common.core.GreekVatValidator;
import gr.bytethat.gsis.gsis39a.abstractions.Buyer;
import gr.bytethat.gsis.gsis39a.abstractions.Gsis39a;
import gr.bytethat.gsis.gsis39a.abstractions.Otp;
import gr.bytethat.gsis.gsis39a.abstractions.Representative;
import gr.bytethat.gsis.gsis39a.core.client.*;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
public class Gsis39aImpl implements Gsis39a {
    private final Gsis39aOptions options;
    private final VtWs39AFPAService service;

    public Gsis39aImpl(Gsis39aOptions options) {
        this.options = options;

        this.service = new VtWs39AFPAService();

        this.service.setHandlerResolver(_ -> List.of(new GsisSecurityHandler(options.username(), options.password())));
    }

    public Gsis39aImpl(Gsis39aOptions options, VtWs39AFPAService service) {
        this.options = options;
        this.service = service;
    }

    @Override
    public Buyer getBuyer(String vat) {
        if (!GreekVatValidator.isValid(vat)) {
            throw new GsisException(GsisException.ErrorCodes.INVALID_VAT_FORMAT, "Invalid VAT format. Must be a 9-digit numeric string with a valid check digit.");
        }

        try {
            // Construct input record using generated ObjectFactory
            var factory = new ObjectFactory();
            var input = factory.createVtWs39AfpaBu3InRtType();

            input.setBuyerAfm(factory.createVtWs39AfpaBu3InRtTypeBuyerAfm(vat));

            VtWs39AfpaBu3ResultRtType response;
            try {
                var result = service.getVtWs39AFPAServicePort().vt39AfpaBu3GetBuyer(input);

                response = (result != null) ? result.getVtWs39AfpaBu3ResultRtType() : null;

                if (response == null) {
                    throw new NullPointerException("GSIS returned null response");
                }
            } catch (Exception e) {
                log.error("GSIS lookup failed", e);

                throw new GsisException(GsisException.ErrorCodes.GSIS_COMMUNICATION_ERROR, e.getMessage(), e);
            }

            var error = response.getMessageRec();
            if (error != null) {
                var code = error.getMessageCode() != null ? error.getMessageCode().getValue() : null;
                var description = error.getMessageDescr() != null ? error.getMessageDescr().getValue() : null;

                if (code != null && !code.trim().isEmpty() && !"OK".equalsIgnoreCase(code.trim())) {
                    throw new GsisRemoteException(code, description);
                }
            }

            return BuyerMapper.map(response);

        } catch (GsisRemoteException | GsisException e) {
            throw e;
        } catch (Exception e) {
            throw new GsisException(e.getMessage(), e);
        }
    }

    @Override
    public void setBuyer(String vat, String email, String mobile) {
        if (!GreekVatValidator.isValid(vat)) {
            throw new GsisException(GsisException.ErrorCodes.INVALID_VAT_FORMAT, "Invalid VAT format. Must be a 9-digit numeric string with a valid check digit.");
        }

        try {
            // Construct input record using generated ObjectFactory
            var factory = new ObjectFactory();
            var input = factory.createVtWs39AfpaBu1InRtType();

            input.setBuyerAfm(factory.createVtWs39AfpaBu3InRtTypeBuyerAfm(vat));
            input.setBuyerEmail(factory.createVtWs39AfpaBu1InRtTypeBuyerEmail(email));
            input.setBuyerMobile(factory.createVtWs39AfpaBu1InRtTypeBuyerMobile(mobile));

            VtWs39AfpaBu1ResultRtType response;
            try {
                var result = service.getVtWs39AFPAServicePort().vt39AfpaBu1SetBuyer(input);

                response = (result != null) ? result.getVtWs39AfpaBu1ResultRtType() : null;

                if (response == null) {
                    throw new NullPointerException("GSIS returned null response");
                }
            } catch (Exception e) {
                log.error("GSIS lookup failed", e);

                throw new GsisException(GsisException.ErrorCodes.GSIS_COMMUNICATION_ERROR, e.getMessage(), e);
            }

            var error = response.getMessageRec();
            if (error != null) {
                var code = error.getMessageCode() != null ? error.getMessageCode().getValue() : null;
                var description = error.getMessageDescr() != null ? error.getMessageDescr().getValue() : null;

                if (code != null && !code.trim().isEmpty() && !"OK".equalsIgnoreCase(code.trim())) {
                    throw new GsisRemoteException(code, description);
                }
            }
        } catch (GsisRemoteException | GsisException e) {
            throw e;
        } catch (Exception e) {
            throw new GsisException(e.getMessage(), e);
        }
    }

    @Override
    public void deleteBuyer(String vat) {
        if (!GreekVatValidator.isValid(vat)) {
            throw new GsisException(GsisException.ErrorCodes.INVALID_VAT_FORMAT, "Invalid VAT format. Must be a 9-digit numeric string with a valid check digit.");
        }

        try {
            // Construct input record using generated ObjectFactory
            var factory = new ObjectFactory();
            var input = factory.createVtWs39AfpaBu2InRtType();

            input.setBuyerAfm(factory.createVtWs39AfpaBu2InRtTypeBuyerAfm(vat));

            VtWs39AfpaBu2ResultRtType response;
            try {
                // Invoke SOAP Service method vt39AfpaBu2DelBuyer
                var result = service.getVtWs39AFPAServicePort().vt39AfpaBu2DelBuyer(input);

                response = (result != null) ? result.getVtWs39AfpaBu2ResultRtType() : null;

                if (response == null) {
                    throw new NullPointerException("GSIS returned null response");
                }
            } catch (Exception e) {
                log.error("GSIS lookup failed", e);

                throw new GsisException(GsisException.ErrorCodes.GSIS_COMMUNICATION_ERROR, e.getMessage(), e);
            }

            var error = response.getMessageRec();
            if (error != null) {
                var code = error.getMessageCode() != null ? error.getMessageCode().getValue() : null;
                var description = error.getMessageDescr() != null ? error.getMessageDescr().getValue() : null;

                if (code != null && !code.trim().isEmpty() && !"OK".equalsIgnoreCase(code.trim())) {
                    throw new GsisRemoteException(code, description);
                }
            }
        } catch (GsisRemoteException | GsisException e) {
            throw e;
        } catch (Exception e) {
            throw new GsisException(e.getMessage(), e);
        }
    }

    @Override
    public Optional<Representative> getBuyerRepresentative(String vat, String representativeVat) {
        return this.getBuyerRepresentatives(vat)
                .stream()
                .filter(x -> x.id().equalsIgnoreCase(representativeVat))
                .findFirst();
    }

    @Override
    public List<Representative> getBuyerRepresentatives(String vat) {
        if (!GreekVatValidator.isValid(vat)) {
            throw new GsisException(GsisException.ErrorCodes.INVALID_VAT_FORMAT, "Invalid VAT format. Must be a 9-digit numeric string with a valid check digit.");
        }

        var pageSize = BigDecimal.valueOf(100);
        var fromIndex = BigDecimal.ONE;
        var results = new ArrayList<RepresentativeResult>();

        try {
            // Construct input record using generated ObjectFactory
            var factory = new ObjectFactory();

            while (true) {
                var input = factory.createVtWs39AfpaBu7InRtType();

                input.setBuyerReprRole(factory.createVtWs39AfpaBu7InRtTypeBuyerReprRole("B"));
                input.setBuyerAfm(factory.createVtWs39AfpaBu7InRtTypeBuyerAfm(vat));
                input.setFetchSizeRequested(factory.createVtWs39AfpaBu7InRtTypeFetchSizeRequested(pageSize));
                input.setFetchAaFromRequested(factory.createVtWs39AfpaBu7InRtTypeFetchAaFromRequested(fromIndex));

                VtWs39AfpaBu7ResultRtType response;
                try {
                    // Invoke SOAP Service method
                    var result = service.getVtWs39AFPAServicePort().vt39AfpaBu7GetRepr(input);

                    response = (result != null) ? result.getVtWs39AfpaBu7ResultRtType() : null;

                    if (response == null) {
                        throw new NullPointerException("GSIS returned null response");
                    }
                } catch (Exception e) {
                    log.error("GSIS lookup failed", e);

                    throw new GsisException(GsisException.ErrorCodes.GSIS_COMMUNICATION_ERROR, e.getMessage(), e);
                }

                var error = response.getMessageRec();
                if (error != null) {
                    var code = error.getMessageCode() != null ? error.getMessageCode().getValue() : null;
                    var description = error.getMessageDescr() != null ? error.getMessageDescr().getValue() : null;

                    if (code != null && !code.trim().isEmpty() && !"OK".equalsIgnoreCase(code.trim())) {
                        throw new GsisRemoteException(code, description);
                    }
                }

                if (response.getBu7OutTab() == null) {
                    break;
                }

                response.getBu7OutTab()
                        .getItem()
                        .stream()
                        .map(Gsis39aImpl::map)
                        .forEach(results::add);

                var count = response.getBu7RowsoutRec().getResultsNo().getValue();

                if (count.compareTo(pageSize) < 0) {
                    break;
                }

                fromIndex = fromIndex.add(pageSize);
            }

            return results
                    .stream()
                    .collect(Collectors.groupingBy(RepresentativeResult::representativeId))
                    .entrySet()
                    .stream()
                    .map(Gsis39aImpl::map)
                    .toList();

        } catch (GsisRemoteException | GsisException e) {
            throw e;
        } catch (Exception e) {
            throw new GsisException(e.getMessage(), e);
        }
    }

    @Override
    public void createRange(String vat, String representativeVat, Representative.Range range) {
        if (!GreekVatValidator.isValid(vat)) {
            throw new GsisException(GsisException.ErrorCodes.INVALID_VAT_FORMAT, "Invalid VAT format. Must be a 9-digit numeric string with a valid check digit.");
        }

        if (!GreekVatValidator.isValid(representativeVat)) {
            throw new GsisException(GsisException.ErrorCodes.INVALID_VAT_FORMAT, "Invalid VAT format. Must be a 9-digit numeric string with a valid check digit.");
        }

        var representative = this.getBuyerRepresentative(vat, representativeVat)
                .orElse(new Representative(
                        representativeVat,
                        "",
                        List.of()));

        if (hasOverlap(representative.ranges(), range)) {
            throw new GsisException(GsisException.ErrorCodes.OVERLAPPING_RANGE, "The requested date range overlaps with an existing authorized period for this representative.");
        }

        this.CreateOrUpdateRepresentativeRange(vat, representative, range);
    }

    @Override
    public void updateRange(String vat, String representativeVat, Representative.Range range) {
        if (!GreekVatValidator.isValid(vat)) {
            throw new GsisException(GsisException.ErrorCodes.INVALID_VAT_FORMAT, "Invalid VAT format. Must be a 9-digit numeric string with a valid check digit.");
        }

        if (!GreekVatValidator.isValid(representativeVat)) {
            throw new GsisException(GsisException.ErrorCodes.INVALID_VAT_FORMAT, "Invalid VAT format. Must be a 9-digit numeric string with a valid check digit.");
        }

        var representative = this.getBuyerRepresentative(vat, representativeVat)
                .orElseThrow(() -> new GsisException(GsisException.ErrorCodes.NOT_FOUND, "Representative with VAT %s not found for buyer %s".formatted(representativeVat, vat)));

        var existingRange = representative.ranges()
                .stream()
                .filter(x -> x.id().equalsIgnoreCase(range.id()))
                .findFirst()
                .orElseThrow(() -> new GsisException(GsisException.ErrorCodes.NOT_FOUND, "Range with id %s not found for representative %s of buyer %s".formatted(range.id(), representativeVat, vat)));

        if (hasOverlap(representative.ranges().stream().filter(x -> !x.id().equalsIgnoreCase(range.id())).toList(), range)) {
            throw new GsisException(GsisException.ErrorCodes.OVERLAPPING_RANGE, "The requested date range overlaps with an existing authorized period for this representative.");
        }

        this.CreateOrUpdateRepresentativeRange(vat, representative, range);
    }

    @Override
    public void deleteRange(String vat, String representativeVat, String rangeId) {
        if (!GreekVatValidator.isValid(vat)) {
            throw new GsisException(GsisException.ErrorCodes.INVALID_VAT_FORMAT, "Invalid VAT format. Must be a 9-digit numeric string with a valid check digit.");
        }

        if (!GreekVatValidator.isValid(representativeVat)) {
            throw new GsisException(GsisException.ErrorCodes.INVALID_VAT_FORMAT, "Invalid VAT format. Must be a 9-digit numeric string with a valid check digit.");
        }

        var representative = this.getBuyerRepresentative(vat, representativeVat)
                .orElseThrow(() -> new GsisException(GsisException.ErrorCodes.NOT_FOUND, "Representative with VAT %s not found for buyer %s".formatted(representativeVat, vat)));

        var range = representative.ranges()
                .stream()
                .filter(x -> x.id().equalsIgnoreCase(rangeId))
                .findFirst()
                .orElseThrow(() -> new GsisException(GsisException.ErrorCodes.NOT_FOUND, "Range with id %s not found for representative %s of buyer %s".formatted(rangeId, representativeVat, vat)));

        try {
            // Construct input record using generated ObjectFactory
            var factory = new ObjectFactory();
            var input = factory.createVtWs39AfpaBu6InRtType();

            input.setBuyerAfm(factory.createVtWs39AfpaBu3InRtTypeBuyerAfm(vat));
            input.setReprInsertCallId(factory.createVtWs39AfpaBu6InRtTypeReprInsertCallId(new BigDecimal(range.id())));
            input.setReprAfm(factory.createVtWs39AfpaBu5InRtTypeReprAfm(representative.id()));
            input.setReprStartDatetime(factory.createVtWs39AfpaBu5InRtTypeReprStartDatetime(range.start().toString()));
            if (range.end() != null) {
                input.setReprEndDatetime(factory.createVtWs39AfpaBu5InRtTypeReprEndDatetime(range.end().toString()));
            }
            input.setOtpRequired(factory.createVtWs39AfpaBu5InRtTypeOtpRequired(range.otp() ? "Y" : "N"));

            VtWs39AfpaBu6ResultRtType response;
            try {
                var result = service.getVtWs39AFPAServicePort().vt39AfpaBu6DelRepr(input);

                response = (result != null) ? result.getVtWs39AfpaBu6ResultRtType() : null;

                if (response == null) {
                    throw new NullPointerException("GSIS returned null response");
                }
            } catch (Exception e) {
                log.error("GSIS lookup failed", e);

                throw new GsisException(GsisException.ErrorCodes.GSIS_COMMUNICATION_ERROR, e.getMessage(), e);
            }

            var error = response.getMessageRec();
            if (error != null) {
                var code = error.getMessageCode() != null ? error.getMessageCode().getValue() : null;
                var description = error.getMessageDescr() != null ? error.getMessageDescr().getValue() : null;

                if (code != null && !code.trim().isEmpty() && !"OK".equalsIgnoreCase(code.trim())) {
                    throw new GsisRemoteException(code, description);
                }
            }
        } catch (GsisRemoteException | GsisException e) {
            throw e;
        } catch (Exception e) {
            throw new GsisException(e.getMessage(), e);
        }
    }

    @Override
    public Otp getOtp(String vat, String representativeId) {
        if (!GreekVatValidator.isValid(vat)) {
            throw new GsisException(GsisException.ErrorCodes.INVALID_VAT_FORMAT, "Invalid buyer VAT format. Must be a 9-digit numeric string with a valid check digit.");
        }

        if (representativeId != null && !representativeId.trim().isEmpty() && !GreekVatValidator.isValid(representativeId)) {
            throw new GsisException(GsisException.ErrorCodes.INVALID_VAT_FORMAT, "Invalid representative VAT format. Must be a 9-digit numeric string with a valid check digit.");
        }

        try {
            var factory = new ObjectFactory();
            var input = factory.createVtWs39AfpaBu9InRtType();

            input.setBuyerAfm(factory.createVtWs39AfpaBu9InRtTypeBuyerAfm(vat));
            if (representativeId != null && !representativeId.trim().isEmpty()) {
                input.setReprAfm(factory.createVtWs39AfpaBu9InRtTypeReprAfm(representativeId));
            }
            input.setOtpActionRequested(factory.createVtWs39AfpaBu9InRtTypeOtpActionRequested("F"));

            VtWs39AfpaBu9ResultRtType response;
            try {
                var result = service.getVtWs39AFPAServicePort().vt39AfpaBu9GetOtp(input);

                response = (result != null) ? result.getVtWs39AfpaBu9ResultRtType() : null;

                if (response == null) {
                    throw new NullPointerException("GSIS returned null response");
                }
            } catch (Exception e) {
                log.error("GSIS OTP retrieval failed", e);
                throw new GsisException(GsisException.ErrorCodes.GSIS_COMMUNICATION_ERROR, e.getMessage(), e);
            }

            var error = response.getMessageRec();
            if (error != null) {
                var code = error.getMessageCode() != null ? error.getMessageCode().getValue() : null;
                var description = error.getMessageDescr() != null ? error.getMessageDescr().getValue() : null;

                if (code != null
                        && !code.trim().isEmpty()
                        && !"OK".equalsIgnoreCase(code.trim())
                        && !"39AFPA_OTP_NEW_NOTFOUND".equalsIgnoreCase(code.trim())) {// Ειδική περίπτωση που σημαίνει ότι δεν υπάρχει ενεργό OTP αλλά δεν είναι σφάλμα, απλά πρέπει να εκδώσουμε νέο
                    throw new GsisRemoteException(code, description);
                }
            }

            var now = java.time.Instant.now();

            return OtpMapper.map(response).stream()
                    .filter(otp -> {
                        if (otp.validStart() == null || now.isBefore(otp.validStart())) {
                            return false;
                        }
                        if (otp.validEnd() == null || now.isAfter(otp.validEnd())) {
                            return false;
                        }

                        return true;
                    })
                    .findFirst()
                    .orElse(issueNewOTP(vat, representativeId));
        } catch (GsisRemoteException | GsisException e) {
            throw e;
        } catch (Exception e) {
            throw new GsisException(e.getMessage(), e);
        }
    }

    private Otp issueNewOTP(String vat, String representativeId) {
        try {
            var factory = new ObjectFactory();
            var input = factory.createVtWs39AfpaBu9InRtType();

            input.setBuyerAfm(factory.createVtWs39AfpaBu9InRtTypeBuyerAfm(vat));
            input.setReprAfm(factory.createVtWs39AfpaBu9InRtTypeReprAfm(representativeId));
            input.setOtpActionRequested(factory.createVtWs39AfpaBu9InRtTypeOtpActionRequested("C"));
            input.setOtpSizeRequested(factory.createVtWs39AfpaBu9InRtTypeOtpSizeRequested(BigDecimal.valueOf(1)));

            VtWs39AfpaBu9ResultRtType response;
            try {
                var result = service.getVtWs39AFPAServicePort().vt39AfpaBu9GetOtp(input);

                response = (result != null) ? result.getVtWs39AfpaBu9ResultRtType() : null;

                if (response == null) {
                    throw new NullPointerException("GSIS returned null response during OTP creation");
                }
            } catch (Exception e) {
                log.error("GSIS OTP creation failed", e);
                throw new GsisException(GsisException.ErrorCodes.GSIS_COMMUNICATION_ERROR, e.getMessage(), e);
            }

            var error = response.getMessageRec();
            if (error != null) {
                var code = error.getMessageCode() != null ? error.getMessageCode().getValue() : null;
                var description = error.getMessageDescr() != null ? error.getMessageDescr().getValue() : null;

                if (code != null && !code.trim().isEmpty() && !"OK".equalsIgnoreCase(code.trim())) {
                    throw new GsisRemoteException(code, description);
                }
            }

            return OtpMapper.map(response).stream()
                    .findFirst()
                    .orElseThrow(() -> new GsisException(GsisException.ErrorCodes.NOT_FOUND, "No OTP was returned after creation request."));
        } catch (GsisRemoteException | GsisException e) {
            throw e;
        } catch (Exception e) {
            throw new GsisException(e.getMessage(), e);
        }
    }

    private void CreateOrUpdateRepresentativeRange(String vat, Representative representative, Representative.Range range) {
        try {
            // Construct input record using generated ObjectFactory
            var factory = new ObjectFactory();
            var input = factory.createVtWs39AfpaBu5InRtType();

            input.setBuyerAfm(factory.createVtWs39AfpaBu3InRtTypeBuyerAfm(vat));
            input.setReprAfm(factory.createVtWs39AfpaBu5InRtTypeReprAfm(representative.id()));
            input.setReprIdentityType(factory.createVtWs39AfpaBu5InRtTypeReprIdentityType(new BigDecimal(range.identity().type())));
            input.setReprIdentityNo(factory.createVtWs39AfpaBu5InRtTypeReprIdentityNo(range.identity().value()));
            input.setReprEmail(factory.createVtWs39AfpaBu5InRtTypeReprEmail(range.email()));
            input.setReprMobile(factory.createVtWs39AfpaBu5InRtTypeReprMobile(range.mobile()));
            input.setReprStartDatetime(factory.createVtWs39AfpaBu5InRtTypeReprStartDatetime(range.start().toString()));
            if (range.end() != null) {
                input.setReprEndDatetime(factory.createVtWs39AfpaBu5InRtTypeReprEndDatetime(range.end().toString()));
            }
            input.setOtpRequired(factory.createVtWs39AfpaBu5InRtTypeOtpRequired(range.otp() ? "Y" : "N"));

            VtWs39AfpaBu5ResultRtType response;
            try {
                var result = service.getVtWs39AFPAServicePort().vt39AfpaBu5SetRepr(input);

                response = (result != null) ? result.getVtWs39AfpaBu5ResultRtType() : null;

                if (response == null) {
                    throw new NullPointerException("GSIS returned null response");
                }
            } catch (Exception e) {
                log.error("GSIS lookup failed", e);

                throw new GsisException(GsisException.ErrorCodes.GSIS_COMMUNICATION_ERROR, e.getMessage(), e);
            }

            var error = response.getMessageRec();
            if (error != null) {
                var code = error.getMessageCode() != null ? error.getMessageCode().getValue() : null;
                var description = error.getMessageDescr() != null ? error.getMessageDescr().getValue() : null;

                if (code != null && !code.trim().isEmpty() && !"OK".equalsIgnoreCase(code.trim())) {
                    throw new GsisRemoteException(code, description);
                }
            }
        } catch (GsisRemoteException | GsisException e) {
            throw e;
        } catch (Exception e) {
            throw new GsisException(e.getMessage(), e);
        }
    }

    private static Boolean hasOverlap(List<Representative.Range> ranges, Representative.Range range) {
        // Κρατάμε τις ημερομηνίες του νέου range που πάμε να βάλουμε
        Instant newStart = range.start();
        Instant newEnd = range.end(); // Μπορεί να είναι null

        return ranges.stream().anyMatch(existingRange -> {
            Instant extStart = existingRange.start();
            Instant extEnd = existingRange.end(); // Μπορεί να είναι null

            // 1. Έλεγχος: newStart <= extEnd (αν το extEnd δεν είναι άπειρο)
            boolean condition1 = (extEnd == null) || (!newStart.isAfter(extEnd));

            // 2. Έλεγχος: extStart <= newEnd (αν το newEnd δεν είναι άπειρο)
            boolean condition2 = (newEnd == null) || (!extStart.isAfter(newEnd));

            // Αν ισχύουν και τα δύο, έχουμε overlap!
            return condition1 && condition2;
        });
    }

    private static Representative map(Map.Entry<String, List<RepresentativeResult>> group) {
        return new Representative(
                group.getKey(),
                group.getValue().getFirst().title(),
                group.getValue()
                        .stream()
                        .map(row -> new Representative.Range(
                                row.id(),
                                new Representative.Identity(row.identityType(), row.identityValue()),
                                row.start(),
                                row.end(),
                                row.email(),
                                row.mobile(),
                                row.otp()
                        ))
                        .toList()
        );
    }

    private static RepresentativeResult map(VtWs39AfpaBu7OutRtType tab) {

        return new RepresentativeResult(
                tab.getInsertCallId().getValue().toString(),
                tab.getReprAfm().getValue().trim(),
                tab.getReprFullname().getValue().trim(),

                tab.getReprIdentityType().getValue().toString(),
                tab.getReprIdentityNo().getValue(),
                Instant.parse(tab.getReprStartDatetime().getValue()),
                !tab.getReprEndDatetime().isNil()
                        ? Instant.parse(tab.getReprEndDatetime().getValue())
                        : null,
                tab.getReprEmail().getValue(),
                tab.getReprMobile().getValue(),
                tab.getOtpRequired().getValue().equals("Y")
        );
    }

    private record RepresentativeResult(
            String id,
            String representativeId,
            String title,

            String identityType,
            String identityValue,

            Instant start,
            Instant end,

            String email,
            String mobile,
            Boolean otp
    ) {

    }
}
