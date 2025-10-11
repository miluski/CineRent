package pl.kielce.tu.backend.service.validation.implementations.dvd;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import pl.kielce.tu.backend.exception.ValidationException;
import pl.kielce.tu.backend.model.constant.ValidationStrategyType;
import pl.kielce.tu.backend.util.UserContextLogger;

@ExtendWith(MockitoExtension.class)
class DvdDurationValidationStrategyTest {

    @Mock
    private UserContextLogger userContextLogger;

    @Test
    void validate_withPositiveDuration_logsStartedAndSuccess_andDoesNotThrow() {
        DvdDurationValidationStrategy strategy = new DvdDurationValidationStrategy(userContextLogger);

        assertDoesNotThrow(() -> strategy.validate(120));

        InOrder inOrder = inOrder(userContextLogger);
        inOrder.verify(userContextLogger).logValidationOperation("DVD_DURATION", "STARTED", "120 min");
        inOrder.verify(userContextLogger).logValidationOperation("DVD_DURATION", "SUCCESS", "120 min");
        verifyNoMoreInteractions(userContextLogger);
    }

    @Test
    void validate_withNull_throwsValidationException_andLogsStartedOnly() {
        DvdDurationValidationStrategy strategy = new DvdDurationValidationStrategy(userContextLogger);

        ValidationException ex = assertThrows(ValidationException.class, () -> strategy.validate(null));
        assertEquals("Duration is required", ex.getMessage());

        verify(userContextLogger).logValidationOperation("DVD_DURATION", "STARTED", "null");
        verifyNoMoreInteractions(userContextLogger);
    }

    @Test
    void validate_withZero_throwsValidationException_andLogsStartedOnly() {
        DvdDurationValidationStrategy strategy = new DvdDurationValidationStrategy(userContextLogger);

        ValidationException ex = assertThrows(ValidationException.class, () -> strategy.validate(0));
        assertEquals("Duration must be greater than 0 minutes", ex.getMessage());

        verify(userContextLogger).logValidationOperation("DVD_DURATION", "STARTED", "0 min");
        verifyNoMoreInteractions(userContextLogger);
    }

    @Test
    void validate_withNegative_throwsValidationException_andLogsStartedOnly() {
        DvdDurationValidationStrategy strategy = new DvdDurationValidationStrategy(userContextLogger);

        ValidationException ex = assertThrows(ValidationException.class, () -> strategy.validate(-5));
        assertEquals("Duration must be greater than 0 minutes", ex.getMessage());

        verify(userContextLogger).logValidationOperation("DVD_DURATION", "STARTED", "-5 min");
        verifyNoMoreInteractions(userContextLogger);
    }

    @Test
    void getStrategyType_returnsDvdDuration() {
        DvdDurationValidationStrategy strategy = new DvdDurationValidationStrategy(userContextLogger);

        assertEquals(ValidationStrategyType.DVD_DURATION, strategy.getStrategyType());
    }
}
