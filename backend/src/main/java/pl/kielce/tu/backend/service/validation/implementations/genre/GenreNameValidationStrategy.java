package pl.kielce.tu.backend.service.validation.implementations.genre;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import pl.kielce.tu.backend.exception.ValidationException;
import pl.kielce.tu.backend.model.constant.ValidationStrategyType;
import pl.kielce.tu.backend.repository.GenreRepository;
import pl.kielce.tu.backend.service.validation.FieldValidationStrategy;
import pl.kielce.tu.backend.util.UserContextLogger;

@Component
@RequiredArgsConstructor
public class GenreNameValidationStrategy implements FieldValidationStrategy<String> {

    private final GenreRepository genreRepository;
    private final UserContextLogger userContextLogger;

    @Override
    public void validate(String genreName) throws ValidationException {
        String nameInfo = genreName != null ? genreName.length() + " chars" : "null";
        userContextLogger.logValidationOperation("GENRE_NAME", "STARTED", nameInfo);

        try {
            validateNotNull(genreName);
            validateNotBlank(genreName);
            validateLength(genreName);
            validateUniqueness(genreName);
            userContextLogger.logValidationOperation("GENRE_NAME", "SUCCESS", nameInfo);
        } catch (ValidationException e) {
            userContextLogger.logValidationOperation("GENRE_NAME", "FAILURE", e.getMessage());
            throw e;
        }
    }

    @Override
    public ValidationStrategyType getStrategyType() {
        return ValidationStrategyType.GENRE_NAME;
    }

    private void validateNotNull(String genreName) throws ValidationException {
        if (genreName == null) {
            throw new ValidationException("Genre name is required");
        }
    }

    private void validateNotBlank(String genreName) throws ValidationException {
        if (genreName.trim().isEmpty()) {
            throw new ValidationException("Genre name cannot be blank");
        }
    }

    private void validateLength(String genreName) throws ValidationException {
        if (genreName.length() < 5) {
            throw new ValidationException("Genre name must be at least 5 characters long");
        }
        if (genreName.length() > 75) {
            throw new ValidationException("Genre name cannot exceed 75 characters");
        }
    }

    private void validateUniqueness(String genreName) throws ValidationException {
        boolean exists = genreRepository.findByName(genreName).isPresent();
        if (exists) {
            throw new ValidationException("Genre name already exists");
        }
    }

}
