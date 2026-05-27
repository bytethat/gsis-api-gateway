package gr.bytethat.gsis.registry.core;

import gr.bytethat.gsis.registry.core.client.RgWsPublic2ResultRtType;
import gr.bytethat.gsis.registry.abstractions.BusinessDetails;

import java.util.List;

final class BusinessDetailsMapper {
    public static BusinessDetails map(RgWsPublic2ResultRtType result) {
        return new BusinessDetails(
                result.getBasicRec().getAfm().getValue(),
                result.getBasicRec().getDoyDescr().getValue(),
                result.getBasicRec().getDeactivationFlag().getValue().equalsIgnoreCase("1"),
                switch (result.getBasicRec().getINiFlagDescr().getValue()) {
                    case "ΦΠ" -> BusinessDetails.Type.NATURAL_PERSON;
                    case "ΜΗ ΦΠ" -> BusinessDetails.Type.LEGAL_ENTITY;
                    default ->
                            throw new IllegalArgumentException("Unknown INiFlagDescr value: " + result.getBasicRec().getINiFlagDescr().getValue());
                },
                switch (result.getBasicRec().getFirmFlagDescr().getValue()) {
                    case "ΕΠΙΤΗΔΕΥΜΑΤΙΑΣ" -> BusinessDetails.Status.BUSINESS;
                    case "ΜΗ ΕΠΙΤΗΔΕΥΜΑΤΙΑΣ" -> BusinessDetails.Status.INDIVIDUAL;
                    case "ΠΡΩΗΝ ΕΠΙΤΗΔΕΥΜΑΤΙΑΣ" -> BusinessDetails.Status.INACTIVE;
                    default ->
                            throw new IllegalArgumentException("Unknown FirmFlagDescr value: " + result.getBasicRec().getINiFlagDescr().getValue());
                },
                !result.getBasicRec().getNormalVatSystemFlag().isNil()
                        ? result.getBasicRec().getNormalVatSystemFlag().getValue().equalsIgnoreCase("Y")
                        : null,
                result.getBasicRec().getOnomasia().getValue(),
                result.getBasicRec().getCommerTitle().getValue(),
                !result.getBasicRec().getRegistDate().isNil()
                        ? result.getBasicRec().getRegistDate().getValue().toGregorianCalendar().toZonedDateTime().toInstant()
                        : null,
                !result.getBasicRec().getStopDate().isNil()
                        ? result.getBasicRec().getStopDate().getValue().toGregorianCalendar().toZonedDateTime().toInstant()
                        : null,
                !result.getBasicRec().getPostalAddress().isNil()
                        || !result.getBasicRec().getPostalAddressNo().isNil()
                        || !result.getBasicRec().getPostalAreaDescription().isNil()
                        || !result.getBasicRec().getPostalZipCode().isNil()
                        ?
                        new BusinessDetails.Address(
                                result.getBasicRec().getPostalAddress().getValue(),
                                result.getBasicRec().getPostalAddressNo().getValue(),
                                result.getBasicRec().getPostalAreaDescription().getValue(),
                                result.getBasicRec().getPostalZipCode().getValue()
                        )
                        : null,
                result.getFirmActTab() != null && !result.getFirmActTab().getItem().isEmpty()
                        ? result.getFirmActTab().getItem().stream().map(x -> new BusinessDetails.Activity(
                        x.getFirmActCode().getValue().toString(),
                        x.getFirmActDescr().getValue(),
                        switch (x.getFirmActKind().getValue()) {
                            case "1" -> BusinessDetails.Activity.Type.PRIMARY;
                            case "2" -> BusinessDetails.Activity.Type.SECONDARY;
                            case "3" -> BusinessDetails.Activity.Type.OTHER;
                            case "4" -> BusinessDetails.Activity.Type.ANCILLARY;
                            default ->
                                    throw new IllegalArgumentException("Unknown FirmActKind value: " + x.getFirmActKind().getValue());
                        }
                )).toList()
                        : List.of()
        );
    }
}
