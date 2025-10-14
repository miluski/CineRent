package pl.kielce.tu.backend.service.invoice;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;

import jakarta.servlet.http.HttpServletRequest;
import pl.kielce.tu.backend.extractor.ClaimsExtractor;
import pl.kielce.tu.backend.mapper.TransactionMapper;
import pl.kielce.tu.backend.model.constant.BillType;
import pl.kielce.tu.backend.model.constant.CookieNames;
import pl.kielce.tu.backend.model.constant.RankType;
import pl.kielce.tu.backend.model.constant.RentalStatus;
import pl.kielce.tu.backend.model.dto.BillRequestDto;
import pl.kielce.tu.backend.model.dto.TransactionDto;
import pl.kielce.tu.backend.model.entity.Rental;
import pl.kielce.tu.backend.model.entity.User;
import pl.kielce.tu.backend.repository.RentalRepository;
import pl.kielce.tu.backend.repository.TransactionRepository;
import pl.kielce.tu.backend.repository.UserRepository;
import pl.kielce.tu.backend.service.auth.CookieService;
import pl.kielce.tu.backend.service.invoice.factory.BillPdfStrategyFactory;
import pl.kielce.tu.backend.service.invoice.strategy.BillPdfStrategy;
import pl.kielce.tu.backend.util.UserContextLogger;

@ExtendWith(MockitoExtension.class)
class InvoiceServiceTest {

    @Mock
    private CookieService cookieService;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ClaimsExtractor claimsExtractor;
    @Mock
    private RentalRepository rentalRepository;
    @Mock
    private TransactionMapper transactionMapper;
    @Mock
    private UserContextLogger userContextLogger;
    @Mock
    private BillPdfStrategyFactory strategyFactory;
    @Mock
    private TransactionRepository transactionRepository;

    private InvoiceService invoiceService;

    @BeforeEach
    void setUp() {
        invoiceService = new InvoiceService(cookieService, userRepository, claimsExtractor,
                rentalRepository, transactionMapper, userContextLogger, strategyFactory,
                transactionRepository);
        ReflectionTestUtils.setField(invoiceService, "jwtSecret", "test-secret");
    }

    @Test
    void handleGetUserTransactions_success_returnsOk() {
        HttpServletRequest request = org.mockito.Mockito.mock(HttpServletRequest.class);
        when(cookieService.getTokenFromCookie(request, CookieNames.ACCESS_TOKEN)).thenReturn("token");
        when(claimsExtractor.extractUserId("token", "test-secret")).thenReturn(42L);
        when(transactionRepository.findByUserIdOrderByGeneratedAtDesc(42L)).thenReturn(Collections.emptyList());

        var response = invoiceService.handleGetUserTransactions(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        List<TransactionDto> body = response.getBody();
        assertEquals(0, body != null ? body.size() : 0);
    }

    @Test
    void handleGetUserTransactions_missingToken_returnsInternalServerError() {
        HttpServletRequest request = org.mockito.Mockito.mock(HttpServletRequest.class);
        when(cookieService.getTokenFromCookie(request, CookieNames.ACCESS_TOKEN)).thenReturn(null);

        var response = invoiceService.handleGetUserTransactions(request);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    void handleGetAllTransactions_asAdmin_returnsOk() {
        HttpServletRequest request = org.mockito.Mockito.mock(HttpServletRequest.class);
        when(cookieService.getTokenFromCookie(request, CookieNames.ACCESS_TOKEN)).thenReturn("token");
        when(claimsExtractor.extractUserId("token", "test-secret")).thenReturn(1L);
        User adminUser = org.mockito.Mockito.mock(User.class);
        when(adminUser.getRank()).thenReturn(RankType.ADMIN);
        when(userRepository.findById(1L)).thenReturn(Optional.of(adminUser));
        when(transactionRepository.findAllOrderByGeneratedAtDesc()).thenReturn(Collections.emptyList());

        var response = invoiceService.handleGetAllTransactions(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        List<TransactionDto> body = response.getBody();
        assertEquals(0, body != null ? body.size() : 0);
    }

    @Test
    void handleGetAllTransactions_notAdmin_returnsForbidden() {
        HttpServletRequest request = org.mockito.Mockito.mock(HttpServletRequest.class);
        when(cookieService.getTokenFromCookie(request, CookieNames.ACCESS_TOKEN)).thenReturn("token");
        when(claimsExtractor.extractUserId("token", "test-secret")).thenReturn(2L);
        User nonAdmin = org.mockito.Mockito.mock(User.class);
        when(nonAdmin.getRank()).thenReturn(RankType.USER);
        when(userRepository.findById(2L)).thenReturn(Optional.of(nonAdmin));

        var response = invoiceService.handleGetAllTransactions(request);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    void handleGenerateBill_success_returnsPdfResponse() {
        HttpServletRequest request = org.mockito.Mockito.mock(HttpServletRequest.class);
        when(cookieService.getTokenFromCookie(request, CookieNames.ACCESS_TOKEN)).thenReturn("token");
        when(claimsExtractor.extractUserId("token", "test-secret")).thenReturn(10L);

        User rentalUser = org.mockito.Mockito.mock(User.class);
        when(rentalUser.getId()).thenReturn(10L);

        Rental rental = org.mockito.Mockito.mock(Rental.class);
        when(rental.getStatus()).thenReturn(RentalStatus.INACTIVE);
        when(rental.getUser()).thenReturn(rentalUser);
        when(rental.getId()).thenReturn(123L);
        when(rentalRepository.findById(123L)).thenReturn(Optional.of(rental));
        when(userRepository.findById(10L)).thenReturn(Optional.of(rentalUser));
        when(rentalUser.getRank()).thenReturn(RankType.USER);
        BillRequestDto billRequest = org.mockito.Mockito.mock(BillRequestDto.class);
        when(billRequest.getBillType()).thenReturn(BillType.INVOICE);
        BillPdfStrategy strategy = org.mockito.Mockito.mock(BillPdfStrategy.class);
        byte[] pdf = "pdf-bytes".getBytes();
        when(strategy.generatePdf(rental)).thenReturn(pdf);
        when(strategyFactory.getStrategy(BillType.INVOICE)).thenReturn(strategy);

        var response = invoiceService.handleGenerateBill(123L, billRequest, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(MediaType.APPLICATION_PDF, response.getHeaders().getContentType());
        assertEquals(pdf.length, response.getHeaders().getContentLength());
        assertArrayEquals(pdf, response.getBody());
        String contentDisposition = response.getHeaders().getFirst("Content-Disposition");
        org.junit.jupiter.api.Assertions
                .assertTrue(contentDisposition != null && contentDisposition.contains("faktura_123.pdf"));
    }

    @Test
    void handleGenerateBill_rentalNotEnded_returnsBadRequest() {
        HttpServletRequest request = org.mockito.Mockito.mock(HttpServletRequest.class);
        when(cookieService.getTokenFromCookie(request, CookieNames.ACCESS_TOKEN)).thenReturn("token");
        when(claimsExtractor.extractUserId("token", "test-secret")).thenReturn(11L);

        Rental rental = org.mockito.Mockito.mock(Rental.class);
        when(rentalRepository.findById(5L)).thenReturn(Optional.of(rental));
        when(rental.getStatus()).thenReturn(RentalStatus.ACTIVE);

        BillRequestDto billRequest = org.mockito.Mockito.mock(BillRequestDto.class);

        var response = invoiceService.handleGenerateBill(5L, billRequest, request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void handleGenerateBill_accessDenied_returnsBadRequest() {
        HttpServletRequest request = org.mockito.Mockito.mock(HttpServletRequest.class);
        when(cookieService.getTokenFromCookie(request, CookieNames.ACCESS_TOKEN)).thenReturn("token");
        when(claimsExtractor.extractUserId("token", "test-secret")).thenReturn(20L);

        User owner = org.mockito.Mockito.mock(User.class);
        when(owner.getId()).thenReturn(999L);
        when(userRepository.findById(20L)).thenReturn(Optional.of(org.mockito.Mockito.mock(User.class)));

        Rental rental = org.mockito.Mockito.mock(Rental.class);
        when(rentalRepository.findById(7L)).thenReturn(Optional.of(rental));
        when(rental.getStatus()).thenReturn(RentalStatus.INACTIVE);
        when(rental.getUser()).thenReturn(owner);

        BillRequestDto billRequest = org.mockito.Mockito.mock(BillRequestDto.class);

        var response = invoiceService.handleGenerateBill(7L, billRequest, request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }
}
