package pl.kielce.tu.backend.model.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

class GenreDtoTest {

    private static Validator validator;
    private static ValidatorFactory validatorFactory;

    @BeforeAll
    static void setUp() {
        validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();
    }

    @AfterAll
    static void tearDown() {
        validatorFactory.close();
    }

    @Test
    void shouldCreateGenreWithBuilderAndAccessorsWork() {
        GenreDto dto = GenreDto.builder()
                .id(1L)
                .name("Drama")
                .build();

        assertEquals(1L, dto.getId());
        assertEquals("Drama", dto.getName());
        assertNotNull(dto.toString());
    }

    @Test
    void validGenreShouldHaveNoConstraintViolations() {
        GenreDto dto = new GenreDto();
        dto.setName("Science-Fiction");

        Set<ConstraintViolation<GenreDto>> violations = validator.validate(dto);
        assertTrue(violations.isEmpty(), "Expected no validation violations for a valid GenreDto");
    }

    @Test
    void nullNameShouldTriggerNotBlankViolation() {
        GenreDto dto = new GenreDto();
        dto.setName(null);

        Set<ConstraintViolation<GenreDto>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());

        Optional<ConstraintViolation<GenreDto>> nameViolation = violations.stream()
                .filter(v -> "name".equals(v.getPropertyPath().toString()))
                .findFirst();

        assertTrue(nameViolation.isPresent());
        assertEquals("Name is required", nameViolation.get().getMessage());
    }

    @Test
    void blankNameShouldTriggerNotBlankViolation() {
        GenreDto dto = new GenreDto();
        dto.setName("   ");

        Set<ConstraintViolation<GenreDto>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());

        Optional<ConstraintViolation<GenreDto>> nameViolation = violations.stream()
                .filter(v -> "name".equals(v.getPropertyPath().toString()))
                .findFirst();

        assertTrue(nameViolation.isPresent());
        assertNotNull(nameViolation.get().getMessage());
    }

    @Test
    void tooShortNameShouldTriggerSizeViolation() {
        GenreDto dto = new GenreDto();
        dto.setName("Abcd");

        Set<ConstraintViolation<GenreDto>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());

        Optional<ConstraintViolation<GenreDto>> sizeViolation = violations.stream()
                .filter(v -> "name".equals(v.getPropertyPath().toString()))
                .findFirst();

        assertTrue(sizeViolation.isPresent());
        assertEquals("Name must be between 5 and 75 characters", sizeViolation.get().getMessage());
    }

    @Test
    void tooLongNameShouldTriggerSizeViolation() {
        String longName = "A".repeat(76);
        GenreDto dto = new GenreDto();
        dto.setName(longName);

        Set<ConstraintViolation<GenreDto>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());

        Optional<ConstraintViolation<GenreDto>> sizeViolation = violations.stream()
                .filter(v -> "name".equals(v.getPropertyPath().toString()))
                .findFirst();

        assertTrue(sizeViolation.isPresent());
        assertEquals("Name must be between 5 and 75 characters", sizeViolation.get().getMessage());
    }

    @Test
    void equalsAndHashCodeFromLombokShouldConsiderFields() {
        GenreDto a = GenreDto.builder().id(10L).name("Comedy").build();
        GenreDto b = GenreDto.builder().id(10L).name("Comedy").build();
        GenreDto c = GenreDto.builder().id(11L).name("Comedy").build();

        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
        assertNotEquals(a, c);
    }
}
