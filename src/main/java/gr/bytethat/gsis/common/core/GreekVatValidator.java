package gr.bytethat.gsis.common.core;

public final class GreekVatValidator {
    /**
     * Implements standard AFM validation using the Modulo 11 check digit algorithm.
     */
    public static boolean isValid(String vat) {
        if (vat == null || vat.length() != 9) {
            return false;
        }
        if (!vat.matches("\\d{9}")) {
            return false;
        }

        int sum = 0;
        for (int i = 0; i < 8; i++) {
            int digit = Character.getNumericValue(vat.charAt(i));
            sum += digit * (1 << (8 - i)); // Multipliers: 256, 128, 64, 32, 16, 8, 4, 2
        }

        int remainder = sum % 11;
        int checkDigit = Character.getNumericValue(vat.charAt(8));
        int expectedCheckDigit = remainder % 10; // if remainder is 10, checkDigit must be 0

        return checkDigit == expectedCheckDigit;
    }
}
