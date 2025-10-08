package pl.kielce.tu.backend.model.constant;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class RankTypeTest {

    @Test
    void valuesContainExpectedConstantsInOrder() {
        RankType[] values = RankType.values();
        assertEquals(2, values.length, "There should be exactly two RankType constants");
        assertArrayEquals(new RankType[] { RankType.USER, RankType.ADMIN }, values,
                "Order of enum constants should be USER, ADMIN");
    }

    @Test
    void valueOfReturnsCorrectEnum() {
        assertEquals(RankType.USER, RankType.valueOf("USER"));
        assertEquals(RankType.ADMIN, RankType.valueOf("ADMIN"));
    }

    @Test
    void namesAndOrdinalsAreStable() {
        assertEquals("USER", RankType.USER.name());
        assertEquals("ADMIN", RankType.ADMIN.name());
        assertEquals(0, RankType.USER.ordinal());
        assertEquals(1, RankType.ADMIN.ordinal());
    }
}
