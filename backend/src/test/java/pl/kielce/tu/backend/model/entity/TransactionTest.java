package pl.kielce.tu.backend.model.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

import pl.kielce.tu.backend.model.constant.BillType;

class TransactionTest {

    @Test
    void builderSetsFieldsAndUsesDefaultBillType() {
        LocalDateTime now = LocalDateTime.now();
        Transaction tx = Transaction.builder()
                .invoiceId("INV-123")
                .dvdTitle("The Matrix")
                .rentalPeriodDays(5)
                .pricePerDay(new BigDecimal("3.50"))
                .lateFee(new BigDecimal("1.00"))
                .totalAmount(new BigDecimal("18.50"))
                .generatedAt(now)
                .pdfUrl("http://example.com/inv-123.pdf")
                .build();

        assertEquals("INV-123", tx.getInvoiceId());
        assertEquals("The Matrix", tx.getDvdTitle());
        assertEquals(5, tx.getRentalPeriodDays());
        assertEquals(new BigDecimal("3.50"), tx.getPricePerDay());
        assertEquals(new BigDecimal("1.00"), tx.getLateFee());
        assertEquals(new BigDecimal("18.50"), tx.getTotalAmount());
        assertEquals(now, tx.getGeneratedAt());
        assertEquals("http://example.com/inv-123.pdf", tx.getPdfUrl());
        assertEquals(BillType.INVOICE, tx.getBillType(),
                "Builder should provide default BillType.INVOICE when not set");
    }

    @Test
    void noArgsConstructorAndSettersWorkAndDefaultBillTypeIsPresent() {
        Transaction tx = new Transaction();
        tx.setInvoiceId("INV-999");
        tx.setDvdTitle("Inception");
        tx.setRentalPeriodDays(2);
        tx.setPricePerDay(new BigDecimal("4.00"));
        tx.setLateFee(new BigDecimal("0.00"));
        tx.setTotalAmount(new BigDecimal("8.00"));
        LocalDateTime generated = LocalDateTime.of(2024, 1, 1, 12, 0);
        tx.setGeneratedAt(generated);
        tx.setPdfUrl(null);

        assertEquals("INV-999", tx.getInvoiceId());
        assertEquals("Inception", tx.getDvdTitle());
        assertEquals(2, tx.getRentalPeriodDays());
        assertEquals(new BigDecimal("4.00"), tx.getPricePerDay());
        assertEquals(new BigDecimal("0.00"), tx.getLateFee());
        assertEquals(new BigDecimal("8.00"), tx.getTotalAmount());
        assertEquals(generated, tx.getGeneratedAt());
        assertNull(tx.getPdfUrl());
        assertEquals(BillType.INVOICE, tx.getBillType());
    }

    @Test
    void equalsAndHashCodeBasedOnFields() {
        LocalDateTime now = LocalDateTime.now();
        Transaction a = Transaction.builder()
                .invoiceId("EQ-1")
                .dvdTitle("Title")
                .rentalPeriodDays(1)
                .pricePerDay(new BigDecimal("2.00"))
                .lateFee(new BigDecimal("0.00"))
                .totalAmount(new BigDecimal("2.00"))
                .generatedAt(now)
                .build();

        Transaction b = Transaction.builder()
                .invoiceId("EQ-1")
                .dvdTitle("Title")
                .rentalPeriodDays(1)
                .pricePerDay(new BigDecimal("2.00"))
                .lateFee(new BigDecimal("0.00"))
                .totalAmount(new BigDecimal("2.00"))
                .generatedAt(now)
                .build();

        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void toStringContainsImportantFields() {
        Transaction tx = Transaction.builder()
                .invoiceId("TS-1")
                .dvdTitle("Sample")
                .rentalPeriodDays(3)
                .pricePerDay(new BigDecimal("1.50"))
                .lateFee(new BigDecimal("0.25"))
                .totalAmount(new BigDecimal("4.75"))
                .generatedAt(LocalDateTime.of(2023, 12, 31, 23, 59))
                .build();

        String s = tx.toString();
        assertNotNull(s);
        assertTrue(s.contains("TS-1"));
        assertTrue(s.contains("Sample"));
        assertTrue(s.contains("3"));
    }
}
