package pl.kielce.tu.backend.service.validation.implementations.dvd;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

import pl.kielce.tu.backend.exception.ValidationException;
import pl.kielce.tu.backend.model.constant.ValidationStrategyType;
import pl.kielce.tu.backend.util.UserContextLogger;

class DvdPriceValidationStrategyTest {

    private final UserContextLogger logger = mock(UserContextLogger.class);
    private final DvdPriceValidationStrategy strategy = new DvdPriceValidationStrategy(logger);

    @Test
    void shouldValidate_WhenPriceIsWithinRange() {
        Float price = 5.0f;
        assertDoesNotThrow(() -> strategy.validate(price));

        String expectedInfo = price + " per day";
        InOrder inOrder = inOrder(logger);
        inOrder.verify(logger).logValidationOperation("DVD_PRICE", "STARTED", expectedInfo);
        inOrder.verify(logger).logValidationOperation("DVD_PRICE", "SUCCESS", expectedInfo);
        verifyNoMoreInteractions(logger);
    }

    @Test
    void shouldThrowValidationException_WhenPriceIsNull() {
        ValidationException ex = assertThrows(ValidationException.class, () -> strategy.validate(null));
        assertEquals("Rental price is required", ex.getMessage());

        verify(logger).logValidationOperation("DVD_PRICE", "STARTED", "null");
        verify(logger, never()).logValidationOperation("DVD_PRICE", "SUCCESS", "null");
        verifyNoMoreInteractions(logger);
    }

    @Test
    void shouldThrowValidationException_WhenPriceIsZero() {
        Float price = 0.0f;
        ValidationException ex = assertThrows(ValidationException.class, () -> strategy.validate(price));
        assertEquals("Rental price must be greater than 0", ex.getMessage());

        verify(logger).logValidationOperation("DVD_PRICE", "STARTED", price + " per day");
        verify(logger, never()).logValidationOperation("DVD_PRICE", "SUCCESS", price + " per day");
        verifyNoMoreInteractions(logger);
    }

    @Test
    void shouldThrowValidationException_WhenPriceIsNegative() {
        Float price = -1.0f;
        ValidationException ex = assertThrows(ValidationException.class, () -> strategy.validate(price));
        assertEquals("Rental price must be greater than 0", ex.getMessage());

        verify(logger).logValidationOperation("DVD_PRICE", "STARTED", price + " per day");
        verify(logger, never()).logValidationOperation("DVD_PRICE", "SUCCESS", price + " per day");
        verifyNoMoreInteractions(logger);
    }

    @Test
    void shouldThrowValidationException_WhenPriceIsTooHigh() {
        Float price = 50.0f;
        ValidationException ex = assertThrows(ValidationException.class, () -> strategy.validate(price));
        assertEquals("Rental price must be less than 50", ex.getMessage());

        verify(logger).logValidationOperation("DVD_PRICE", "STARTED", price + " per day");
        verify(logger, never()).logValidationOperation("DVD_PRICE", "SUCCESS", price + " per day");
        verifyNoMoreInteractions(logger);
    }

    @Test
    void getStrategyTypeReturnsDvdPrice() {
        assertEquals(ValidationStrategyType.DVD_PRICE, strategy.getStrategyType());
    }
}
