package pl.kielce.tu.backend.service.validation.implementations.genre;

import org.springframework.stereotype.Component;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import pl.kielce.tu.backend.exception.ValidationException;
import pl.kielce.tu.backend.model.constant.ValidationStrategyType;
import pl.kielce.tu.backend.repository.DvdRepository;
import pl.kielce.tu.backend.repository.GenreRepository;
import pl.kielce.tu.backend.service.validation.FieldValidationStrategy;
import pl.kielce.tu.backend.util.UserContextLogger;

@Component
@RequiredArgsConstructor
public class GenreDeletionValidationStrategy implements FieldValidationStrategy<Long> {

    private final GenreRepository genreRepository;
    private final DvdRepository dvdRepository;
    private final UserContextLogger userContextLogger;

    @Override
    public void validate(Long genreId) throws ValidationException {
        String idInfo = genreId != null ? genreId.toString() : "null";
        userContextLogger.logValidationOperation("GENRE_DELETION", "STARTED", idInfo);
        try {
            validateNotNull(genreId);
            validateGenreExists(genreId);
            validateMinimumGenreCount();
            validateNotAssignedToDvds(genreId);
            userContextLogger.logValidationOperation("GENRE_DELETION", "SUCCESS", idInfo);
        } catch (ValidationException e) {
            userContextLogger.logValidationOperation("GENRE_DELETION", "FAILURE", e.getMessage());
            throw e;
        } catch (EntityNotFoundException e) {
            userContextLogger.logValidationOperation("GENRE_DELETION", "FAILURE", e.getMessage());
            throw e;
        }
    }

    @Override
    public ValidationStrategyType getStrategyType() {
        return ValidationStrategyType.GENRE_DELETION;
    }

    private void validateNotNull(Long genreId) throws ValidationException {
        if (genreId == null) {
            throw new ValidationException("Genre ID is required");
        }
    }

    private void validateGenreExists(Long genreId) throws EntityNotFoundException {
        boolean exists = genreRepository.existsById(genreId);
        if (!exists) {
            throw new EntityNotFoundException("Genre not found with ID: " + genreId);
        }
    }

    private void validateMinimumGenreCount() throws ValidationException {
        long genreCount = genreRepository.count();
        if (genreCount <= 2) {
            throw new ValidationException("Cannot delete genre: minimum 2 genres must remain in database");
        }
    }

    private void validateNotAssignedToDvds(Long genreId) throws ValidationException {
        boolean isAssigned = dvdRepository.existsByGenresId(genreId);
        if (isAssigned) {
            throw new ValidationException("Cannot delete genre: it is assigned to one or more DVDs");
        }
    }

}
