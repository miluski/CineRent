package pl.kielce.tu.backend.service.validation.implementations.reservation;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import pl.kielce.tu.backend.exception.ValidationException;
import pl.kielce.tu.backend.model.constant.ReservationStatus;
import pl.kielce.tu.backend.model.constant.ValidationStrategyType;
import pl.kielce.tu.backend.model.dto.ReservationCancellationRequest;
import pl.kielce.tu.backend.model.entity.Reservation;
import pl.kielce.tu.backend.service.validation.FieldValidationStrategy;
import pl.kielce.tu.backend.util.UserContextLogger;

@Component
@RequiredArgsConstructor
public class ReservationCancellationValidationStrategy
        implements FieldValidationStrategy<ReservationCancellationRequest> {

    private final UserContextLogger userContextLogger;

    @Override
    public void validate(ReservationCancellationRequest request) throws ValidationException {
        String requestInfo = buildRequestInfo(request);
        userContextLogger.logValidationOperation("RESERVATION_CANCELLATION", "STARTED", requestInfo);

        validateRequestNotNull(request);
        validateReservationStatus(request.getReservation());
        validateOwnership(request.getReservation(), request.getUserId());

        userContextLogger.logValidationOperation("RESERVATION_CANCELLATION", "SUCCESS", requestInfo);
    }

    @Override
    public ValidationStrategyType getStrategyType() {
        return ValidationStrategyType.RESERVATION_CANCELLATION;
    }

    private void validateRequestNotNull(ReservationCancellationRequest request) throws ValidationException {
        if (request == null) {
            throw new ValidationException("Cancellation request is required");
        }
        if (request.getReservation() == null) {
            throw new ValidationException("Reservation is required for cancellation");
        }
        if (request.getUserId() == null) {
            throw new ValidationException("User ID is required for cancellation");
        }
    }

    private void validateReservationStatus(Reservation reservation) throws ValidationException {
        if (reservation.getStatus() != ReservationStatus.PENDING) {
            throw new ValidationException("Only pending reservations can be cancelled");
        }
    }

    private void validateOwnership(Reservation reservation, Long userId) throws ValidationException {
        if (!reservation.getUser().getId().equals(userId)) {
            throw new ValidationException("User can only cancel their own reservations");
        }
    }

    private String buildRequestInfo(ReservationCancellationRequest request) {
        if (request == null || request.getReservation() == null) {
            return "null request";
        }
        return String.format("Reservation ID: %d, User ID: %d, Status: %s",
                request.getReservation().getId(),
                request.getUserId(),
                request.getReservation().getStatus());
    }

}
