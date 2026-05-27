package gr.bytethat.gsis.controller;

import gr.bytethat.gsis.common.abstractions.exception.GsisException;
import gr.bytethat.gsis.gsis39a.abstractions.Gsis39a;
import gr.bytethat.gsis.gsis39a.abstractions.Otp;
import gr.bytethat.gsis.registry.infrastructure.JacksonConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(Gsis39aController.class)
@Import(JacksonConfiguration.class)
class Gsis39aControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private Gsis39a gsis39a;

    @Test
    void whenValidOtpExists_thenReturnFirstValidOtp() throws Exception {
        String vat = "090165560";
        String representativeId = "803237662";

        Instant now = Instant.now();
        Otp validOtp1 = new Otp("222", "B", now.minus(1, ChronoUnit.HOURS), now.plus(1, ChronoUnit.HOURS));

        when(gsis39a.getOtp(vat, representativeId)).thenReturn(validOtp1);

        mockMvc.perform(get("/api/v1/gsis/39a/buyer/{vat}/representatives/{representativeId}/otp", vat, representativeId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("222"))
                .andExpect(jsonPath("$.usageFlag").value("B"));
    }

    @Test
    void whenNoValidOtpExists_thenReturn404() throws Exception {
        String vat = "090165560";
        String representativeId = "803237662";

        when(gsis39a.getOtp(vat, representativeId))
                .thenThrow(new GsisException(GsisException.ErrorCodes.NOT_FOUND, "No valid OTP found"));

        mockMvc.perform(get("/api/v1/gsis/39a/buyer/{vat}/representatives/{representativeId}/otp", vat, representativeId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void whenOtpListIsEmpty_thenReturn404() throws Exception {
        String vat = "090165560";
        String representativeId = "803237662";

        when(gsis39a.getOtp(vat, representativeId))
                .thenThrow(new GsisException(GsisException.ErrorCodes.NOT_FOUND, "No OTP found"));

        mockMvc.perform(get("/api/v1/gsis/39a/buyer/{vat}/representatives/{representativeId}/otp", vat, representativeId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }
}
