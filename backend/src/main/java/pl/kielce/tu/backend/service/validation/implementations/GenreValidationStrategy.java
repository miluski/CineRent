package pl.kielce.tu.backend.service.validation.implementations;

import java.util.List;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import pl.kielce.tu.backend.exception.ValidationException;
import pl.kielce.tu.backend.model.constant.ValidationStrategyType;
import pl.kielce.tu.backend.repository.GenreRepository;
import pl.kielce.tu.backend.service.validation.FieldValidationStrategy;

@Slf4j
@Component
@RequiredArgsConstructor
public class GenreValidationStrategy implements FieldValidationStrategy<List<Long>> {

    private final GenreRepository genreRepository;

    @Override
    public void validate(List<Long> genreIds) throws ValidationException {
        if (genreIds == null || genreIds.isEmpty()) {
            return;
        }
        validateAllGenresExist(genreIds);
    }

    @Override
    public ValidationStrategyType getStrategyType() {
        return ValidationStrategyType.GENRE;
    }

    private void validateAllGenresExist(List<Long> genreIds) throws ValidationException {
        for (Long genreId : genreIds) {
            validateGenreExists(genreId);
        }
    }

    private void validateGenreExists(Long genreId) throws ValidationException {
        validateNotNull(genreId);
        validateGenreExistsInDatabase(genreId);
    }

    private void validateNotNull(Long genreId) throws ValidationException {
        if (genreId == null) {
            throw new ValidationException("Genre identifier cannot be null");
        }
    }

    private void validateGenreExistsInDatabase(Long genreId) throws ValidationException {
        boolean exists = genreRepository.existsById(genreId);
        if (!exists) {
            throw new ValidationException(
                    String.format("Genre with identifier %d does not exist", genreId));
        }
    }

}
