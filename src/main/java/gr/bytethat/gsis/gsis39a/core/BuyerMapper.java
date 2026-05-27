package gr.bytethat.gsis.gsis39a.core;

import gr.bytethat.gsis.gsis39a.abstractions.Buyer;
import gr.bytethat.gsis.gsis39a.core.client.VtWs39AfpaBu3ResultRtType;

public class BuyerMapper {
    public static Buyer map(VtWs39AfpaBu3ResultRtType result) {
        var messageRec = result.getBu3OutRec();

        return new Buyer(
                !messageRec.getBuyerAfm().isNil()
                        ? messageRec.getBuyerAfm().getValue().trim()
                        : null,
                messageRec.getBuyerFullname().getValue(),
                messageRec.getBuyerEmail().getValue(),
                messageRec.getBuyerMobile().getValue(),
                switch (messageRec.getBuyerINiFlag().getValue()) {
                    case "1" -> Buyer.Type.NATURAL_PERSON;
                    case "2" -> Buyer.Type.LEGAL_ENTITY;
                    default ->
                            throw new IllegalArgumentException("Unknown INiFlagDescr value: " + messageRec.getBuyerINiFlag().getValue());
                }
        );
    }
}

