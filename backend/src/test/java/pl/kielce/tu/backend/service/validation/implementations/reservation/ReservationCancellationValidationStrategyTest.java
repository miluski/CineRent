package pl.kielce.tu.backend.service.validation.implementations.reservation;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import pl.kielce.tu.backend.exception.ValidationException;
import pl.kielce.tu.backend.model.constant.ReservationStatus;
import pl.kielce.tu.backend.model.constant.ValidationStrategyType;
import pl.kielce.tu.backend.model.dto.ReservationCancellationRequest;
import pl.kielce.tu.backend.model.entity.Reservation;
import pl.kielce.tu.backend.model.entity.User;
import pl.kielce.tu.backend.util.UserContextLogger;

@ExtendWith(MockitoExtension.class)
class ReservationCancellationValidationStrategyTest {

    @Mock
    private UserContextLogger userContextLogger;

    @InjectMocks
    private ReservationCancellationValidationStrategy strategy;

    @Test
    void validate_success_withValidPendingReservation() {
        User user = mock(User.class);
        when(user.getId()).thenReturn(42L);

        Reservation reservation = mock(Reservation.class);
        when(reservation.getId()).thenReturn(1L);
        when(reservation.getStatus()).thenReturn(ReservationStatus.PENDING);
        when(reservation.getUser()).thenReturn(user);

        ReservationCancellationRequest request = ReservationCancellationRequest.builder()
                .reservation(reservation)
                .userId(42L)
                .build();

        assertDoesNotThrow(() -> strategy.validate(request));

        verify(userContextLogger).logValidationOperation(eq("RESERVATION_CANCELLATION"), eq("STARTED"), any());
        verify(userContextLogger).logValidationOperation(eq("RESERVATION_CANCELLATION"), eq("SUCCESS"), any());
    }

    @Test
    void validate_throwsException_whenRequestIsNull() {
        ValidationException exception = assertThrows(ValidationException.class,
                () -> strategy.validate(null));

        assertEquals("Cancellation request is required", exception.getMessage());
    }

    @Test
    void validate_throwsException_whenReservationIsNull() {
        ReservationCancellationRequest request = ReservationCancellationRequest.builder()
                .reservation(null)
                .userId(42L)
                .build();

        ValidationException exception = assertThrows(ValidationException.class,
                () -> strategy.validate(request));

        assertEquals("Reservation is required for cancellation", exception.getMessage());
    }

    @Test
    void validate_throwsException_whenUserIdIsNull() {
        Reservation reservation = mock(Reservation.class);
        ReservationCancellationRequest request = ReservationCancellationRequest.builder()
                .reservation(reservation)
                .userId(null)
                .build();

        ValidationException exception = assertThrows(ValidationException.class,
                () -> strategy.validate(request));

        assertEquals("User ID is required for cancellation", exception.getMessage());
    }

    @Test
    void validate_throwsException_whenReservationStatusIsNotPending() {
        User user = mock(User.class);
        lenient().when(user.getId()).thenReturn(42L);

        Reservation reservation = mock(Reservation.class);
        lenient().when(reservation.getStatus()).thenReturn(ReservationStatus.ACCEPTED);
        lenient().when(reservation.getUser()).thenReturn(user);

        ReservationCancellationRequest request = ReservationCancellationRequest.builder()
                .reservation(reservation)
                .userId(42L)
                .build();

        ValidationException exception = assertThrows(ValidationException.class,
                () -> strategy.validate(request));

        assertEquals("Only pending reservations can be cancelled", exception.getMessage());
    }

    @Test
    void validate_throwsException_whenUserIsNotOwner() {
        User user = mock(User.class);
        when(user.getId()).thenReturn(99L);

        Reservation reservation = mock(Reservation.class);
        when(reservation.getStatus()).thenReturn(ReservationStatus.PENDING);
        when(reservation.getUser()).thenReturn(user);

        ReservationCancellationRequest request = ReservationCancellationRequest.builder()
                .reservation(reservation)
                .userId(42L)
                .build();

        ValidationException exception = assertThrows(ValidationException.class,
                () -> strategy.validate(request));

        assertEquals("User can only cancel their own reservations", exception.getMessage());
    }

    @Test
    void getStrategyType_returnsCorrectType() {
        assertEquals(ValidationStrategyType.RESERVATION_CANCELLATION, strategy.getStrategyType());
    }

}
