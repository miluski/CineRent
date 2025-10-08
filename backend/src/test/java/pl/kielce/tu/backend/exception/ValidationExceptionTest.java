package pl.kielce.tu.backend.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.jupiter.api.Test;

class ValidationExceptionTest {

    @Test
    void noArgConstructor_hasNullMessageAndCause() {
        ValidationException ex = new ValidationException();
        assertNull(ex.getMessage());
        assertNull(ex.getCause());
    }

    @Test
    void messageConstructor_setsMessageAndLeavesCauseNull() {
        ValidationException ex = new ValidationException("validation failed");
        assertEquals("validation failed", ex.getMessage());
        assertNull(ex.getCause());
    }

    @Test
    void messageAndCauseConstructor_setsBoth() {
        Throwable cause = new RuntimeException("root cause");
        ValidationException ex = new ValidationException("validation failed", cause);
        assertEquals("validation failed", ex.getMessage());
        assertSame(cause, ex.getCause());
    }

    @Test
    void causeConstructor_setsCauseAndMessageFromCauseToString() {
        Throwable cause = new IllegalArgumentException("bad input");
        ValidationException ex = new ValidationException(cause);
        assertSame(cause, ex.getCause());
        assertEquals(cause.toString(), ex.getMessage());
    }
}


