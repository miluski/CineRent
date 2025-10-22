package pl.kielce.tu.backend.exception;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class EmailSendingExceptionTest {

    @Test
    void constructorWithMessage_ShouldCreateException() {

        String message = "Failed to send email";

        EmailSendingException exception = new EmailSendingException(message);

        assertThat(exception.getMessage()).isEqualTo(message);
        assertThat(exception.getCause()).isNull();
    }

    @Test
    void constructorWithMessageAndCause_ShouldCreateException() {

        String message = "Failed to send email";
        Throwable cause = new RuntimeException("SMTP connection failed");

        EmailSendingException exception = new EmailSendingException(message, cause);

        assertThat(exception.getMessage()).isEqualTo(message);
        assertThat(exception.getCause()).isEqualTo(cause);
        assertThat(exception.getCause().getMessage()).isEqualTo("SMTP connection failed");
    }

    @Test
    void exceptionShouldBeRuntimeException() {

        EmailSendingException exception = new EmailSendingException("Test");

        assertThat(exception).isInstanceOf(RuntimeException.class);
    }

    @Test
    void constructorWithNullMessage_ShouldCreateException() {

        EmailSendingException exception = new EmailSendingException(null);

        assertThat(exception.getMessage()).isNull();
        assertThat(exception.getCause()).isNull();
    }

    @Test
    void constructorWithNullCause_ShouldCreateException() {

        String message = "Failed to send email";

        EmailSendingException exception = new EmailSendingException(message, null);

        assertThat(exception.getMessage()).isEqualTo(message);
        assertThat(exception.getCause()).isNull();
    }

}
