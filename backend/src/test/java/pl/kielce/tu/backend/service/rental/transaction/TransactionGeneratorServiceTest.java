package pl.kielce.tu.backend.service.rental.transaction;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import pl.kielce.tu.backend.mapper.TransactionMapper;
import pl.kielce.tu.backend.model.entity.Rental;
import pl.kielce.tu.backend.model.entity.Transaction;
import pl.kielce.tu.backend.service.rental.calculation.LateFeeCalculationService;
import pl.kielce.tu.backend.service.rental.calculation.RentalCalculationService;
import pl.kielce.tu.backend.util.UserContextLogger;

@ExtendWith(MockitoExtension.class)
class TransactionGeneratorServiceTest {

    @Mock
    private TransactionMapper transactionMapper;

    @Mock
    private LateFeeCalculationService lateFeeService;

    @Mock
    private RentalCalculationService calculationService;

    @Mock
    private UserContextLogger userContextLogger;

    private TransactionGeneratorService transactionService;

    @BeforeEach
    void setUp() {
        transactionService = new TransactionGeneratorService(transactionMapper, userContextLogger, lateFeeService,
                calculationService);
    }

    @Test
    void generateTransaction_withValidRental_shouldGenerateCompleteTransaction() {
        Rental rental = new Rental();
        rental.setId(1L);
        Transaction expectedTransaction = new Transaction();

        BigDecimal baseAmount = new BigDecimal("35.00");
        BigDecimal lateFee = BigDecimal.ZERO;
        BigDecimal totalAmount = new BigDecimal("35.00");
        long rentalDays = 7L;

        when(calculationService.calculateRentalDays(rental)).thenReturn(rentalDays);
        when(calculationService.calculateBaseAmount(rental, rentalDays)).thenReturn(baseAmount);
        when(lateFeeService.calculateLateFee(rental)).thenReturn(lateFee);
        when(calculationService.calculateTotalAmount(baseAmount, lateFee)).thenReturn(totalAmount);
        when(transactionMapper.toEntity(rental, rentalDays, baseAmount, lateFee, totalAmount))
                .thenReturn(expectedTransaction);

        Transaction result = transactionService.generateTransaction(rental);

        assertNotNull(result);
        assertEquals(expectedTransaction, result);
        verify(calculationService).calculateRentalDays(rental);
        verify(calculationService).calculateBaseAmount(rental, rentalDays);
        verify(lateFeeService).calculateLateFee(rental);
        verify(calculationService).calculateTotalAmount(baseAmount, lateFee);
        verify(transactionMapper).toEntity(rental, rentalDays, baseAmount, lateFee, totalAmount);
    }

    @Test
    void generateTransaction_withLateFees_shouldIncludeLateFeeInCalculation() {
        Rental rental = new Rental();
        Transaction expectedTransaction = new Transaction();

        BigDecimal baseAmount = new BigDecimal("35.00");
        BigDecimal lateFee = new BigDecimal("50.00");
        BigDecimal totalAmount = new BigDecimal("85.00");
        long rentalDays = 7L;

        when(calculationService.calculateRentalDays(rental)).thenReturn(rentalDays);
        when(calculationService.calculateBaseAmount(rental, rentalDays)).thenReturn(baseAmount);
        when(lateFeeService.calculateLateFee(rental)).thenReturn(lateFee);
        when(calculationService.calculateTotalAmount(baseAmount, lateFee)).thenReturn(totalAmount);
        when(transactionMapper.toEntity(rental, rentalDays, baseAmount, lateFee, totalAmount))
                .thenReturn(expectedTransaction);

        Transaction result = transactionService.generateTransaction(rental);

        assertNotNull(result);
        verify(lateFeeService).calculateLateFee(rental);
        verify(calculationService).calculateTotalAmount(eq(baseAmount), eq(lateFee));
    }

    @Test
    void generateTransaction_shouldCallAllDependenciesExactlyOnce() {
        Rental rental = new Rental();
        Transaction expectedTransaction = new Transaction();

        when(calculationService.calculateRentalDays(rental)).thenReturn(5L);
        when(calculationService.calculateBaseAmount(eq(rental), eq(5L))).thenReturn(new BigDecimal("25.00"));
        when(lateFeeService.calculateLateFee(rental)).thenReturn(BigDecimal.ZERO);
        when(calculationService.calculateTotalAmount(eq(new BigDecimal("25.00")), eq(BigDecimal.ZERO)))
                .thenReturn(new BigDecimal("25.00"));
        when(transactionMapper.toEntity(eq(rental), eq(5L), eq(new BigDecimal("25.00")), eq(BigDecimal.ZERO),
                eq(new BigDecimal("25.00")))).thenReturn(expectedTransaction);

        transactionService.generateTransaction(rental);

        verify(calculationService, times(1)).calculateRentalDays(rental);
        verify(calculationService, times(1)).calculateBaseAmount(rental, 5L);
        verify(lateFeeService, times(1)).calculateLateFee(rental);
        verify(calculationService, times(1)).calculateTotalAmount(new BigDecimal("25.00"), BigDecimal.ZERO);
        verify(transactionMapper, times(1)).toEntity(rental, 5L, new BigDecimal("25.00"), BigDecimal.ZERO,
                new BigDecimal("25.00"));
    }
}
