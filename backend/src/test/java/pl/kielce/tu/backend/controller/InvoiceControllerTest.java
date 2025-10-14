package pl.kielce.tu.backend.controller;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import jakarta.servlet.http.HttpServletRequest;
import pl.kielce.tu.backend.model.dto.BillRequestDto;
import pl.kielce.tu.backend.model.dto.TransactionDto;
import pl.kielce.tu.backend.service.invoice.InvoiceService;

class InvoiceControllerTest {

    @Test
    void getUserTransactions_delegatesToService_andReturnsResponse() {
        InvoiceService invoiceService = mock(InvoiceService.class);
        InvoiceController controller = new InvoiceController(invoiceService);

        HttpServletRequest request = mock(HttpServletRequest.class);
        ResponseEntity<List<TransactionDto>> expected = ResponseEntity.ok(Collections.<TransactionDto>emptyList());

        when(invoiceService.handleGetUserTransactions(request)).thenReturn(expected);

        ResponseEntity<List<TransactionDto>> actual = controller.getUserTransactions(request);

        verify(invoiceService, times(1)).handleGetUserTransactions(request);
        assertSame(expected, actual);
    }

    @Test
    void getAllTransactions_delegatesToService_andReturnsResponse() {
        InvoiceService invoiceService = mock(InvoiceService.class);
        InvoiceController controller = new InvoiceController(invoiceService);

        HttpServletRequest request = mock(HttpServletRequest.class);
        ResponseEntity<List<TransactionDto>> expected = ResponseEntity.ok(Collections.<TransactionDto>emptyList());

        when(invoiceService.handleGetAllTransactions(request)).thenReturn(expected);

        ResponseEntity<List<TransactionDto>> actual = controller.getAllTransactions(request);

        verify(invoiceService, times(1)).handleGetAllTransactions(request);
        assertSame(expected, actual);
    }

    @Test
    void generateBill_delegatesToService_withCorrectParameters_andReturnsResponse() {
        InvoiceService invoiceService = mock(InvoiceService.class);
        InvoiceController controller = new InvoiceController(invoiceService);

        HttpServletRequest request = mock(HttpServletRequest.class);
        BillRequestDto billRequest = mock(BillRequestDto.class);
        byte[] pdfBytes = new byte[] { 1, 2, 3 };
        ResponseEntity<byte[]> expected = ResponseEntity.ok(pdfBytes);

        when(invoiceService.handleGenerateBill(42L, billRequest, request)).thenReturn(expected);

        ResponseEntity<byte[]> actual = controller.generateBill(42L, billRequest, request);

        verify(invoiceService, times(1)).handleGenerateBill(42L, billRequest, request);
        assertSame(expected, actual);
    }
}
