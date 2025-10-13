package pl.kielce.tu.backend.service.validation.implementations.reservation;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import pl.kielce.tu.backend.exception.ValidationException;
import pl.kielce.tu.backend.model.constant.ValidationStrategyType;
import pl.kielce.tu.backend.model.entity.Dvd;
import pl.kielce.tu.backend.service.validation.FieldValidationStrategy;
import pl.kielce.tu.backend.util.UserContextLogger;

@Component
@RequiredArgsConstructor
public class ReservationDvdAvailabilityValidationStrategy implements FieldValidationStrategy<Dvd> {

    private final UserContextLogger userContextLogger;

    @Override
    public void validate(Dvd dvd) throws ValidationException {
        String dvdInfo = dvd != null ? "DVD ID: " + dvd.getId() + ", Available: " + dvd.getAvalaible() : "null";
        userContextLogger.logValidationOperation("RESERVATION_DVD_AVAILABILITY", "STARTED", dvdInfo);

        validateNotNull(dvd);
        validateDvdAvailable(dvd);

        userContextLogger.logValidationOperation("RESERVATION_DVD_AVAILABILITY", "SUCCESS", dvdInfo);
    }

    @Override
    public ValidationStrategyType getStrategyType() {
        return ValidationStrategyType.RESERVATION_DVD_AVAILABILITY;
    }

    private void validateNotNull(Dvd dvd) throws ValidationException {
        if (dvd == null) {
            throw new ValidationException("DVD is required for reservation");
        }
    }

    private void validateDvdAvailable(Dvd dvd) throws ValidationException {
        if (!Boolean.TRUE.equals(dvd.getAvalaible())) {
            throw new ValidationException("DVD is not available for reservation");
        }
    }

}
