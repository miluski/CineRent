package pl.kielce.tu.backend.service.validation.implementations.dvd;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import pl.kielce.tu.backend.exception.ValidationException;
import pl.kielce.tu.backend.model.constant.ValidationStrategyType;
import pl.kielce.tu.backend.util.UserContextLogger;

@ExtendWith(MockitoExtension.class)
class DvdCopiesValidationStrategyTest {

    @Mock
    private UserContextLogger userContextLogger;

    @InjectMocks
    private DvdCopiesValidationStrategy strategy;

    @ParameterizedTest
    @ValueSource(ints = { 0, 50, 100 })
    void validate_validValues_doesNotThrow_andLogsStartedAndSuccess(int copies) {
        Integer copiesInteger = copies;
        assertDoesNotThrow(() -> strategy.validate(copiesInteger));

        String copiesInfo = copiesInteger + " copies";
        verify(userContextLogger).logValidationOperation("DVD_COPIES", "STARTED", copiesInfo);
        verify(userContextLogger).logValidationOperation("DVD_COPIES", "SUCCESS", copiesInfo);
    }

    @Test
    void validate_null_throwsValidationException_andLogsStartedOnly() {
        ValidationException ex = assertThrows(ValidationException.class, () -> strategy.validate(null));
        assertEquals("Number of available copies is required", ex.getMessage());

        verify(userContextLogger).logValidationOperation("DVD_COPIES", "STARTED", "null");
        verify(userContextLogger, never()).logValidationOperation("DVD_COPIES", "SUCCESS", "null");
    }

    @Test
    void validate_negative_throwsValidationException_andLogsStartedOnly() {
        Integer copies = -1;
        ValidationException ex = assertThrows(ValidationException.class, () -> strategy.validate(copies));
        assertEquals("Available copies cannot be negative", ex.getMessage());

        String copiesInfo = copies + " copies";
        verify(userContextLogger).logValidationOperation("DVD_COPIES", "STARTED", copiesInfo);
        verify(userContextLogger, never()).logValidationOperation("DVD_COPIES", "SUCCESS", copiesInfo);
    }

    @Test
    void validate_aboveMax_throwsValidationException_andLogsStartedOnly() {
        Integer copies = 101;
        ValidationException ex = assertThrows(ValidationException.class, () -> strategy.validate(copies));
        assertEquals("Available copies cannot exceed 100", ex.getMessage());

        String copiesInfo = copies + " copies";
        verify(userContextLogger).logValidationOperation("DVD_COPIES", "STARTED", copiesInfo);
        verify(userContextLogger, never()).logValidationOperation("DVD_COPIES", "SUCCESS", copiesInfo);
    }

    @Test
    void getStrategyType_returnsDvdCopies() {
        assertEquals(ValidationStrategyType.DVD_COPIES, strategy.getStrategyType());
    }
}
