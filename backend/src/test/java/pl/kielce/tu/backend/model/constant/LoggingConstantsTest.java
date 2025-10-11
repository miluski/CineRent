package pl.kielce.tu.backend.model.constant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

public class LoggingConstantsTest {

    @Test
    void userPrefixValueIsCorrect() {
        assertEquals("user:", LoggingConstants.USER_PREFIX.getValue());
    }

    @Test
    void anonymousUserValueIsCorrect() {
        assertEquals("anonymous", LoggingConstants.ANONYMOUS_USER.getValue());
    }

    @Test
    void logSeparatorValueIsCorrect() {
        assertEquals(" | ", LoggingConstants.LOG_SEPARATOR.getValue());
    }

    @Test
    void noValueIsNull() {
        for (LoggingConstants constant : LoggingConstants.values()) {
            assertNotNull(constant.getValue());
        }
    }

    @Test
    void expectedNumberOfConstants() {
        assertEquals(3, LoggingConstants.values().length);
    }
}
