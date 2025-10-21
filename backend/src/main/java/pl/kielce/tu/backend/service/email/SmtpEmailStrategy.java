package pl.kielce.tu.backend.service.email;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class SmtpEmailStrategy implements EmailStrategy {

    private final JavaMailSender mailSender;

    @Value("${mail.from}")
    private String fromEmail;

    @Override
    public void sendEmail(String to, String subject, String body) {
        try {
            MimeMessage message = createHtmlMailMessage(to, subject, body);
            mailSender.send(message);
            log.info("Email sent successfully to: {}", to);
        } catch (Exception e) {
            log.error("Failed to send email to: {}", to, e);
            throw new RuntimeException("Failed to send email", e);
        }
    }

    private MimeMessage createHtmlMailMessage(String to, String subject, String body) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body, true);
            return message;
        } catch (Exception e) {
            throw new RuntimeException("Failed to create email message", e);
        }
    }

}
