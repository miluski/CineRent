package pl.kielce.tu.backend.service.validation.implementations.dvd;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import pl.kielce.tu.backend.exception.ValidationException;
import pl.kielce.tu.backend.model.constant.ValidationStrategyType;
import pl.kielce.tu.backend.service.validation.FieldValidationStrategy;
import pl.kielce.tu.backend.util.UserContextLogger;

@Component
@RequiredArgsConstructor
public class DvdDescriptionValidationStrategy implements FieldValidationStrategy<String> {

    private final UserContextLogger userContextLogger;

    @Override
    public void validate(String description) throws ValidationException {
        String descInfo = description != null ? description.length() + " chars" : "null";
        userContextLogger.logValidationOperation("DVD_DESCRIPTION", "STARTED", descInfo);

        validateNotNull(description);
        validateNotBlank(description);
        validateLength(description);

        userContextLogger.logValidationOperation("DVD_DESCRIPTION", "SUCCESS", descInfo);
    }

    @Override
    public ValidationStrategyType getStrategyType() {
        return ValidationStrategyType.DVD_DESCRIPTION;
    }

    private void validateNotNull(String description) throws ValidationException {
        if (description == null) {
            throw new ValidationException("Description is required");
        }
    }

    private void validateNotBlank(String description) throws ValidationException {
        if (description.trim().isEmpty()) {
            throw new ValidationException("Description cannot be blank");
        }
    }

    private void validateLength(String description) throws ValidationException {
        if (description.length() < 25) {
            throw new ValidationException("Description must be at least 25 characters long");
        }
        if (description.length() > 500) {
            throw new ValidationException("Description cannot exceed 500 characters");
        }
    }

}
