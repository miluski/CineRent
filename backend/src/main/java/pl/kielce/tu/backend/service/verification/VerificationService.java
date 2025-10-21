package pl.kielce.tu.backend.service.verification;

import java.security.SecureRandom;
import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import pl.kielce.tu.backend.exception.EmailSendingException;
import pl.kielce.tu.backend.model.dto.ResendVerificationRequestDto;
import pl.kielce.tu.backend.model.dto.VerificationRequestDto;
import pl.kielce.tu.backend.model.entity.User;
import pl.kielce.tu.backend.repository.UserRepository;
import pl.kielce.tu.backend.service.email.EmailService;

@Slf4j
@Service
@RequiredArgsConstructor
public class VerificationService {

    private static final SecureRandom RANDOM = new SecureRandom();

    private final UserRepository userRepository;
    private final EmailService emailService;

    @Value("${verification.code.expiration:900000}")
    private long codeExpirationMs;

    @Transactional
    public void generateAndSendVerificationCode(String email) {
        User user = findUserByEmail(email);
        String code = generateVerificationCode();
        saveVerificationCode(user, code);
        sendVerificationEmail(email, code);
    }

    private void sendVerificationEmail(String email, String code) {
        try {
            emailService.sendVerificationEmail(email, code);
        } catch (Exception e) {
            log.error("Failed to send verification email to: {}", email, e);
            throw new EmailSendingException("Failed to send verification email", e);
        }
    }

    @Transactional
    public ResponseEntity<Void> verifyCode(VerificationRequestDto request) {
        try {
            User user = findUserByEmail(request.getEmail());
            validateVerificationCode(user, request.getCode());
            markUserAsVerified(user);
            return ResponseEntity.status(HttpStatus.OK).build();
        } catch (IllegalArgumentException e) {
            log.warn("Verification failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (Exception e) {
            log.error("Error during verification", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Transactional
    public ResponseEntity<Void> resendVerificationCode(ResendVerificationRequestDto request) {
        try {
            generateAndSendVerificationCode(request.getEmail());
            return ResponseEntity.status(HttpStatus.OK).build();
        } catch (Exception e) {
            log.error("Error resending verification code", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    private User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }

    private String generateVerificationCode() {
        int code = RANDOM.nextInt(900000) + 100000;
        return String.valueOf(code);
    }

    private void saveVerificationCode(User user, String code) {
        user.setVerificationCode(code);
        user.setVerificationCodeExpiry(calculateExpiry());
        userRepository.save(user);
    }

    private LocalDateTime calculateExpiry() {
        return LocalDateTime.now().plusSeconds(codeExpirationMs / 1000);
    }

    private void validateVerificationCode(User user, String code) {
        validateUserNotVerified(user);
        validateCodeExists(user);
        validateCodeNotExpired(user);
        validateCodeMatches(user, code);
    }

    private void validateUserNotVerified(User user) {
        if (Boolean.TRUE.equals(user.getIsVerified())) {
            throw new IllegalArgumentException("User already verified");
        }
    }

    private void validateCodeExists(User user) {
        if (user.getVerificationCode() == null) {
            throw new IllegalArgumentException("No verification code found");
        }
    }

    private void validateCodeNotExpired(User user) {
        if (user.getVerificationCodeExpiry().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Verification code expired");
        }
    }

    private void validateCodeMatches(User user, String code) {
        if (!user.getVerificationCode().equals(code)) {
            throw new IllegalArgumentException("Invalid verification code");
        }
    }

    private void markUserAsVerified(User user) {
        user.setIsVerified(true);
        user.setVerificationCode(null);
        user.setVerificationCodeExpiry(null);
        userRepository.save(user);
        log.info("User verified successfully: {}", user.getEmail());
    }

}
