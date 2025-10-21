package pl.kielce.tu.backend.service.email;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock
    private EmailStrategy emailStrategy;

    @Mock
    private ResourceLoader resourceLoader;

    @Mock
    private Resource resource;

    @InjectMocks
    private EmailService emailService;

    private String testEmail;
    private String testCode;
    private String templateContent;

    @BeforeEach
    void setUp() throws Exception {
        testEmail = "test@example.com";
        testCode = "123456";
        templateContent = "<!DOCTYPE html><html><body>Kod: {code}, Wygasa za: {expirationMinutes} minut</body></html>";

        ReflectionTestUtils.setField(emailService, "verificationSubject", "CineRent - Weryfikacja adresu email");
        ReflectionTestUtils.setField(emailService, "verificationTemplatePath",
                "classpath:templates/email-verification.html");
        ReflectionTestUtils.setField(emailService, "codeExpirationMs", 900000L);

        when(resourceLoader.getResource(any())).thenReturn(resource);
        when(resource.getInputStream())
                .thenReturn(new ByteArrayInputStream(templateContent.getBytes(StandardCharsets.UTF_8)));
    }

    @Test
    void sendVerificationEmail_Success() {
        doNothing().when(emailStrategy).sendEmail(any(), any(), any());

        emailService.sendVerificationEmail(testEmail, testCode);

        verify(emailStrategy).sendEmail(eq(testEmail), eq("CineRent - Weryfikacja adresu email"), contains(testCode));
    }

    @Test
    void sendVerificationEmail_EmailStrategyThrowsException() {
        doThrow(new RuntimeException("Email send failed"))
                .when(emailStrategy).sendEmail(any(), any(), any());

        assertThrows(RuntimeException.class, () -> {
            emailService.sendVerificationEmail(testEmail, testCode);
        });

        verify(emailStrategy).sendEmail(eq(testEmail), eq("CineRent - Weryfikacja adresu email"), contains(testCode));
    }

}
