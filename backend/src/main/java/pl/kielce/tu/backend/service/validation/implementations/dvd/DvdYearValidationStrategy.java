package pl.kielce.tu.backend.service.validation.implementations.dvd;

import java.time.Year;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import pl.kielce.tu.backend.exception.ValidationException;
import pl.kielce.tu.backend.model.constant.ValidationStrategyType;
import pl.kielce.tu.backend.service.validation.FieldValidationStrategy;
import pl.kielce.tu.backend.util.UserContextLogger;

@Component
@RequiredArgsConstructor
public class DvdYearValidationStrategy implements FieldValidationStrategy<Integer> {

    private final UserContextLogger userContextLogger;

    @Override
    public void validate(Integer releaseYear) throws ValidationException {
        String yearInfo = releaseYear != null ? releaseYear.toString() : "null";
        userContextLogger.logValidationOperation("DVD_YEAR", "STARTED", yearInfo);

        validateNotNull(releaseYear);
        validateRange(releaseYear);

        userContextLogger.logValidationOperation("DVD_YEAR", "SUCCESS", yearInfo);
    }

    @Override
    public ValidationStrategyType getStrategyType() {
        return ValidationStrategyType.DVD_YEAR;
    }

    private void validateNotNull(Integer releaseYear) throws ValidationException {
        if (releaseYear == null) {
            throw new ValidationException("Release year is required");
        }
    }

    private void validateRange(Integer releaseYear) throws ValidationException {
        if (releaseYear < 1000) {
            throw new ValidationException("Release year must be greater than 1000");
        }

        int currentYear = Year.now().getValue();
        if (releaseYear > currentYear) {
            throw new ValidationException("Release year cannot be in the future");
        }
    }

}
