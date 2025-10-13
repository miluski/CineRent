package pl.kielce.tu.backend.model.constant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

public class BillTypeTest {

    @Test
    void valuesContainExpectedEntriesInOrder() {
        BillType[] values = BillType.values();
        assertEquals(2, values.length, "There should be exactly two BillType values");
        assertEquals(BillType.INVOICE, values[0], "First enum constant should be INVOICE");
        assertEquals(BillType.RECEIPT, values[1], "Second enum constant should be RECEIPT");
    }

    @Test
    void valueOfReturnsCorrectEnum() {
        assertEquals(BillType.INVOICE, BillType.valueOf("INVOICE"));
        assertEquals(BillType.RECEIPT, BillType.valueOf("RECEIPT"));
    }

    @Test
    void nameAndToStringAreConsistent() {
        assertEquals("INVOICE", BillType.INVOICE.name());
        assertEquals("RECEIPT", BillType.RECEIPT.name());
        assertEquals(BillType.INVOICE.name(), BillType.INVOICE.toString());
        assertEquals(BillType.RECEIPT.name(), BillType.RECEIPT.toString());
    }

    @Test
    void valueOfWithInvalidNameThrows() {
        assertThrows(IllegalArgumentException.class, () -> BillType.valueOf("UNKNOWN"));
    }
}
