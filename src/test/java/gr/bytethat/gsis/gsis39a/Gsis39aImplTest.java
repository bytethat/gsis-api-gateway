package gr.bytethat.gsis.gsis39a;

import gr.bytethat.gsis.common.abstractions.exception.GsisException;
import gr.bytethat.gsis.common.abstractions.exception.GsisRemoteException;
import gr.bytethat.gsis.gsis39a.abstractions.Buyer;
import gr.bytethat.gsis.gsis39a.abstractions.Otp;
import gr.bytethat.gsis.gsis39a.core.Gsis39aImpl;
import gr.bytethat.gsis.gsis39a.core.Gsis39aOptions;
import gr.bytethat.gsis.gsis39a.core.client.*;
import jakarta.xml.ws.BindingProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class Gsis39aImplTest {

    private VtWs39AFPAService mockService;
    private VtWs39AFPAServiceInterface mockPort;
    private Gsis39aImpl gsis39a;
    private final ObjectFactory factory = new ObjectFactory();

    @BeforeEach
    void setUp() {
        mockService = mock(VtWs39AFPAService.class);
        
        // Mock the port to also implement BindingProvider so dynamic proxy casting works
        mockPort = mock(
                VtWs39AFPAServiceInterface.class,
                Mockito.withSettings().extraInterfaces(BindingProvider.class)
        );

        // Stub getRequestContext to avoid NullPointerException in port calls
        BindingProvider bp = (BindingProvider) mockPort;
        when(bp.getRequestContext()).thenReturn(new HashMap<>());

        // Stub port retrieval on service
        when(mockService.getVtWs39AFPAServicePort()).thenReturn(mockPort);

        Gsis39aOptions options = new Gsis39aOptions(
                "test-user",
                "test-pass",
                "test-caller"
        );

        gsis39a = new Gsis39aImpl(options, mockService);
    }

    // --- Validation Tests ---

    @Test
    void whenVatIsInvalid_thenThrowInvalidVatFormatException() {
        String invalidVat = "12345"; // Invalid format & checksum

        GsisException exception1 = assertThrows(GsisException.class, () -> gsis39a.getBuyer(invalidVat));
        assertEquals(GsisException.ErrorCodes.INVALID_VAT_FORMAT, exception1.getCode());

        GsisException exception2 = assertThrows(GsisException.class, () -> gsis39a.setBuyer(invalidVat, "test@test.com", "123"));
        assertEquals(GsisException.ErrorCodes.INVALID_VAT_FORMAT, exception2.getCode());

        GsisException exception3 = assertThrows(GsisException.class, () -> gsis39a.deleteBuyer(invalidVat));
        assertEquals(GsisException.ErrorCodes.INVALID_VAT_FORMAT, exception3.getCode());
    }

    // --- GET BUYER TESTS ---

    @Test
    void whenGetBuyerIsSuccessful_thenReturnMappedBuyer() {
        // Mathematically valid Greek AFM
        String validVat = "090165560";

        Vt39AfpaBu3GetBuyerResponseType.Result responseResult = mock(Vt39AfpaBu3GetBuyerResponseType.Result.class);
        VtWs39AfpaBu3ResultRtType result = mock(VtWs39AfpaBu3ResultRtType.class);
        GenWsMessageRtType messageRec = mock(GenWsMessageRtType.class);
        VtWs39AfpaBu3OutRtType bu3OutRec = mock(VtWs39AfpaBu3OutRtType.class);

        when(mockPort.vt39AfpaBu3GetBuyer(any())).thenReturn(responseResult);
        when(responseResult.getVtWs39AfpaBu3ResultRtType()).thenReturn(result);
        when(result.getMessageRec()).thenReturn(messageRec);
        when(result.getBu3OutRec()).thenReturn(bu3OutRec);

        // Mock messageRec code as "OK"
        var codeElement = factory.createGenWsMessageRtTypeMessageCode("OK");
        when(messageRec.getMessageCode()).thenReturn(codeElement);

        // Mock buyer values
        when(bu3OutRec.getBuyerAfm()).thenReturn(factory.createVtWs39AfpaBu3OutRtTypeBuyerAfm(validVat));
        when(bu3OutRec.getBuyerFullname()).thenReturn(factory.createVtWs39AfpaBu3OutRtTypeBuyerFullname("Test Company"));
        when(bu3OutRec.getBuyerEmail()).thenReturn(factory.createVtWs39AfpaBu3OutRtTypeBuyerEmail("info@test.gr"));
        when(bu3OutRec.getBuyerMobile()).thenReturn(factory.createVtWs39AfpaBu3OutRtTypeBuyerMobile("6999999999"));
        when(bu3OutRec.getBuyerINiFlag()).thenReturn(factory.createVtWs39AfpaBu3OutRtTypeBuyerINiFlag("2")); // Legal Entity

        Buyer buyer = gsis39a.getBuyer(validVat);

        assertNotNull(buyer);
        assertEquals(validVat, buyer.id());
        assertEquals("Test Company", buyer.title());
        assertEquals("info@test.gr", buyer.email());
        assertEquals("6999999999", buyer.mobile());
        assertEquals(Buyer.Type.LEGAL_ENTITY, buyer.type());
    }

    @Test
    void whenGetBuyerReturnsBusinessError_thenThrowGsisRemoteException() {
        String validVat = "090165560";

        Vt39AfpaBu3GetBuyerResponseType.Result responseResult = mock(Vt39AfpaBu3GetBuyerResponseType.Result.class);
        VtWs39AfpaBu3ResultRtType result = mock(VtWs39AfpaBu3ResultRtType.class);
        GenWsMessageRtType messageRec = mock(GenWsMessageRtType.class);

        when(mockPort.vt39AfpaBu3GetBuyer(any())).thenReturn(responseResult);
        when(responseResult.getVtWs39AfpaBu3ResultRtType()).thenReturn(result);
        when(result.getMessageRec()).thenReturn(messageRec);

        var codeElement = factory.createGenWsMessageRtTypeMessageCode("VT39A_INVALID_CREDENTIALS");
        var descrElement = factory.createGenWsMessageRtTypeMessageDescr("Invalid SOAP credentials");
        when(messageRec.getMessageCode()).thenReturn(codeElement);
        when(messageRec.getMessageDescr()).thenReturn(descrElement);

        GsisRemoteException exception = assertThrows(GsisRemoteException.class, () -> gsis39a.getBuyer(validVat));
        assertEquals("VT39A_INVALID_CREDENTIALS", exception.getCode());
        assertEquals("Invalid SOAP credentials", exception.getDescription());
    }

    @Test
    void whenGetBuyerSoapFails_thenThrowGsisException() {
        String validVat = "090165560";

        when(mockPort.vt39AfpaBu3GetBuyer(any())).thenThrow(new RuntimeException("Connection timeout"));

        GsisException exception = assertThrows(GsisException.class, () -> gsis39a.getBuyer(validVat));
        assertEquals(GsisException.ErrorCodes.GSIS_COMMUNICATION_ERROR, exception.getCode());
        assertTrue(exception.getMessage().contains("Connection timeout"));
    }

    // --- SET BUYER TESTS ---

    @Test
    void whenSetBuyerIsSuccessful_thenExecuteWithoutErrors() {
        String validVat = "090165560";

        Vt39AfpaBu1SetBuyerResponseType.Result responseResult = mock(Vt39AfpaBu1SetBuyerResponseType.Result.class);
        VtWs39AfpaBu1ResultRtType result = mock(VtWs39AfpaBu1ResultRtType.class);
        GenWsMessageRtType messageRec = mock(GenWsMessageRtType.class);

        when(mockPort.vt39AfpaBu1SetBuyer(any())).thenReturn(responseResult);
        when(responseResult.getVtWs39AfpaBu1ResultRtType()).thenReturn(result);
        when(result.getMessageRec()).thenReturn(messageRec);

        var codeElement = factory.createGenWsMessageRtTypeMessageCode("OK");
        when(messageRec.getMessageCode()).thenReturn(codeElement);

        assertDoesNotThrow(() -> gsis39a.setBuyer(validVat, "test@test.gr", "6999999999"));
    }

    @Test
    void whenSetBuyerReturnsBusinessError_thenThrowGsisRemoteException() {
        String validVat = "090165560";

        Vt39AfpaBu1SetBuyerResponseType.Result responseResult = mock(Vt39AfpaBu1SetBuyerResponseType.Result.class);
        VtWs39AfpaBu1ResultRtType result = mock(VtWs39AfpaBu1ResultRtType.class);
        GenWsMessageRtType messageRec = mock(GenWsMessageRtType.class);

        when(mockPort.vt39AfpaBu1SetBuyer(any())).thenReturn(responseResult);
        when(responseResult.getVtWs39AfpaBu1ResultRtType()).thenReturn(result);
        when(result.getMessageRec()).thenReturn(messageRec);

        var codeElement = factory.createGenWsMessageRtTypeMessageCode("ERROR_01");
        var descrElement = factory.createGenWsMessageRtTypeMessageDescr("Operation rejected");
        when(messageRec.getMessageCode()).thenReturn(codeElement);
        when(messageRec.getMessageDescr()).thenReturn(descrElement);

        GsisRemoteException exception = assertThrows(GsisRemoteException.class, () -> gsis39a.setBuyer(validVat, "test@test.gr", "6999999999"));
        assertEquals("ERROR_01", exception.getCode());
        assertEquals("Operation rejected", exception.getDescription());
    }

    // --- DELETE BUYER TESTS ---

    @Test
    void whenDeleteBuyerIsSuccessful_thenExecuteWithoutErrors() {
        String validVat = "090165560";

        Vt39AfpaBu2DelBuyerResponseType.Result responseResult = mock(Vt39AfpaBu2DelBuyerResponseType.Result.class);
        VtWs39AfpaBu2ResultRtType result = mock(VtWs39AfpaBu2ResultRtType.class);
        GenWsMessageRtType messageRec = mock(GenWsMessageRtType.class);

        when(mockPort.vt39AfpaBu2DelBuyer(any())).thenReturn(responseResult);
        when(responseResult.getVtWs39AfpaBu2ResultRtType()).thenReturn(result);
        when(result.getMessageRec()).thenReturn(messageRec);

        var codeElement = factory.createGenWsMessageRtTypeMessageCode("OK");
        when(messageRec.getMessageCode()).thenReturn(codeElement);

        assertDoesNotThrow(() -> gsis39a.deleteBuyer(validVat));
    }

    @Test
    void whenDeleteBuyerReturnsBusinessError_thenThrowGsisRemoteException() {
        String validVat = "090165560";

        Vt39AfpaBu2DelBuyerResponseType.Result responseResult = mock(Vt39AfpaBu2DelBuyerResponseType.Result.class);
        VtWs39AfpaBu2ResultRtType result = mock(VtWs39AfpaBu2ResultRtType.class);
        GenWsMessageRtType messageRec = mock(GenWsMessageRtType.class);

        when(mockPort.vt39AfpaBu2DelBuyer(any())).thenReturn(responseResult);
        when(responseResult.getVtWs39AfpaBu2ResultRtType()).thenReturn(result);
        when(result.getMessageRec()).thenReturn(messageRec);

        var codeElement = factory.createGenWsMessageRtTypeMessageCode("VT39A_OTP_DEACTIVATION_FAIL");
        var descrElement = factory.createGenWsMessageRtTypeMessageDescr("No active registration found");
        when(messageRec.getMessageCode()).thenReturn(codeElement);
        when(messageRec.getMessageDescr()).thenReturn(descrElement);

        GsisRemoteException exception = assertThrows(GsisRemoteException.class, () -> gsis39a.deleteBuyer(validVat));
        assertEquals("VT39A_OTP_DEACTIVATION_FAIL", exception.getCode());
        assertEquals("No active registration found", exception.getDescription());
    }

    @Test
    void whenDeleteBuyerSoapFails_thenThrowGsisException() {
        String validVat = "090165560";

        when(mockPort.vt39AfpaBu2DelBuyer(any())).thenThrow(new RuntimeException("SOAP connection failure"));

        GsisException exception = assertThrows(GsisException.class, () -> gsis39a.deleteBuyer(validVat));
        assertEquals(GsisException.ErrorCodes.GSIS_COMMUNICATION_ERROR, exception.getCode());
        assertTrue(exception.getMessage().contains("SOAP connection failure"));
    }

    // --- GET OTP TESTS ---

    @Test
    void whenGetOtpIsSuccessful_thenReturnMappedOtps() {
        String validBuyerVat = "090165560";
        String validReprVat = "803237662";

        Vt39AfpaBu9GetOtpResponseType.Result responseResult = mock(Vt39AfpaBu9GetOtpResponseType.Result.class);
        VtWs39AfpaBu9ResultRtType result = mock(VtWs39AfpaBu9ResultRtType.class);
        GenWsMessageRtType messageRec = mock(GenWsMessageRtType.class);
        VtWs39AfpaBu9ResultRtType.Bu9OtpoutTab otpoutTab = mock(VtWs39AfpaBu9ResultRtType.Bu9OtpoutTab.class);

        when(mockPort.vt39AfpaBu9GetOtp(any())).thenReturn(responseResult);
        when(responseResult.getVtWs39AfpaBu9ResultRtType()).thenReturn(result);
        when(result.getMessageRec()).thenReturn(messageRec);
        when(result.getBu9OtpoutTab()).thenReturn(otpoutTab);

        // Mock messageRec code as "OK"
        var codeElement = factory.createGenWsMessageRtTypeMessageCode("OK");
        when(messageRec.getMessageCode()).thenReturn(codeElement);

        // Mock OTP items
        VtWs39AfpaOtpoutRtType item = mock(VtWs39AfpaOtpoutRtType.class);
        when(item.getOtpId()).thenReturn(factory.createVtWs39AfpaOtpoutRtTypeOtpId(new java.math.BigDecimal("12345")));
        when(item.getOtpUsageFlag()).thenReturn(factory.createVtWs39AfpaOtpoutRtTypeOtpUsageFlag("A"));

        java.time.Instant now = java.time.Instant.now();
        when(item.getOtpValidStartDatetime()).thenReturn(factory.createVtWs39AfpaOtpoutRtTypeOtpValidStartDatetime(now.minus(10, java.time.temporal.ChronoUnit.MINUTES).toString()));
        when(item.getOtpValidEndDatetime()).thenReturn(factory.createVtWs39AfpaOtpoutRtTypeOtpValidEndDatetime(now.plus(10, java.time.temporal.ChronoUnit.MINUTES).toString()));

        when(otpoutTab.getItem()).thenReturn(java.util.List.of(item));

        var otp = gsis39a.getOtp(validBuyerVat, validReprVat);

        assertNotNull(otp);
        assertEquals("12345", otp.id());
        assertEquals("A", otp.usageFlag());
    }

    @Test
    void whenNoActiveOtpExists_thenIssueNewOtp() {
        String validBuyerVat = "090165560";
        String validReprVat = "803237662";

        // First call (Get OTP) returns "39AFPA_OTP_NEW_NOTFOUND"
        Vt39AfpaBu9GetOtpResponseType.Result getOtpResponseResult = mock(Vt39AfpaBu9GetOtpResponseType.Result.class);
        VtWs39AfpaBu9ResultRtType getOtpResult = mock(VtWs39AfpaBu9ResultRtType.class);
        GenWsMessageRtType getOtpMessageRec = mock(GenWsMessageRtType.class);
        when(getOtpResult.getMessageRec()).thenReturn(getOtpMessageRec);
        when(getOtpResponseResult.getVtWs39AfpaBu9ResultRtType()).thenReturn(getOtpResult);

        var notFoundCodeElement = factory.createGenWsMessageRtTypeMessageCode("39AFPA_OTP_NEW_NOTFOUND");
        when(getOtpMessageRec.getMessageCode()).thenReturn(notFoundCodeElement);

        // Second call (Issue New OTP) returns OK and a newly created OTP
        Vt39AfpaBu9GetOtpResponseType.Result createResponseResult = mock(Vt39AfpaBu9GetOtpResponseType.Result.class);
        VtWs39AfpaBu9ResultRtType createResult = mock(VtWs39AfpaBu9ResultRtType.class);
        GenWsMessageRtType createMessageRec = mock(GenWsMessageRtType.class);
        VtWs39AfpaBu9ResultRtType.Bu9OtpoutTab createOtpoutTab = mock(VtWs39AfpaBu9ResultRtType.Bu9OtpoutTab.class);

        when(createResult.getMessageRec()).thenReturn(createMessageRec);
        when(createResult.getBu9OtpoutTab()).thenReturn(createOtpoutTab);
        when(createResponseResult.getVtWs39AfpaBu9ResultRtType()).thenReturn(createResult);

        var okCodeElement = factory.createGenWsMessageRtTypeMessageCode("OK");
        when(createMessageRec.getMessageCode()).thenReturn(okCodeElement);

        // Mock OTP item returned by creation
        VtWs39AfpaOtpoutRtType newItem = mock(VtWs39AfpaOtpoutRtType.class);
        when(newItem.getOtpId()).thenReturn(factory.createVtWs39AfpaOtpoutRtTypeOtpId(new java.math.BigDecimal("99999")));
        when(newItem.getOtpUsageFlag()).thenReturn(factory.createVtWs39AfpaOtpoutRtTypeOtpUsageFlag("A"));

        java.time.Instant now = java.time.Instant.now();
        when(newItem.getOtpValidStartDatetime()).thenReturn(factory.createVtWs39AfpaOtpoutRtTypeOtpValidStartDatetime(now.minus(5, java.time.temporal.ChronoUnit.MINUTES).toString()));
        when(newItem.getOtpValidEndDatetime()).thenReturn(factory.createVtWs39AfpaOtpoutRtTypeOtpValidEndDatetime(now.plus(5, java.time.temporal.ChronoUnit.MINUTES).toString()));

        when(createOtpoutTab.getItem()).thenReturn(java.util.List.of(newItem));

        // Stub mockPort vt39AfpaBu9GetOtp to return getOtpResponseResult first, then createResponseResult
        when(mockPort.vt39AfpaBu9GetOtp(any()))
                .thenReturn(getOtpResponseResult)
                .thenReturn(createResponseResult);

        Otp otp = gsis39a.getOtp(validBuyerVat, validReprVat);

        assertNotNull(otp);
        assertEquals("99999", otp.id());
        assertEquals("A", otp.usageFlag());
    }

    @Test
    void whenGetOtpReturnsBusinessError_thenThrowGsisRemoteException() {
        String validBuyerVat = "090165560";
        String validReprVat = "803237662";

        Vt39AfpaBu9GetOtpResponseType.Result responseResult = mock(Vt39AfpaBu9GetOtpResponseType.Result.class);
        VtWs39AfpaBu9ResultRtType result = mock(VtWs39AfpaBu9ResultRtType.class);
        GenWsMessageRtType messageRec = mock(GenWsMessageRtType.class);

        when(mockPort.vt39AfpaBu9GetOtp(any())).thenReturn(responseResult);
        when(responseResult.getVtWs39AfpaBu9ResultRtType()).thenReturn(result);
        when(result.getMessageRec()).thenReturn(messageRec);

        var codeElement = factory.createGenWsMessageRtTypeMessageCode("VT39A_INVALID_CREDENTIALS");
        var descrElement = factory.createGenWsMessageRtTypeMessageDescr("Invalid SOAP credentials");
        when(messageRec.getMessageCode()).thenReturn(codeElement);
        when(messageRec.getMessageDescr()).thenReturn(descrElement);

        GsisRemoteException exception = assertThrows(GsisRemoteException.class, () -> gsis39a.getOtp(validBuyerVat, validReprVat));
        assertEquals("VT39A_INVALID_CREDENTIALS", exception.getCode());
        assertEquals("Invalid SOAP credentials", exception.getDescription());
    }

    @Test
    void whenGetOtpSoapFails_thenThrowGsisException() {
        String validBuyerVat = "090165560";
        String validReprVat = "803237662";

        when(mockPort.vt39AfpaBu9GetOtp(any())).thenThrow(new RuntimeException("SOAP connection failure"));

        GsisException exception = assertThrows(GsisException.class, () -> gsis39a.getOtp(validBuyerVat, validReprVat));
        assertEquals(GsisException.ErrorCodes.GSIS_COMMUNICATION_ERROR, exception.getCode());
        assertTrue(exception.getMessage().contains("SOAP connection failure"));
    }

    @Test
    void whenGetOtpInputsAreInvalid_thenThrowInvalidVatFormatException() {
        String invalidVat = "12345";
        String validVat = "090165560";

        GsisException exception1 = assertThrows(GsisException.class, () -> gsis39a.getOtp(invalidVat, validVat));
        assertEquals(GsisException.ErrorCodes.INVALID_VAT_FORMAT, exception1.getCode());

        GsisException exception2 = assertThrows(GsisException.class, () -> gsis39a.getOtp(validVat, invalidVat));
        assertEquals(GsisException.ErrorCodes.INVALID_VAT_FORMAT, exception2.getCode());
    }
}
