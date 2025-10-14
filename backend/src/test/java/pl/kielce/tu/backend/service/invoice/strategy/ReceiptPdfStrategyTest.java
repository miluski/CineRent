package pl.kielce.tu.backend.service.invoice.strategy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import pl.kielce.tu.backend.model.constant.BillType;
import pl.kielce.tu.backend.model.entity.Rental;
import pl.kielce.tu.backend.model.entity.Transaction;
import pl.kielce.tu.backend.util.UserContextLogger;

@ExtendWith(MockitoExtension.class)
class ReceiptPdfStrategyTest {

    @Mock
    private UserContextLogger userContextLogger;

    @Mock
    private Rental rental;

    @Mock
    private Transaction transaction;

    @InjectMocks
    private ReceiptPdfStrategy receiptPdfStrategy;

    @Test
    void generatePdf_returnsNonEmptyBytes_whenTransactionValid() {
        lenient().when(rental.getTransaction()).thenReturn(transaction);
        lenient().when(rental.getId()).thenReturn(1L);
        lenient().when(transaction.getGeneratedAt()).thenReturn(LocalDateTime.now());
        lenient().when(transaction.getInvoiceId()).thenReturn("INV-001");
        lenient().when(transaction.getLateFee()).thenReturn(BigDecimal.ZERO);
        lenient().when(transaction.getTotalAmount()).thenReturn(new BigDecimal("10.00"));

        byte[] pdf = receiptPdfStrategy.generatePdf(rental);

        assertNotNull(pdf);
        assertTrue(pdf.length > 0);
        verify(userContextLogger, never()).logUserOperation(anyString(), anyString());
    }

    @Test
    void generatePdf_includesLateFeePath_andProducesPdf() {
        lenient().when(rental.getTransaction()).thenReturn(transaction);
        lenient().when(rental.getId()).thenReturn(2L);
        lenient().when(transaction.getGeneratedAt()).thenReturn(LocalDateTime.now());
        lenient().when(transaction.getInvoiceId()).thenReturn("INV-002");
        lenient().when(transaction.getLateFee()).thenReturn(new BigDecimal("2.50"));
        lenient().when(transaction.getTotalAmount()).thenReturn(new BigDecimal("12.50"));

        byte[] pdf = receiptPdfStrategy.generatePdf(rental);

        assertNotNull(pdf);
        assertTrue(pdf.length > 0);
        verify(userContextLogger, never()).logUserOperation(anyString(), anyString());
    }

    @Test
    void generatePdf_logsAndThrows_whenPdfGenerationFails() {
        when(rental.getTransaction()).thenReturn(transaction);
        when(transaction.getGeneratedAt()).thenReturn(null);
        when(rental.getId()).thenReturn(123L);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> receiptPdfStrategy.generatePdf(rental));
        assertTrue(ex.getMessage().contains("Failed to generate receipt PDF"));

        verify(userContextLogger, times(1))
                .logUserOperation(eq("RECEIPT_PDF_GENERATION_ERROR"),
                        contains("Failed to generate receipt PDF for rental: 123"));
    }

    @Test
    void getSupportedBillType_returnsReceipt() {
        assertEquals(BillType.RECEIPT, receiptPdfStrategy.getSupportedBillType());
    }
}
