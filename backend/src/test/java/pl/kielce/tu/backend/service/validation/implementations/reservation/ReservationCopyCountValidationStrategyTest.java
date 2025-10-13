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
import pl.kielce.tu.backend.model.dto.ReservationDto;
import pl.kielce.tu.backend.model.dto.ReservationValidationRequest;
import pl.kielce.tu.backend.model.entity.Dvd;
import pl.kielce.tu.backend.util.UserContextLogger;

@ExtendWith(MockitoExtension.class)
class ReservationCopyCountValidationStrategyTest {

    @Mock
    private UserContextLogger userContextLogger;

    private ReservationCopyCountValidationStrategy strategy;

    @BeforeEach
    void setUp() {
        strategy = new ReservationCopyCountValidationStrategy(userContextLogger);
    }

    @Test
    void validate_sufficientCopies_succeeds() throws ValidationException {
        ReservationDto reservation = new ReservationDto();
        reservation.setCount(2);
        Dvd dvd = new Dvd();
        dvd.setCopiesAvalaible(5);
        ReservationValidationRequest request = ReservationValidationRequest.builder()
                .reservation(reservation)
                .dvd(dvd)
                .build();

        assertDoesNotThrow(() -> strategy.validate(request));

        verify(userContextLogger).logValidationOperation("RESERVATION_COPY_COUNT", "STARTED",
                "Requested: 2, Available: 5");
        verify(userContextLogger).logValidationOperation("RESERVATION_COPY_COUNT", "SUCCESS",
                "Requested: 2, Available: 5");
    }

    @Test
    void validate_exactCopies_succeeds() throws ValidationException {
        ReservationDto reservation = new ReservationDto();
        reservation.setCount(3);
        Dvd dvd = new Dvd();
        dvd.setCopiesAvalaible(3);
        ReservationValidationRequest request = ReservationValidationRequest.builder()
                .reservation(reservation)
                .dvd(dvd)
                .build();

        assertDoesNotThrow(() -> strategy.validate(request));
    }

    @Test
    void validate_insufficientCopies_throwsException() {
        ReservationDto reservation = new ReservationDto();
        reservation.setCount(5);
        Dvd dvd = new Dvd();
        dvd.setCopiesAvalaible(2);
        ReservationValidationRequest request = ReservationValidationRequest.builder()
                .reservation(reservation)
                .dvd(dvd)
                .build();

        ValidationException exception = assertThrows(ValidationException.class, () -> strategy.validate(request));
        assertEquals("Insufficient copies available. Requested: 5, Available: 2", exception.getMessage());

        verify(userContextLogger).logValidationOperation(eq("RESERVATION_COPY_COUNT"), eq("STARTED"),
                eq("Requested: 5, Available: 2"));
        verify(userContextLogger, never()).logValidationOperation(eq("RESERVATION_COPY_COUNT"), eq("SUCCESS"),
                anyString());
    }

    @Test
    void validate_nullRequest_throwsException() {
        ValidationException exception = assertThrows(ValidationException.class, () -> strategy.validate(null));
        assertEquals("Validation request is required", exception.getMessage());
    }

    @Test
    void validate_nullReservation_throwsException() {
        Dvd dvd = new Dvd();
        dvd.setCopiesAvalaible(5);
        ReservationValidationRequest request = ReservationValidationRequest.builder()
                .reservation(null)
                .dvd(dvd)
                .build();

        ValidationException exception = assertThrows(ValidationException.class, () -> strategy.validate(request));
        assertEquals("Reservation is required", exception.getMessage());
    }

    @Test
    void validate_nullDvd_throwsException() {
        ReservationDto reservation = new ReservationDto();
        reservation.setCount(3);
        ReservationValidationRequest request = ReservationValidationRequest.builder()
                .reservation(reservation)
                .dvd(null)
                .build();

        ValidationException exception = assertThrows(ValidationException.class, () -> strategy.validate(request));
        assertEquals("DVD is required", exception.getMessage());
    }

    @Test
    void validate_nullRequestedCount_throwsException() {
        ReservationDto reservation = new ReservationDto();
        reservation.setCount(null);
        Dvd dvd = new Dvd();
        dvd.setCopiesAvalaible(5);
        ReservationValidationRequest request = ReservationValidationRequest.builder()
                .reservation(reservation)
                .dvd(dvd)
                .build();

        ValidationException exception = assertThrows(ValidationException.class, () -> strategy.validate(request));
        assertEquals("Requested copy count is required", exception.getMessage());
    }

    @Test
    void validate_nullAvailableCopies_throwsException() {
        ReservationDto reservation = new ReservationDto();
        reservation.setCount(3);
        Dvd dvd = new Dvd();
        dvd.setCopiesAvalaible(null);
        ReservationValidationRequest request = ReservationValidationRequest.builder()
                .reservation(reservation)
                .dvd(dvd)
                .build();

        ValidationException exception = assertThrows(ValidationException.class, () -> strategy.validate(request));
        assertEquals("Available copy count is required", exception.getMessage());
    }

    @Test
    void getStrategyType_returnsCorrectType() {
        assertEquals(ValidationStrategyType.RESERVATION_COPY_COUNT, strategy.getStrategyType());
    }

}
