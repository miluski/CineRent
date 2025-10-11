package pl.kielce.tu.backend.service.validation.implementations.dvd;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import pl.kielce.tu.backend.exception.ValidationException;
import pl.kielce.tu.backend.model.constant.ValidationStrategyType;
import pl.kielce.tu.backend.service.validation.FieldValidationStrategy;
import pl.kielce.tu.backend.util.UserContextLogger;

@Component
@RequiredArgsConstructor
public class DvdTitleValidationStrategy implements FieldValidationStrategy<String> {

    private final UserContextLogger userContextLogger;

    @Override
    public void validate(String title) throws ValidationException {
        String titleInfo = title != null ? title.length() + " chars" : "null";
        userContextLogger.logValidationOperation("DVD_TITLE", "STARTED", titleInfo);

        validateNotNull(title);
        validateNotBlank(title);
        validateLength(title);

        userContextLogger.logValidationOperation("DVD_TITLE", "SUCCESS", titleInfo);
    }

    @Override
    public ValidationStrategyType getStrategyType() {
        return ValidationStrategyType.DVD_TITLE;
    }

    private void validateNotNull(String title) throws ValidationException {
        if (title == null) {
            throw new ValidationException("Title is required");
        }
    }

    private void validateNotBlank(String title) throws ValidationException {
        if (title.trim().isEmpty()) {
            throw new ValidationException("Title cannot be blank");
        }
    }

    private void validateLength(String title) throws ValidationException {
        if (title.length() < 5) {
            throw new ValidationException("Title must be at least 5 characters long");
        }
        if (title.length() > 75) {
            throw new ValidationException("Title cannot exceed 75 characters");
        }
    }

}
