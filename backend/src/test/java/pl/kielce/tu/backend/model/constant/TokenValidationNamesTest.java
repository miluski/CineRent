package pl.kielce.tu.backend.model.constant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

class TokenValidationNamesTest {

    @Test
    void values_shouldContainAllExpectedConstants_andHaveExpectedCount() {
        TokenValidationNames[] values = TokenValidationNames.values();
        assertEquals(5, values.length, "There should be exactly 5 enum constants");

        assertTrue(Arrays.asList(values).contains(TokenValidationNames.PRESENCE));
        assertTrue(Arrays.asList(values).contains(TokenValidationNames.BLACKLIST));
        assertTrue(Arrays.asList(values).contains(TokenValidationNames.EXTRACTION));
        assertTrue(Arrays.asList(values).contains(TokenValidationNames.USER_EXISTENCE));
        assertTrue(Arrays.asList(values).contains(TokenValidationNames.ADMIN_ACCESS));
    }

    @Test
    void valueOf_shouldReturnEnumForEachName() {
        for (TokenValidationNames v : TokenValidationNames.values()) {
            TokenValidationNames byName = TokenValidationNames.valueOf(v.name());
            assertSame(v, byName, "valueOf should return the same enum instance for name: " + v.name());
        }
    }

    @Test
    void names_shouldBeUnique() {
        TokenValidationNames[] values = TokenValidationNames.values();
        Set<String> names = new HashSet<>();
        for (TokenValidationNames v : values) {
            names.add(v.name());
        }
        assertEquals(values.length, names.size(), "Enum constant names must be unique");
    }
}
