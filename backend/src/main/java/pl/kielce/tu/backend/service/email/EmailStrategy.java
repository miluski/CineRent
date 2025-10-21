package pl.kielce.tu.backend.service.email;

public interface EmailStrategy {

    void sendEmail(String to, String subject, String body);

}
