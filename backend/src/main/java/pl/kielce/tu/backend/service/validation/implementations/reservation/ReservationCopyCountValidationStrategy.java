package pl.kielce.tu.backend.service.validation.implementations.reservation;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import pl.kielce.tu.backend.exception.ValidationException;
import pl.kielce.tu.backend.model.constant.ValidationStrategyType;
import pl.kielce.tu.backend.model.dto.ReservationValidationRequest;
import pl.kielce.tu.backend.service.validation.FieldValidationStrategy;
import pl.kielce.tu.backend.util.UserContextLogger;

@Component
@RequiredArgsConstructor
public class ReservationCopyCountValidationStrategy
        implements FieldValidationStrategy<ReservationValidationRequest> {

    private final UserContextLogger userContextLogger;

    @Override
    public void validate(ReservationValidationRequest request) throws ValidationException {
        String requestInfo = request != null && request.getReservation() != null && request.getDvd() != null
                ? "Requested: " + request.getReservation().getCount() + ", Available: "
                        + request.getDvd().getCopiesAvalaible()
                : "null";
        userContextLogger.logValidationOperation("RESERVATION_COPY_COUNT", "STARTED", requestInfo);

        validateNotNull(request);
        validateSufficientCopies(request);

        userContextLogger.logValidationOperation("RESERVATION_COPY_COUNT", "SUCCESS", requestInfo);
    }

    @Override
    public ValidationStrategyType getStrategyType() {
        return ValidationStrategyType.RESERVATION_COPY_COUNT;
    }

    private void validateNotNull(ReservationValidationRequest request) throws ValidationException {
        if (request == null) {
            throw new ValidationException("Validation request is required");
        }
        if (request.getReservation() == null) {
            throw new ValidationException("Reservation is required");
        }
        if (request.getDvd() == null) {
            throw new ValidationException("DVD is required");
        }
        if (request.getReservation().getCount() == null) {
            throw new ValidationException("Requested copy count is required");
        }
        if (request.getDvd().getCopiesAvalaible() == null) {
            throw new ValidationException("Available copy count is required");
        }
    }

    private void validateSufficientCopies(ReservationValidationRequest request) throws ValidationException {
        Integer requestedCount = request.getReservation().getCount();
        Integer availableCopies = request.getDvd().getCopiesAvalaible();

        if (requestedCount > availableCopies) {
            throw new ValidationException(
                    "Insufficient copies available. Requested: " + requestedCount +
                            ", Available: " + availableCopies);
        }
    }

}
