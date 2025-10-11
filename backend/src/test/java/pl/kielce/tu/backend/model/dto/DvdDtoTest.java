package pl.kielce.tu.backend.model.dto;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

public class DvdDtoTest {

    private static ValidatorFactory validatorFactory;
    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();
    }

    @AfterAll
    static void closeValidatorFactory() {
        validatorFactory.close();
    }

    private DvdDto buildValidDto() {
        return DvdDto.builder()
                .id(1L)
                .title("The Matrix")
                .genres(Arrays.asList("Action", "Sci-Fi"))
                .genresIdentifiers(Arrays.asList(1L))
                .releaseYear(1999)
                .directors(Arrays.asList("Lana Wachowski"))
                .description("A computer hacker learns from mysterious rebels about the true nature of his reality.")
                .durationMinutes(136)
                .available(Boolean.TRUE)
                .copiesAvailable(5)
                .rentalPricePerDay(4.99f)
                .posterUrl("https://example.com/posters/matrix.jpg")
                .addedAt(LocalDateTime.now())
                .build();
    }

    @Test
    void validDtoHasNoValidationErrors() {
        DvdDto dto = buildValidDto();
        Set<ConstraintViolation<DvdDto>> violations = validator.validate(dto);
        assertTrue(violations.isEmpty(), "Expected no validation errors for a valid DTO");
    }

    @Test
    void titleTooShortTriggersValidationError() {
        DvdDto dto = buildValidDto();
        dto.setTitle("Abc");
        Set<ConstraintViolation<DvdDto>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> "title".equals(v.getPropertyPath().toString())));
    }

    @Test
    void emptyGenresIdentifiersTriggersValidationError() {
        DvdDto dto = buildValidDto();
        dto.setGenresIdentifiers(Collections.emptyList());
        Set<ConstraintViolation<DvdDto>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> "genresIdentifiers".equals(v.getPropertyPath().toString())));
    }

    @Test
    void releaseYearOutOfRangeTriggersValidationError() {
        DvdDto dto = buildValidDto();
        dto.setReleaseYear(3000);
        Set<ConstraintViolation<DvdDto>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> "releaseYear".equals(v.getPropertyPath().toString())));
    }

    @Test
    void directorNameTooShortTriggersValidationError() {
        DvdDto dto = buildValidDto();
        dto.setDirectors(Arrays.asList("Short"));
        Set<ConstraintViolation<DvdDto>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().contains("directors")));
    }

    @Test
    void descriptionTooShortTriggersValidationError() {
        DvdDto dto = buildValidDto();
        dto.setDescription("Too short description");
        Set<ConstraintViolation<DvdDto>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> "description".equals(v.getPropertyPath().toString())));
    }

    @Test
    void durationMustBeGreaterThanZero() {
        DvdDto dto = buildValidDto();
        dto.setDurationMinutes(0);
        Set<ConstraintViolation<DvdDto>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> "durationMinutes".equals(v.getPropertyPath().toString())));
    }

    @Test
    void copiesAvailableCannotBeNegative() {
        DvdDto dto = buildValidDto();
        dto.setCopiesAvailable(-1);
        Set<ConstraintViolation<DvdDto>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> "copiesAvailable".equals(v.getPropertyPath().toString())));
    }

    @Test
    void rentalPricePerDayMustBeWithinRange() {
        DvdDto dto = buildValidDto();
        dto.setRentalPricePerDay(0.0f);
        Set<ConstraintViolation<DvdDto>> violationsLow = validator.validate(dto);
        assertFalse(violationsLow.isEmpty());
        assertTrue(violationsLow.stream().anyMatch(v -> "rentalPricePerDay".equals(v.getPropertyPath().toString())));

        dto.setRentalPricePerDay(100.0f);
        Set<ConstraintViolation<DvdDto>> violationsHigh = validator.validate(dto);
        assertFalse(violationsHigh.isEmpty());
        assertTrue(violationsHigh.stream().anyMatch(v -> "rentalPricePerDay".equals(v.getPropertyPath().toString())));
    }

    @Test
    void availableMustNotBeNull() {
        DvdDto dto = buildValidDto();
        dto.setAvailable(null);
        Set<ConstraintViolation<DvdDto>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> "available".equals(v.getPropertyPath().toString())));
    }

    @Test
    void releaseYearCannotBeNull() {
        DvdDto dto = buildValidDto();
        dto.setReleaseYear(null);
        Set<ConstraintViolation<DvdDto>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> "releaseYear".equals(v.getPropertyPath().toString())));
    }
}
