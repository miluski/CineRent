package pl.kielce.tu.backend.service.validation.implementations.dvd;

import java.util.List;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import pl.kielce.tu.backend.exception.ValidationException;
import pl.kielce.tu.backend.model.constant.ValidationStrategyType;
import pl.kielce.tu.backend.service.validation.FieldValidationStrategy;
import pl.kielce.tu.backend.util.UserContextLogger;

@Component
@RequiredArgsConstructor
public class DvdDirectorsValidationStrategy implements FieldValidationStrategy<List<String>> {

    private final UserContextLogger userContextLogger;

    @Override
    public void validate(List<String> directors) throws ValidationException {
        String directorsInfo = directors != null ? directors.size() + " directors" : "null";
        userContextLogger.logValidationOperation("DVD_DIRECTORS", "STARTED", directorsInfo);

        validateNotNull(directors);
        validateNotEmpty(directors);
        validateEachDirector(directors);

        userContextLogger.logValidationOperation("DVD_DIRECTORS", "SUCCESS", directorsInfo);
    }

    @Override
    public ValidationStrategyType getStrategyType() {
        return ValidationStrategyType.DVD_DIRECTORS;
    }

    private void validateNotNull(List<String> directors) throws ValidationException {
        if (directors == null) {
            throw new ValidationException("Directors list is required");
        }
    }

    private void validateNotEmpty(List<String> directors) throws ValidationException {
        if (directors.isEmpty()) {
            throw new ValidationException("At least one director is required");
        }
    }

    private void validateEachDirector(List<String> directors) throws ValidationException {
        for (String director : directors) {
            validateDirectorNotNull(director);
            validateDirectorNotBlank(director);
            validateDirectorLength(director);
        }
    }

    private void validateDirectorNotNull(String director) throws ValidationException {
        if (director == null) {
            throw new ValidationException("Director name cannot be null");
        }
    }

    private void validateDirectorNotBlank(String director) throws ValidationException {
        if (director.trim().isEmpty()) {
            throw new ValidationException("Director name cannot be blank");
        }
    }

    private void validateDirectorLength(String director) throws ValidationException {
        if (director.length() < 10) {
            throw new ValidationException("Director name must be at least 10 characters long");
        }
        if (director.length() > 50) {
            throw new ValidationException("Director name cannot exceed 50 characters");
        }
    }

}
