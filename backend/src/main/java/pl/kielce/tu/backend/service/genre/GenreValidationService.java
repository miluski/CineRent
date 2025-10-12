package pl.kielce.tu.backend.service.genre;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import pl.kielce.tu.backend.exception.ValidationException;
import pl.kielce.tu.backend.model.constant.ValidationStrategyType;
import pl.kielce.tu.backend.model.dto.GenreDto;
import pl.kielce.tu.backend.service.validation.factory.ValidationStrategyFactory;

@Component
@RequiredArgsConstructor
public class GenreValidationService {

    private final ValidationStrategyFactory validationFactory;

    public void validateForCreation(GenreDto genreDto) throws ValidationException {
        if (genreDto == null) {
            throw new ValidationException("Genre data cannot be null");
        }
        validateName(genreDto.getName());
    }

    public void validateForDeletion(String genreId) throws ValidationException {
        validateGenreIdFormat(genreId);
        Long id = Long.parseLong(genreId);
        validateDeletion(id);
    }

    private void validateName(String name) throws ValidationException {
        if (name != null) {
            validationFactory.getStrategy(ValidationStrategyType.GENRE_NAME).validate(name);
        }
    }

    private void validateGenreIdFormat(String genreId) throws ValidationException {
        if (genreId == null || genreId.trim().isEmpty()) {
            throw new ValidationException("Genre ID cannot be empty");
        }
        try {
            Long.parseLong(genreId);
        } catch (NumberFormatException e) {
            throw new ValidationException("Genre ID must be a valid number");
        }
    }

    private void validateDeletion(Long genreId) throws ValidationException {
        validationFactory.getStrategy(ValidationStrategyType.GENRE_DELETION).validate(genreId);
    }

}
