package pl.kielce.tu.backend.service.validation.implementations.dvd;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import pl.kielce.tu.backend.exception.ValidationException;
import pl.kielce.tu.backend.model.constant.ValidationStrategyType;
import pl.kielce.tu.backend.util.UserContextLogger;

@ExtendWith(MockitoExtension.class)
class DvdDescriptionValidationStrategyTest {

    @Mock
    private UserContextLogger userContextLogger;

    @InjectMocks
    private DvdDescriptionValidationStrategy strategy;

    @Test
    void validate_validDescription_logsStartedAndSuccess() throws ValidationException {
        String desc = "A".repeat(100);
        String expectedInfo = "100 chars";

        strategy.validate(desc);

        InOrder inOrder = inOrder(userContextLogger);
        inOrder.verify(userContextLogger).logValidationOperation("DVD_DESCRIPTION", "STARTED", expectedInfo);
        inOrder.verify(userContextLogger).logValidationOperation("DVD_DESCRIPTION", "SUCCESS", expectedInfo);
        verifyNoMoreInteractions(userContextLogger);
    }

    @Test
    void validate_nullDescription_throwsValidationException_andLogsStartedWithNull() {
        ValidationException ex = assertThrows(ValidationException.class, () -> strategy.validate(null));
        assertEquals("Description is required", ex.getMessage());

        verify(userContextLogger, times(1)).logValidationOperation("DVD_DESCRIPTION", "STARTED", "null");
        verifyNoMoreInteractions(userContextLogger);
    }

    @Test
    void validate_blankDescription_throwsValidationException_andLogsStarted() {
        String desc = "   ";
        ValidationException ex = assertThrows(ValidationException.class, () -> strategy.validate(desc));
        assertEquals("Description cannot be blank", ex.getMessage());

        verify(userContextLogger, times(1)).logValidationOperation("DVD_DESCRIPTION", "STARTED", "3 chars");
        verifyNoMoreInteractions(userContextLogger);
    }

    @Test
    void validate_tooShortDescription_throwsValidationException_andLogsStarted() {
        String desc = "short description";
        String info = desc.length() + " chars";
        ValidationException ex = assertThrows(ValidationException.class, () -> strategy.validate(desc));
        assertEquals("Description must be at least 25 characters long", ex.getMessage());

        verify(userContextLogger, times(1)).logValidationOperation("DVD_DESCRIPTION", "STARTED", info);
        verifyNoMoreInteractions(userContextLogger);
    }

    @Test
    void validate_tooLongDescription_throwsValidationException_andLogsStarted() {
        String desc = "A".repeat(501);
        String info = "501 chars";
        ValidationException ex = assertThrows(ValidationException.class, () -> strategy.validate(desc));
        assertEquals("Description cannot exceed 500 characters", ex.getMessage());

        verify(userContextLogger, times(1)).logValidationOperation("DVD_DESCRIPTION", "STARTED", info);
        verifyNoMoreInteractions(userContextLogger);
    }

    @Test
    void getStrategyType_returnsDvdDescription() {
        ValidationStrategyType type = strategy.getStrategyType();
        assertEquals(ValidationStrategyType.DVD_DESCRIPTION, type);
    }
}
