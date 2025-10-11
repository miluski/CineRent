package pl.kielce.tu.backend.service.validation.implementations.dvd;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import pl.kielce.tu.backend.exception.ValidationException;
import pl.kielce.tu.backend.model.constant.ValidationStrategyType;
import pl.kielce.tu.backend.service.validation.FieldValidationStrategy;
import pl.kielce.tu.backend.util.UserContextLogger;

@Component
@RequiredArgsConstructor
public class DvdCopiesValidationStrategy implements FieldValidationStrategy<Integer> {

    private final UserContextLogger userContextLogger;

    @Override
    public void validate(Integer copiesAvailable) throws ValidationException {
        String copiesInfo = copiesAvailable != null ? copiesAvailable + " copies" : "null";
        userContextLogger.logValidationOperation("DVD_COPIES", "STARTED", copiesInfo);

        validateNotNull(copiesAvailable);
        validateRange(copiesAvailable);

        userContextLogger.logValidationOperation("DVD_COPIES", "SUCCESS", copiesInfo);
    }

    @Override
    public ValidationStrategyType getStrategyType() {
        return ValidationStrategyType.DVD_COPIES;
    }

    private void validateNotNull(Integer copiesAvailable) throws ValidationException {
        if (copiesAvailable == null) {
            throw new ValidationException("Number of available copies is required");
        }
    }

    private void validateRange(Integer copiesAvailable) throws ValidationException {
        if (copiesAvailable < 0) {
            throw new ValidationException("Available copies cannot be negative");
        }
        if (copiesAvailable > 100) {
            throw new ValidationException("Available copies cannot exceed 100");
        }
    }

}
