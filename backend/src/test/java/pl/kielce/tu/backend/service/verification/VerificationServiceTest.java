package pl.kielce.tu.backend.service.verification;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

import pl.kielce.tu.backend.model.dto.ResendVerificationRequestDto;
import pl.kielce.tu.backend.model.dto.VerificationRequestDto;
import pl.kielce.tu.backend.model.entity.User;
import pl.kielce.tu.backend.repository.UserRepository;
import pl.kielce.tu.backend.service.email.EmailService;

@ExtendWith(MockitoExtension.class)
class VerificationServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private VerificationService verificationService;

    private User user;
    private VerificationRequestDto verificationRequest;
    private ResendVerificationRequestDto resendRequest;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(verificationService, "codeExpirationMs", 900000L);

        user = User.builder()
                .id(1L)
                .email("test@example.com")
                .nickname("testuser")
                .password("encodedPassword")
                .age(25)
                .isVerified(false)
                .build();

        verificationRequest = VerificationRequestDto.builder()
                .email("test@example.com")
                .code("123456")
                .build();

        resendRequest = ResendVerificationRequestDto.builder()
                .email("test@example.com")
                .build();
    }

    @Test
    void generateAndSendVerificationCode_Success() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);
        doNothing().when(emailService).sendVerificationEmail(anyString(), anyString());

        verificationService.generateAndSendVerificationCode("test@example.com");

        verify(userRepository).findByEmail("test@example.com");
        verify(userRepository).save(any(User.class));
        verify(emailService).sendVerificationEmail(eq("test@example.com"), anyString());
    }

    @Test
    void generateAndSendVerificationCode_UserNotFound() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());

        try {
            verificationService.generateAndSendVerificationCode("test@example.com");
        } catch (IllegalArgumentException e) {
            assertEquals("User not found", e.getMessage());
        }

        verify(userRepository).findByEmail("test@example.com");
        verify(userRepository, never()).save(any(User.class));
        verify(emailService, never()).sendVerificationEmail(anyString(), anyString());
    }

    @Test
    void verifyCode_Success() {
        user.setVerificationCode("123456");
        user.setVerificationCodeExpiry(LocalDateTime.now().plusMinutes(10));
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        ResponseEntity<Void> response = verificationService.verifyCode(verificationRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(userRepository).findByEmail("test@example.com");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void verifyCode_InvalidCode() {
        user.setVerificationCode("654321");
        user.setVerificationCodeExpiry(LocalDateTime.now().plusMinutes(10));
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));

        ResponseEntity<Void> response = verificationService.verifyCode(verificationRequest);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        verify(userRepository).findByEmail("test@example.com");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void verifyCode_ExpiredCode() {
        user.setVerificationCode("123456");
        user.setVerificationCodeExpiry(LocalDateTime.now().minusMinutes(10));
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));

        ResponseEntity<Void> response = verificationService.verifyCode(verificationRequest);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        verify(userRepository).findByEmail("test@example.com");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void verifyCode_UserAlreadyVerified() {
        user.setIsVerified(true);
        user.setVerificationCode("123456");
        user.setVerificationCodeExpiry(LocalDateTime.now().plusMinutes(10));
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));

        ResponseEntity<Void> response = verificationService.verifyCode(verificationRequest);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        verify(userRepository).findByEmail("test@example.com");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void verifyCode_NoCodeFound() {
        user.setVerificationCode(null);
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));

        ResponseEntity<Void> response = verificationService.verifyCode(verificationRequest);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        verify(userRepository).findByEmail("test@example.com");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void verifyCode_UserNotFound() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());

        ResponseEntity<Void> response = verificationService.verifyCode(verificationRequest);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        verify(userRepository).findByEmail("test@example.com");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void verifyCode_InternalError() {
        when(userRepository.findByEmail("test@example.com")).thenThrow(new RuntimeException("DB error"));

        ResponseEntity<Void> response = verificationService.verifyCode(verificationRequest);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        verify(userRepository).findByEmail("test@example.com");
    }

    @Test
    void resendVerificationCode_Success() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);
        doNothing().when(emailService).sendVerificationEmail(anyString(), anyString());

        ResponseEntity<Void> response = verificationService.resendVerificationCode(resendRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(userRepository).findByEmail("test@example.com");
        verify(userRepository).save(any(User.class));
        verify(emailService).sendVerificationEmail(eq("test@example.com"), anyString());
    }

    @Test
    void resendVerificationCode_Error() {
        when(userRepository.findByEmail("test@example.com")).thenThrow(new RuntimeException("DB error"));

        ResponseEntity<Void> response = verificationService.resendVerificationCode(resendRequest);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        verify(userRepository).findByEmail("test@example.com");
        verify(emailService, never()).sendVerificationEmail(anyString(), anyString());
    }

}
