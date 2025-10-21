package pl.kielce.tu.backend.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;

import pl.kielce.tu.backend.model.dto.ResendVerificationRequestDto;
import pl.kielce.tu.backend.model.dto.VerificationRequestDto;
import pl.kielce.tu.backend.service.verification.VerificationService;

@ExtendWith(MockitoExtension.class)
class VerificationControllerTest {

    @Mock
    private VerificationService verificationService;

    @InjectMocks
    private VerificationController verificationController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    private VerificationRequestDto verificationRequest;
    private ResendVerificationRequestDto resendRequest;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(verificationController).build();
        objectMapper = new ObjectMapper();

        verificationRequest = VerificationRequestDto.builder()
                .email("test@example.com")
                .code("123456")
                .build();

        resendRequest = ResendVerificationRequestDto.builder()
                .email("test@example.com")
                .build();
    }

    @Test
    void verifyEmail_Success() throws Exception {
        when(verificationService.verifyCode(any(VerificationRequestDto.class)))
                .thenReturn(ResponseEntity.status(HttpStatus.OK).build());

        mockMvc.perform(post("/api/v1/verification/verify")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(verificationRequest)))
                .andExpect(status().isOk());

        verify(verificationService).verifyCode(any(VerificationRequestDto.class));
    }

    @Test
    void verifyEmail_Forbidden() throws Exception {
        when(verificationService.verifyCode(any(VerificationRequestDto.class)))
                .thenReturn(ResponseEntity.status(HttpStatus.FORBIDDEN).build());

        mockMvc.perform(post("/api/v1/verification/verify")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(verificationRequest)))
                .andExpect(status().isForbidden());

        verify(verificationService).verifyCode(any(VerificationRequestDto.class));
    }

    @Test
    void verifyEmail_InternalServerError() throws Exception {
        when(verificationService.verifyCode(any(VerificationRequestDto.class)))
                .thenReturn(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());

        mockMvc.perform(post("/api/v1/verification/verify")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(verificationRequest)))
                .andExpect(status().isInternalServerError());

        verify(verificationService).verifyCode(any(VerificationRequestDto.class));
    }

    @Test
    void resendVerificationCode_Success() throws Exception {
        when(verificationService.resendVerificationCode(any(ResendVerificationRequestDto.class)))
                .thenReturn(ResponseEntity.status(HttpStatus.OK).build());

        mockMvc.perform(post("/api/v1/verification/resend")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(resendRequest)))
                .andExpect(status().isOk());

        verify(verificationService).resendVerificationCode(any(ResendVerificationRequestDto.class));
    }

    @Test
    void resendVerificationCode_InternalServerError() throws Exception {
        when(verificationService.resendVerificationCode(any(ResendVerificationRequestDto.class)))
                .thenReturn(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());

        mockMvc.perform(post("/api/v1/verification/resend")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(resendRequest)))
                .andExpect(status().isInternalServerError());

        verify(verificationService).resendVerificationCode(any(ResendVerificationRequestDto.class));
    }

}
