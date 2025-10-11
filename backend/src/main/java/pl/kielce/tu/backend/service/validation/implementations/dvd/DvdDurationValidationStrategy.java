package pl.kielce.tu.backend.service.validation.implementations.dvd;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import pl.kielce.tu.backend.exception.ValidationException;
import pl.kielce.tu.backend.model.constant.ValidationStrategyType;
import pl.kielce.tu.backend.service.validation.FieldValidationStrategy;
import pl.kielce.tu.backend.util.UserContextLogger;

@Component
@RequiredArgsConstructor
public class DvdDurationValidationStrategy implements FieldValidationStrategy<Integer> {

    private final UserContextLogger userContextLogger;

    @Override
    public void validate(Integer durationMinutes) throws ValidationException {
        String durationInfo = durationMinutes != null ? durationMinutes + " min" : "null";
        userContextLogger.logValidationOperation("DVD_DURATION", "STARTED", durationInfo);

        validateNotNull(durationMinutes);
        validatePositive(durationMinutes);

        userContextLogger.logValidationOperation("DVD_DURATION", "SUCCESS", durationInfo);
    }

    @Override
    public ValidationStrategyType getStrategyType() {
        return ValidationStrategyType.DVD_DURATION;
    }

    private void validateNotNull(Integer durationMinutes) throws ValidationException {
        if (durationMinutes == null) {
            throw new ValidationException("Duration is required");
        }
    }

    private void validatePositive(Integer durationMinutes) throws ValidationException {
        if (durationMinutes <= 0) {
            throw new ValidationException("Duration must be greater than 0 minutes");
        }
    }

}
