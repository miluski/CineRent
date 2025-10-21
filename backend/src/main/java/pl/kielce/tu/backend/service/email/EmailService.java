package pl.kielce.tu.backend.service.email;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final EmailStrategy emailStrategy;
    private final ResourceLoader resourceLoader;

    @Value("${email.verification.subject}")
    private String verificationSubject;

    @Value("${email.verification.template}")
    private String verificationTemplatePath;

    @Value("${verification.code.expiration}")
    private long codeExpirationMs;

    public void sendVerificationEmail(String to, String code) {
        String body = buildVerificationBody(code);
        emailStrategy.sendEmail(to, verificationSubject, body);
    }

    private String buildVerificationBody(String code) {
        try {
            String template = loadTemplate(verificationTemplatePath);
            long expirationMinutes = codeExpirationMs / 60000;
            return template
                    .replace("{code}", code)
                    .replace("{expirationMinutes}", String.valueOf(expirationMinutes));
        } catch (IOException e) {
            throw new RuntimeException("Failed to load email template", e);
        }
    }

    private String loadTemplate(String templatePath) throws IOException {
        Resource resource = resourceLoader.getResource(templatePath);
        return StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);
    }

}
