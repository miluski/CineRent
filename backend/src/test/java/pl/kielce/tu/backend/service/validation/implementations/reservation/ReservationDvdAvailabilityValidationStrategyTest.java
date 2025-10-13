package pl.kielce.tu.backend.service.validation.implementations.reservation;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import pl.kielce.tu.backend.exception.ValidationException;
import pl.kielce.tu.backend.model.constant.ValidationStrategyType;
import pl.kielce.tu.backend.model.entity.Dvd;
import pl.kielce.tu.backend.util.UserContextLogger;

@ExtendWith(MockitoExtension.class)
class ReservationDvdAvailabilityValidationStrategyTest {

    @Mock
    private UserContextLogger userContextLogger;

    private ReservationDvdAvailabilityValidationStrategy strategy;

    @BeforeEach
    void setUp() {
        strategy = new ReservationDvdAvailabilityValidationStrategy(userContextLogger);
    }

    @Test
    void validate_availableDvd_succeeds() throws ValidationException {
        Dvd dvd = new Dvd();
        dvd.setId(1L);
        dvd.setAvalaible(true);
        assertDoesNotThrow(() -> strategy.validate(dvd));

        verify(userContextLogger).logValidationOperation("RESERVATION_DVD_AVAILABILITY", "STARTED",
                "DVD ID: 1, Available: true");
        verify(userContextLogger).logValidationOperation("RESERVATION_DVD_AVAILABILITY", "SUCCESS",
                "DVD ID: 1, Available: true");
    }

    @Test
    void validate_unavailableDvd_throwsException() {
        Dvd dvd = new Dvd();
        dvd.setId(1L);
        dvd.setAvalaible(false);

        ValidationException exception = assertThrows(ValidationException.class, () -> strategy.validate(dvd));
        assertEquals("DVD is not available for reservation", exception.getMessage());

        verify(userContextLogger).logValidationOperation(eq("RESERVATION_DVD_AVAILABILITY"), eq("STARTED"),
                eq("DVD ID: 1, Available: false"));
        verify(userContextLogger, never()).logValidationOperation(eq("RESERVATION_DVD_AVAILABILITY"), eq("SUCCESS"),
                anyString());
    }

    @Test
    void validate_nullAvailability_throwsException() {
        Dvd dvd = new Dvd();
        dvd.setId(1L);
        dvd.setAvalaible(null);
        ValidationException exception = assertThrows(ValidationException.class, () -> strategy.validate(dvd));
        assertEquals("DVD is not available for reservation", exception.getMessage());
    }

    @Test
    void validate_nullDvd_throwsException() {
        ValidationException exception = assertThrows(ValidationException.class, () -> strategy.validate(null));
        assertEquals("DVD is required for reservation", exception.getMessage());

        verify(userContextLogger).logValidationOperation(eq("RESERVATION_DVD_AVAILABILITY"), eq("STARTED"), eq("null"));
        verify(userContextLogger, never()).logValidationOperation(eq("RESERVATION_DVD_AVAILABILITY"), eq("SUCCESS"),
                anyString());
    }

    @Test
    void getStrategyType_returnsCorrectType() {
        assertEquals(ValidationStrategyType.RESERVATION_DVD_AVAILABILITY, strategy.getStrategyType());
    }
}
