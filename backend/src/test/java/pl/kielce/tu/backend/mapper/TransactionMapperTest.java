package pl.kielce.tu.backend.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import pl.kielce.tu.backend.model.entity.Dvd;
import pl.kielce.tu.backend.model.entity.Rental;
import pl.kielce.tu.backend.model.entity.Transaction;

class TransactionMapperTest {

    private TransactionMapper transactionMapper;

    @BeforeEach
    void setUp() {
        transactionMapper = new TransactionMapper();
    }

    @Test
    void shouldCreateTransactionEntity() {
        Rental rental = createTestRental();
        long rentalDays = 7L;
        BigDecimal baseAmount = new BigDecimal("35.00");
        BigDecimal lateFee = new BigDecimal("10.00");
        BigDecimal totalAmount = new BigDecimal("45.00");

        Transaction result = transactionMapper.toEntity(rental, rentalDays, baseAmount, lateFee, totalAmount);

        assertNotNull(result);
        assertEquals(lateFee, result.getLateFee());
        assertEquals(totalAmount, result.getTotalAmount());
        assertEquals((int) rentalDays, result.getRentalPeriodDays());
        assertNotNull(result.getInvoiceId());
        assertTrue(result.getInvoiceId().startsWith("INV-"));
        assertEquals("Test Movie", result.getDvdTitle());
    }

    @Test
    void shouldGenerateUniqueInvoiceIds() {
        Rental rental = createTestRental();
        long rentalDays = 5L;
        BigDecimal baseAmount = new BigDecimal("25.00");
        BigDecimal lateFee = BigDecimal.ZERO;
        BigDecimal totalAmount = new BigDecimal("25.00");

        Transaction first = transactionMapper.toEntity(rental, rentalDays, baseAmount, lateFee, totalAmount);
        Transaction second = transactionMapper.toEntity(rental, rentalDays, baseAmount, lateFee, totalAmount);

        assertNotNull(first.getInvoiceId());
        assertNotNull(second.getInvoiceId());
        assertTrue(first.getInvoiceId().startsWith("INV-"));
        assertTrue(second.getInvoiceId().startsWith("INV-"));
    }

    private Rental createTestRental() {
        Rental rental = new Rental();
        rental.setId(1L);

        Dvd dvd = new Dvd();
        dvd.setTitle("Test Movie");
        dvd.setRentalPricePerDay(5.00f);
        rental.setDvd(dvd);

        return rental;
    }
}
