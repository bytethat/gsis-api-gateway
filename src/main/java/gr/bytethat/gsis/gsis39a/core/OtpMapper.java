package gr.bytethat.gsis.gsis39a.core;

import gr.bytethat.gsis.gsis39a.abstractions.Otp;
import gr.bytethat.gsis.gsis39a.core.client.VtWs39AfpaBu9ResultRtType;
import gr.bytethat.gsis.gsis39a.core.client.VtWs39AfpaOtpoutRtType;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class OtpMapper {

    public static List<Otp> map(VtWs39AfpaBu9ResultRtType result) {
        List<Otp> otps = new ArrayList<>();
        if (result == null) {
            return otps;
        }

        if (result.getBu9OtpoutTab() != null && result.getBu9OtpoutTab().getItem() != null) {
            for (VtWs39AfpaOtpoutRtType item : result.getBu9OtpoutTab().getItem()) {
                otps.add(new Otp(
                        getSafeString(item.getOtpId()),
                        getSafeString(item.getOtpUsageFlag()),
                        parseDateTime(getSafeString(item.getOtpValidStartDatetime())),
                        parseDateTime(getSafeString(item.getOtpValidEndDatetime()))
                ));
            }
        }

        return otps;
    }

    private static String getSafeString(jakarta.xml.bind.JAXBElement<?> element) {
        if (element == null || element.isNil() || element.getValue() == null) {
            return null;
        }
        return element.getValue().toString().trim();
    }

    private static Instant parseDateTime(String datetime) {
        if (datetime == null || datetime.trim().isEmpty()) {
            return null;
        }
        try {
            return Instant.parse(datetime.trim());
        } catch (Exception e) {
            return null;
        }
    }
}
