package pl.kielce.tu.backend.model.constant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

class ValidationConstraintsTest {

    @Test
    void shouldHaveExpectedValues() {
        assertEquals(3, ValidationConstraints.MIN_NICKNAME_LENGTH.getValue());
        assertEquals(50, ValidationConstraints.MAX_NICKNAME_LENGTH.getValue());
        assertEquals(8, ValidationConstraints.MIN_PASSWORD_LENGTH.getValue());
        assertEquals(100, ValidationConstraints.MAX_PASSWORD_LENGTH.getValue());
    }

    @Test
    void valuesShouldBePositive() {
        for (ValidationConstraints c : ValidationConstraints.values()) {
            if (c.getPattern() == null) {
                assertTrue(c.getValue() > 0, c.name() + " must be positive");
            }
        }
    }

    @Test
    void nicknameMinShouldBeLessThanMax() {
        assertTrue(
                ValidationConstraints.MIN_NICKNAME_LENGTH.getValue() < ValidationConstraints.MAX_NICKNAME_LENGTH
                        .getValue(),
                "MIN_NICKNAME_LENGTH should be less than MAX_NICKNAME_LENGTH");
    }

    @Test
    void passwordMinShouldBeLessThanMax() {
        assertTrue(
                ValidationConstraints.MIN_PASSWORD_LENGTH.getValue() < ValidationConstraints.MAX_PASSWORD_LENGTH
                        .getValue(),
                "MIN_PASSWORD_LENGTH should be less than MAX_PASSWORD_LENGTH");
    }

    @Test
    void valuesShouldBeUnique() {
        Set<Integer> unique = Arrays.stream(ValidationConstraints.values())
                .map(ValidationConstraints::getValue)
                .collect(Collectors.toSet());
        assertEquals(ValidationConstraints.values().length, unique.size(), "Enum values must be unique");
    }
}
