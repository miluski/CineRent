package pl.kielce.tu.backend.model.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

import pl.kielce.tu.backend.model.constant.BillType;

class TransactionDtoTest {

    private TransactionDto sampleDto() {
        return TransactionDto.builder()
                .id(1L)
                .invoiceId("INV-ABC12345")
                .dvdTitle("Matrix")
                .rentalPeriodDays(7)
                .pricePerDay(BigDecimal.valueOf(5.00))
                .lateFee(BigDecimal.valueOf(10.50))
                .totalAmount(BigDecimal.valueOf(45.50))
                .generatedAt(LocalDateTime.of(2025, 10, 14, 10, 30, 0))
                .pdfUrl("https://api.example.com/files/bills/INV-ABC12345.pdf")
                .billType(BillType.INVOICE)
                .rentalId(42L)
                .build();
    }

    @Test
    void builder_populatesAllFields_and_gettersReturnValues() {
        TransactionDto dto = sampleDto();

        assertEquals(1L, dto.getId());
        assertEquals("INV-ABC12345", dto.getInvoiceId());
        assertEquals("Matrix", dto.getDvdTitle());
        assertEquals(7, dto.getRentalPeriodDays());
        assertEquals(BigDecimal.valueOf(5.00), dto.getPricePerDay());
        assertEquals(BigDecimal.valueOf(10.50), dto.getLateFee());
        assertEquals(BigDecimal.valueOf(45.50), dto.getTotalAmount());
        assertEquals(LocalDateTime.of(2025, 10, 14, 10, 30, 0), dto.getGeneratedAt());
        assertEquals("https://api.example.com/files/bills/INV-ABC12345.pdf", dto.getPdfUrl());
        assertEquals(BillType.INVOICE, dto.getBillType());
        assertEquals(42L, dto.getRentalId());
    }

    @Test
    void noArgsConstructor_and_settersWork_asExpected() {
        TransactionDto dto = new TransactionDto();
        dto.setId(1L);
        dto.setInvoiceId("INV-ABC12345");
        dto.setDvdTitle("Matrix");
        dto.setRentalPeriodDays(7);
        dto.setPricePerDay(BigDecimal.valueOf(5.00));
        dto.setLateFee(BigDecimal.valueOf(10.50));
        dto.setTotalAmount(BigDecimal.valueOf(45.50));
        dto.setGeneratedAt(LocalDateTime.of(2025, 10, 14, 10, 30, 0));
        dto.setPdfUrl("https://api.example.com/files/bills/INV-ABC12345.pdf");
        dto.setBillType(BillType.INVOICE);
        dto.setRentalId(42L);

        TransactionDto expected = sampleDto();
        assertEquals(expected, dto);
        assertEquals(expected.hashCode(), dto.hashCode());
    }

    @Test
    void equalsAndHashCode_reflectFieldChanges() {
        TransactionDto a = sampleDto();
        TransactionDto b = sampleDto();

        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());

        b.setId(2L);
        assertNotEquals(a, b);
    }

    @Test
    void toString_includesImportantInformation() {
        TransactionDto dto = sampleDto();
        String s = dto.toString();
        assertNotNull(s);
        assertTrue(s.contains("Matrix"));
        assertTrue(s.contains("INV-ABC12345"));
        assertTrue(s.contains("45.50") || s.contains("45.5")); 
    }
}
