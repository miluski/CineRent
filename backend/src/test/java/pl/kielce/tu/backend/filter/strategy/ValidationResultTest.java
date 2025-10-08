package pl.kielce.tu.backend.filter.strategy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import org.junit.jupiter.api.Test;

class ValidationResultTest {

    @Test
    void gettersReturnProvidedValues() {
        ValidationResult vr = new ValidationResult(true, "payload");
        assertTrue(vr.isSuccess(), "expected success to be true");
        assertEquals("payload", vr.getData(), "expected data to match provided payload");
    }

    @Test
    void allowsNullData() {
        ValidationResult vr = new ValidationResult(false, null);
        assertFalse(vr.isSuccess(), "expected success to be false");
        assertNull(vr.getData(), "expected data to be null when constructed with null");
    }

    @Test
    void fieldsArePrivateAndFinal() throws NoSuchFieldException {
        Field successField = ValidationResult.class.getDeclaredField("success");
        Field dataField = ValidationResult.class.getDeclaredField("data");

        int successMods = successField.getModifiers();
        int dataMods = dataField.getModifiers();

        assertTrue(Modifier.isPrivate(successMods), "success field should be private");
        assertTrue(Modifier.isFinal(successMods), "success field should be final");
        assertEquals(boolean.class, successField.getType(), "success field should be boolean");

        assertTrue(Modifier.isPrivate(dataMods), "data field should be private");
        assertTrue(Modifier.isFinal(dataMods), "data field should be final");
        assertEquals(Object.class, dataField.getType(), "data field should be Object");
    }
}
