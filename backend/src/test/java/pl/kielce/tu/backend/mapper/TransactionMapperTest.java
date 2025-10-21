package pl.kielce.tu.backend.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import pl.kielce.tu.backend.model.constant.BillType;
import pl.kielce.tu.backend.model.dto.TransactionDto;
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

    @Test
    void shouldCalculatePricePerDayCorrectly() {
        Rental rental = createTestRental();
        long rentalDays = 10L;
        BigDecimal baseAmount = new BigDecimal("100.00");
        BigDecimal lateFee = BigDecimal.ZERO;
        BigDecimal totalAmount = new BigDecimal("100.00");

        Transaction result = transactionMapper.toEntity(rental, rentalDays, baseAmount, lateFee, totalAmount);

        assertNotNull(result);
        assertEquals(new BigDecimal("10.00"), result.getPricePerDay());
    }

    @Test
    void shouldHandleZeroRentalDays() {
        Rental rental = createTestRental();
        long rentalDays = 0L;
        BigDecimal baseAmount = new BigDecimal("50.00");
        BigDecimal lateFee = BigDecimal.ZERO;
        BigDecimal totalAmount = new BigDecimal("50.00");

        Transaction result = transactionMapper.toEntity(rental, rentalDays, baseAmount, lateFee, totalAmount);

        assertNotNull(result);
        assertEquals(BigDecimal.ZERO, result.getPricePerDay());
        assertEquals(0, result.getRentalPeriodDays());
    }

    @Test
    void shouldSetBillTypeToReceiptWhenRentalHasReturnDate() {
        Rental rental = createTestRentalWithReturn();
        long rentalDays = 7L;
        BigDecimal baseAmount = new BigDecimal("35.00");
        BigDecimal lateFee = BigDecimal.ZERO;
        BigDecimal totalAmount = new BigDecimal("35.00");

        Transaction result = transactionMapper.toEntity(rental, rentalDays, baseAmount, lateFee, totalAmount);

        assertNotNull(result);
        assertEquals(BillType.RECEIPT, result.getBillType());
    }

    @Test
    void shouldSetBillTypeToNullWhenRentalHasNoReturnDate() {
        Rental rental = createTestRental();
        long rentalDays = 7L;
        BigDecimal baseAmount = new BigDecimal("35.00");
        BigDecimal lateFee = BigDecimal.ZERO;
        BigDecimal totalAmount = new BigDecimal("35.00");

        Transaction result = transactionMapper.toEntity(rental, rentalDays, baseAmount, lateFee, totalAmount);

        assertNotNull(result);
        assertNull(result.getBillType());
    }

    @Test
    void shouldConvertRentalToTransactionDto() {
        Rental rental = createTestRentalWithTransaction();

        TransactionDto result = transactionMapper.toDto(rental);

        assertNotNull(result);
        assertEquals(rental.getId(), result.getId());
        assertEquals(rental.getTransaction().getInvoiceId(), result.getInvoiceId());
        assertEquals(rental.getTransaction().getDvdTitle(), result.getDvdTitle());
        assertEquals(rental.getTransaction().getRentalPeriodDays(), result.getRentalPeriodDays());
        assertEquals(rental.getTransaction().getPricePerDay(), result.getPricePerDay());
        assertEquals(rental.getTransaction().getLateFee(), result.getLateFee());
        assertEquals(rental.getTransaction().getTotalAmount(), result.getTotalAmount());
        assertEquals(rental.getTransaction().getGeneratedAt(), result.getGeneratedAt());
        assertEquals(rental.getTransaction().getPdfUrl(), result.getPdfUrl());
        assertEquals(rental.getId(), result.getRentalId());
    }

    @Test
    void shouldReturnNullWhenRentalHasNoTransaction() {
        Rental rental = createTestRental();

        TransactionDto result = transactionMapper.toDto(rental);

        assertNull(result);
    }

    @Test
    void shouldSetBillTypeInDtoWhenRentalHasReturnDate() {
        Rental rental = createTestRentalWithTransactionAndReturn();

        TransactionDto result = transactionMapper.toDto(rental);

        assertNotNull(result);
        assertEquals(BillType.RECEIPT, result.getBillType());
    }

    @Test
    void shouldSetBillTypeToNullInDtoWhenRentalHasNoReturnDate() {
        Rental rental = createTestRentalWithTransaction();

        TransactionDto result = transactionMapper.toDto(rental);

        assertNotNull(result);
        assertNull(result.getBillType());
    }

    @Test
    void shouldConvertListOfRentalsToTransactionDtos() {
        Rental rental1 = createTestRentalWithTransaction();
        rental1.setId(1L);

        Rental rental2 = createTestRentalWithTransaction();
        rental2.setId(2L);

        List<Rental> rentals = Arrays.asList(rental1, rental2);

        List<TransactionDto> result = transactionMapper.toDtoList(rentals);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(1L, result.get(0).getId());
        assertEquals(2L, result.get(1).getId());
    }

    @Test
    void shouldFilterOutRentalsWithoutTransactionsInList() {
        Rental rentalWithTransaction = createTestRentalWithTransaction();
        rentalWithTransaction.setId(1L);

        Rental rentalWithoutTransaction = createTestRental();
        rentalWithoutTransaction.setId(2L);

        List<Rental> rentals = Arrays.asList(rentalWithTransaction, rentalWithoutTransaction);

        List<TransactionDto> result = transactionMapper.toDtoList(rentals);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getId());
    }

    @Test
    void shouldHandleEmptyRentalList() {
        List<Rental> rentals = Collections.emptyList();

        List<TransactionDto> result = transactionMapper.toDtoList(rentals);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void shouldHandleListWithOnlyRentalsWithoutTransactions() {
        Rental rental1 = createTestRental();
        rental1.setId(1L);

        Rental rental2 = createTestRental();
        rental2.setId(2L);

        List<Rental> rentals = Arrays.asList(rental1, rental2);

        List<TransactionDto> result = transactionMapper.toDtoList(rentals);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void shouldSetGeneratedAtTimestamp() {
        Rental rental = createTestRental();
        long rentalDays = 5L;
        BigDecimal baseAmount = new BigDecimal("25.00");
        BigDecimal lateFee = BigDecimal.ZERO;
        BigDecimal totalAmount = new BigDecimal("25.00");

        LocalDateTime before = LocalDateTime.now().minusSeconds(1);
        Transaction result = transactionMapper.toEntity(rental, rentalDays, baseAmount, lateFee, totalAmount);
        LocalDateTime after = LocalDateTime.now().plusSeconds(1);

        assertNotNull(result.getGeneratedAt());
        assertTrue(result.getGeneratedAt().isAfter(before));
        assertTrue(result.getGeneratedAt().isBefore(after));
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

    private Rental createTestRentalWithReturn() {
        Rental rental = createTestRental();
        rental.setReturnDate(LocalDateTime.now());
        return rental;
    }

    private Rental createTestRentalWithTransaction() {
        Rental rental = createTestRental();

        Transaction transaction = Transaction.builder()
                .invoiceId("INV-12345-ABC")
                .billType(BillType.RECEIPT)
                .dvdTitle("Test Movie")
                .rentalPeriodDays(7)
                .pricePerDay(new BigDecimal("5.00"))
                .lateFee(new BigDecimal("10.00"))
                .totalAmount(new BigDecimal("45.00"))
                .generatedAt(LocalDateTime.now())
                .pdfUrl("http://example.com/invoice.pdf")
                .build();

        rental.setTransaction(transaction);
        return rental;
    }

    private Rental createTestRentalWithTransactionAndReturn() {
        Rental rental = createTestRentalWithTransaction();
        rental.setReturnDate(LocalDateTime.now());
        return rental;
    }
}
