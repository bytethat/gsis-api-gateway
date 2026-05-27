package gr.bytethat.gsis.controller;

import gr.bytethat.gsis.common.abstractions.exception.GsisException;
import gr.bytethat.gsis.common.abstractions.exception.GsisRemoteException;
import gr.bytethat.gsis.registry.abstractions.BusinessDetails;
import gr.bytethat.gsis.registry.abstractions.GsisRegistry;
import gr.bytethat.gsis.registry.infrastructure.JacksonConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(GsisRegistryController.class)
@Import(JacksonConfiguration.class)
class GsisControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GsisRegistry gsisRegistry;

    @Test
    void whenAfmIsValid_thenReturnGsisResponse() throws Exception {
        // AFM 090165560 is a mathematically valid Greek AFM
        String validAfm = "090165560";

        BusinessDetails mockResult = new BusinessDetails(
                validAfm,
                "ΔΟΥ ΑΘΗΝΩΝ",
                true,
                BusinessDetails.Type.LEGAL_ENTITY,
                BusinessDetails.Status.BUSINESS,
                true,
                "HELLENIC STATE PUBLIC CORP TEST",
                "TEST CORP",
                java.time.Instant.now(),
                null,
                new BusinessDetails.Address("Stadiou", "10", "Athens", "10564"),
                java.util.List.of(new BusinessDetails.Activity("62010000", "Software development", BusinessDetails.Activity.Type.PRIMARY))
        );

        when(gsisRegistry.lookup(validAfm)).thenReturn(mockResult);

        mockMvc.perform(get("/api/v1/gsis/registry/lookup/{afm}", validAfm)
                        .header("X-GSIS-Username", "user")
                        .header("X-GSIS-Password", "pass")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(validAfm))
                .andExpect(jsonPath("$.title").value("HELLENIC STATE PUBLIC CORP TEST"))
                .andExpect(jsonPath("$.type").value("Entity"))
                .andExpect(jsonPath("$.status").value("Business"))
                .andExpect(jsonPath("$.Activities[0].type").value("Primary"));
    }

    @Test
    void whenAfmIsInvalidFormat_thenReturn400() throws Exception {
        // Mock the validation failures when using GsisRegistry
        String invalidSizeAfm = "12345";
        String nonNumericAfm = "12345678a";
        String invalidChecksumAfm = "123456789";

        String validationMessage = "Invalid VAT format. Must be a 9-digit numeric string with a valid check digit.";

        when(gsisRegistry.lookup(invalidSizeAfm))
                .thenThrow(new GsisException(GsisException.ErrorCodes.INVALID_VAT_FORMAT, validationMessage));
        when(gsisRegistry.lookup(nonNumericAfm))
                .thenThrow(new GsisException(GsisException.ErrorCodes.INVALID_VAT_FORMAT, validationMessage));
        when(gsisRegistry.lookup(invalidChecksumAfm))
                .thenThrow(new GsisException(GsisException.ErrorCodes.INVALID_VAT_FORMAT, validationMessage));

        // Invalid size
        mockMvc.perform(get("/api/v1/gsis/registry/lookup/{afm}", invalidSizeAfm)
                        .header("X-GSIS-Username", "user")
                        .header("X-GSIS-Password", "pass")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.code").value("INVALID_VAT_FORMAT"));

        // Non-numeric
        mockMvc.perform(get("/api/v1/gsis/registry/lookup/{afm}", nonNumericAfm)
                        .header("X-GSIS-Username", "user")
                        .header("X-GSIS-Password", "pass")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_VAT_FORMAT"));

        // Invalid checksum
        mockMvc.perform(get("/api/v1/gsis/registry/lookup/{afm}", invalidChecksumAfm)
                        .header("X-GSIS-Username", "user")
                        .header("X-GSIS-Password", "pass")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid VAT format. Must be a 9-digit numeric string with a valid check digit."));
    }

    @Test
    void whenGsisReturnsBusinessError_thenReturn422() throws Exception {
        String validAfm = "090165560";
        when(gsisRegistry.lookup(validAfm))
                .thenThrow(new GsisRemoteException("RG_WS_PUBLIC_NO_DATA", "No company found for this AFM"));

        mockMvc.perform(get("/api/v1/gsis/registry/lookup/{afm}", validAfm)
                        .header("X-GSIS-Username", "user")
                        .header("X-GSIS-Password", "pass")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.error").value("GSIS Remote Error"))
                .andExpect(jsonPath("$.code").value("RG_WS_PUBLIC_NO_DATA"))
                .andExpect(jsonPath("$.message").value("No company found for this AFM"));
    }

    @Test
    void whenGsisServiceThrowsSoapFault_thenReturn502() throws Exception {
        String validAfm = "090165560";
        when(gsisRegistry.lookup(validAfm))
                .thenThrow(new GsisException(GsisException.ErrorCodes.GSIS_COMMUNICATION_ERROR, "Connection timed out", new RuntimeException("Timeout")));

        mockMvc.perform(get("/api/v1/gsis/registry/lookup/{afm}", validAfm)
                        .header("X-GSIS-Username", "user")
                        .header("X-GSIS-Password", "pass")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.error").value("Bad Gateway"))
                .andExpect(jsonPath("$.code").value("GSIS_COMMUNICATION_ERROR"))
                .andExpect(jsonPath("$.message").value("Connection timed out"));
    }
}
