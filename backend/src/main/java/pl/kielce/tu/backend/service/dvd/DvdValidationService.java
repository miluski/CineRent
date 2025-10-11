package pl.kielce.tu.backend.service.dvd;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import pl.kielce.tu.backend.exception.ValidationException;
import pl.kielce.tu.backend.model.constant.ValidationStrategyType;
import pl.kielce.tu.backend.model.dto.DvdDto;
import pl.kielce.tu.backend.service.validation.factory.ValidationStrategyFactory;

@Component
@RequiredArgsConstructor
public class DvdValidationService {

    private final ValidationStrategyFactory validationFactory;

    public void validateForCreation(DvdDto dvdDto) throws ValidationException {
        if (dvdDto == null) {
            throw new ValidationException("DVD data cannot be null");
        }
        validateAllFields(dvdDto);
    }

    public void validateForUpdate(DvdDto dvdDto) throws ValidationException {
        if (hasNoFieldsToUpdate(dvdDto)) {
            throw new ValidationException("At least one field must be provided for update");
        }
    }

    public void validateTitle(String title) throws ValidationException {
        if (title != null) {
            validationFactory.getStrategy(ValidationStrategyType.DVD_TITLE).validate(title);
        }
    }

    public void validateYear(Integer year) throws ValidationException {
        if (year != null) {
            validationFactory.getStrategy(ValidationStrategyType.DVD_YEAR).validate(year);
        }
    }

    public void validateDirectors(Object directors) throws ValidationException {
        if (directors != null) {
            validationFactory.getStrategy(ValidationStrategyType.DVD_DIRECTORS).validate(directors);
        }
    }

    public void validateDescription(String description) throws ValidationException {
        if (description != null) {
            validationFactory.getStrategy(ValidationStrategyType.DVD_DESCRIPTION).validate(description);
        }
    }

    public void validateDuration(Integer duration) throws ValidationException {
        if (duration != null) {
            validationFactory.getStrategy(ValidationStrategyType.DVD_DURATION).validate(duration);
        }
    }

    public void validateCopies(Integer copies) throws ValidationException {
        if (copies != null) {
            validationFactory.getStrategy(ValidationStrategyType.DVD_COPIES).validate(copies);
        }
    }

    public void validatePrice(Float price) throws ValidationException {
        if (price != null) {
            validationFactory.getStrategy(ValidationStrategyType.DVD_PRICE).validate(price);
        }
    }

    public void validateGenres(Object genres) throws ValidationException {
        if (genres != null) {
            validationFactory.getStrategy(ValidationStrategyType.GENRE).validate(genres);
        }
    }

    private void validateAllFields(DvdDto dvdDto) throws ValidationException {
        validateTitle(dvdDto.getTitle());
        validateGenres(dvdDto.getGenresIdentifiers());
        validateYear(dvdDto.getReleaseYear());
        validateDirectors(dvdDto.getDirectors());
        validateDescription(dvdDto.getDescription());
        validateDuration(dvdDto.getDurationMinutes());
        validateCopies(dvdDto.getCopiesAvailable());
        validatePrice(dvdDto.getRentalPricePerDay());
    }

    private boolean hasNoFieldsToUpdate(DvdDto dvdDto) {
        return dvdDto.getTitle() == null
                && dvdDto.getGenresIdentifiers() == null
                && dvdDto.getReleaseYear() == null
                && dvdDto.getDirectors() == null
                && dvdDto.getDescription() == null
                && dvdDto.getDurationMinutes() == null
                && dvdDto.getAvailable() == null
                && dvdDto.getCopiesAvailable() == null
                && dvdDto.getRentalPricePerDay() == null
                && dvdDto.getPosterImage() == null;
    }
}
