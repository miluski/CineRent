package pl.kielce.tu.backend.model.constant;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Map;

import org.junit.jupiter.api.Test;

class DvdStatusesTest {

    @Test
    void shouldHaveTwoStatuses() {
        assertEquals(2, DvdStatuses.values().length, "There should be exactly two DVD statuses");
    }

    @Test
    void shouldReturnCorrectValueForEachStatus() {
        Map<String, String> expected = Map.of(
                "AVALAIBLE", "AVALAIBLE",
                "UNAVALAIBLE", "UNAVALAIBLE");

        for (Map.Entry<String, String> e : expected.entrySet()) {
            String name = e.getKey();
            String expectedValue = e.getValue();
            DvdStatuses status = DvdStatuses.valueOf(name);
            assertEquals(expectedValue, status.getValue(), "Value for " + name + " should match expected");
        }
    }
}
