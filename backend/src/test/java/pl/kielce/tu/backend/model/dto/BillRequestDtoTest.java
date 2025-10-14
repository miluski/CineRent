package pl.kielce.tu.backend.model.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import pl.kielce.tu.backend.model.constant.BillType;

class BillRequestDtoTest {

    private static ValidatorFactory validatorFactory;
    private static Validator validator;

    @BeforeAll
    static void setupValidator() {
        validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();
    }

    @AfterAll
    static void closeValidatorFactory() {
        validatorFactory.close();
    }

    @Test
    void shouldCreateDtoUsingBuilder() {
        BillRequestDto dto = BillRequestDto.builder()
                .billType(BillType.INVOICE)
                .build();

        assertNotNull(dto);
        assertEquals(BillType.INVOICE, dto.getBillType());
    }

    @Test
    void shouldCreateDtoUsingNoArgsAndSetters() {
        BillRequestDto dto = new BillRequestDto();
        dto.setBillType(BillType.RECEIPT);

        assertEquals(BillType.RECEIPT, dto.getBillType());
    }

    @Test
    void validationShouldFailWhenBillTypeIsNull() {
        BillRequestDto dto = new BillRequestDto(); 
        Set<ConstraintViolation<BillRequestDto>> violations = validator.validate(dto);

        assertFalse(violations.isEmpty(), "Expected validation violations when billType is null");
        boolean hasBillTypeViolation = violations.stream()
                .anyMatch(v -> "billType".equals(v.getPropertyPath().toString())
                        && v.getMessage().toLowerCase().contains("required"));
        assertTrue(hasBillTypeViolation, "Expected a 'billType' NotNull violation");
    }

    @Test
    void validationShouldPassWhenBillTypeIsPresent() {
        BillRequestDto dto = BillRequestDto.builder()
                .billType(BillType.INVOICE)
                .build();

        Set<ConstraintViolation<BillRequestDto>> violations = validator.validate(dto);
        assertTrue(violations.isEmpty(), "Expected no validation violations when billType is set");
    }

    @Test
    void equalsAndHashCodeShouldWorkAsExpected() {
        BillRequestDto a = BillRequestDto.builder().billType(BillType.INVOICE).build();
        BillRequestDto b = BillRequestDto.builder().billType(BillType.INVOICE).build();
        BillRequestDto c = BillRequestDto.builder().billType(BillType.RECEIPT).build();

        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
        assertNotEquals(a, c);
    }
}
