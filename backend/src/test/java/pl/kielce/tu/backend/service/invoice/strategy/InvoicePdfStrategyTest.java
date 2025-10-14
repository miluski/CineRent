package pl.kielce.tu.backend.service.invoice.strategy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

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
import pl.kielce.tu.backend.model.entity.User;
import pl.kielce.tu.backend.util.UserContextLogger;

@ExtendWith(MockitoExtension.class)
class InvoicePdfStrategyTest {

    @Mock
    private UserContextLogger userContextLogger;

    @InjectMocks
    private InvoicePdfStrategy strategy;

    @Mock
    private Rental rental;

    @Mock
    private Transaction transaction;

    @Mock
    private User user;

    @Test
    void getSupportedBillType_shouldReturnInvoice() {
        assertEquals(BillType.INVOICE, strategy.getSupportedBillType());
    }

    @Test
    void generatePdf_whenInternalExceptionOccurs_shouldLogAndRethrow() {
        lenient().when(rental.getId()).thenReturn(123L);
        lenient().when(rental.getTransaction()).thenReturn(transaction);
        lenient().when(transaction.getInvoiceId()).thenReturn("INV-1");
        lenient().when(transaction.getGeneratedAt()).thenReturn(LocalDateTime.now());
        lenient().when(transaction.getLateFee()).thenReturn(BigDecimal.ZERO);
        lenient().when(transaction.getDvdTitle()).thenReturn("Some DVD");
        lenient().when(rental.getUser()).thenReturn(user);
        lenient().when(user.getNickname()).thenReturn("nick");
        lenient().when(transaction.getTotalAmount()).thenReturn(null);
        assertThrows(RuntimeException.class, () -> strategy.generatePdf(rental));
        verify(userContextLogger).logUserOperation(
                eq("INVOICE_PDF_GENERATION_ERROR"),
                contains("Failed to generate invoice PDF for rental: 123"));
        verify(userContextLogger, never()).logUserOperation(eq("SOME_OTHER_KEY"), contains(""));
    }

}
