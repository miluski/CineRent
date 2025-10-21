package pl.kielce.tu.backend.model.constant;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class RecommendationConstantsTest {

    @Test
    void shouldHaveExpectedNumberOfConstants() {
        assertEquals(4, RecommendationConstants.values().length, "There should be exactly 4 constants");
    }

    @Test
    void shouldHaveExpectedValues() {
        assertAll(
                () -> assertEquals(5, RecommendationConstants.MAX_RECOMMENDATIONS.getValue(),
                        "MAX_RECOMMENDATIONS value"),
                () -> assertEquals(5, RecommendationConstants.MIN_AGE_RANGE.getValue(), "MIN_AGE_RANGE value"),
                () -> assertEquals(5, RecommendationConstants.MAX_AGE_RANGE.getValue(), "MAX_AGE_RANGE value"),
                () -> assertEquals(18, RecommendationConstants.MIN_ADULT_AGE.getValue(), "MIN_ADULT_AGE value"));
    }

    @Test
    void valuesShouldBeNonNegative() {
        for (RecommendationConstants rc : RecommendationConstants.values()) {
            assertTrue(rc.getValue() >= 0, () -> "Value for " + rc.name() + " should be non-negative");
        }
    }
}
