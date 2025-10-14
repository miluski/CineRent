package pl.kielce.tu.backend.service.invoice;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import pl.kielce.tu.backend.exception.ValidationException;
import pl.kielce.tu.backend.extractor.ClaimsExtractor;
import pl.kielce.tu.backend.mapper.TransactionMapper;
import pl.kielce.tu.backend.model.constant.BillType;
import pl.kielce.tu.backend.model.constant.CookieNames;
import pl.kielce.tu.backend.model.constant.RankType;
import pl.kielce.tu.backend.model.constant.RentalStatus;
import pl.kielce.tu.backend.model.dto.BillRequestDto;
import pl.kielce.tu.backend.model.dto.TransactionDto;
import pl.kielce.tu.backend.model.entity.Rental;
import pl.kielce.tu.backend.repository.RentalRepository;
import pl.kielce.tu.backend.repository.TransactionRepository;
import pl.kielce.tu.backend.repository.UserRepository;
import pl.kielce.tu.backend.service.auth.CookieService;
import pl.kielce.tu.backend.service.invoice.factory.BillPdfStrategyFactory;
import pl.kielce.tu.backend.service.invoice.strategy.BillPdfStrategy;
import pl.kielce.tu.backend.util.UserContextLogger;

@Service
@RequiredArgsConstructor
public class InvoiceService {

    @Value("${jwt.secret}")
    private String jwtSecret;

    private final CookieService cookieService;
    private final UserRepository userRepository;
    private final ClaimsExtractor claimsExtractor;
    private final RentalRepository rentalRepository;
    private final TransactionMapper transactionMapper;
    private final UserContextLogger userContextLogger;
    private final BillPdfStrategyFactory strategyFactory;
    private final TransactionRepository transactionRepository;

    public ResponseEntity<List<TransactionDto>> handleGetUserTransactions(HttpServletRequest request) {
        try {
            Long userId = extractUserIdFromRequest(request);
            List<TransactionDto> transactions = transactionRepository
                    .findByUserIdOrderByGeneratedAtDesc(userId)
                    .stream()
                    .map(transactionMapper::toDto)
                    .toList();
            userContextLogger.logUserOperation("TRANSACTIONS_RETRIEVED",
                    "Retrieved " + transactions.size() + " transactions for user");
            return ResponseEntity.ok(transactions);
        } catch (Exception e) {
            userContextLogger.logUserOperation("TRANSACTIONS_ERROR",
                    "Failed to get user transactions: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    public ResponseEntity<List<TransactionDto>> handleGetAllTransactions(HttpServletRequest request) {
        try {
            validateAdminAccess(request);
            List<TransactionDto> transactions = transactionRepository
                    .findAllOrderByGeneratedAtDesc()
                    .stream()
                    .map(transactionMapper::toDto)
                    .toList();
            userContextLogger.logUserOperation("ALL_TRANSACTIONS_RETRIEVED",
                    "Retrieved " + transactions.size() + " transactions for admin");
            return ResponseEntity.ok(transactions);
        } catch (Exception e) {
            userContextLogger.logUserOperation("ALL_TRANSACTIONS_ERROR",
                    "Failed to get all transactions: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    public ResponseEntity<byte[]> handleGenerateBill(Long billId,
            BillRequestDto billRequest, HttpServletRequest request) {
        try {
            Long currentUserId = extractUserIdFromRequest(request);
            boolean isAdmin = isUserAdmin(currentUserId);
            Rental rental = findRentalById(billId);
            validateRentalEnded(rental);
            validateUserAccess(rental, currentUserId, isAdmin);
            byte[] pdfBytes = generatePdfForBill(rental, billRequest.getBillType());
            return createPdfResponse(pdfBytes, rental, billRequest.getBillType());
        } catch (ValidationException | SecurityException | EntityNotFoundException e) {
            userContextLogger.logUserOperation("BILL_GENERATION_ERROR",
                    "Failed to generate bill: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            userContextLogger.logUserOperation("BILL_GENERATION_ERROR",
                    "Failed to generate bill: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    private Rental findRentalById(Long billId) {
        return rentalRepository.findById(billId)
                .orElseThrow(() -> new EntityNotFoundException("Bill not found with id: " + billId));
    }

    private void validateUserAccess(Rental rental, Long currentUserId, boolean isAdmin) {
        if (!isAdmin && !rental.getUser().getId().equals(currentUserId)) {
            throw new SecurityException("Access denied to bill");
        }
    }

    private byte[] generatePdfForBill(Rental rental, BillType billType) {
        BillPdfStrategy strategy = strategyFactory.getStrategy(billType);
        return strategy.generatePdf(rental);
    }

    private ResponseEntity<byte[]> createPdfResponse(byte[] pdfBytes,
            Rental rental, BillType billType) {
        String filename = generateFilename(rental, billType);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", filename);
        headers.setContentLength(pdfBytes.length);
        userContextLogger.logUserOperation("BILL_GENERATED",
                "Generated " + billType + " PDF for rental: " + rental.getId());
        return ResponseEntity.status(HttpStatus.OK)
                .headers(headers)
                .body(pdfBytes);
    }

    private String generateFilename(Rental rental, BillType billType) {
        String typePrefix = billType == BillType.INVOICE ? "faktura" : "paragon";
        return String.format("%s_%d.pdf", typePrefix, rental.getId());
    }

    private Long extractUserIdFromRequest(HttpServletRequest request) {
        String token = cookieService.getTokenFromCookie(request, CookieNames.ACCESS_TOKEN);
        if (token == null) {
            throw new RuntimeException("Authentication token not found");
        }
        return claimsExtractor.extractUserId(token, jwtSecret);
    }

    private void validateAdminAccess(HttpServletRequest request) {
        Long userId = extractUserIdFromRequest(request);
        if (!isUserAdmin(userId)) {
            throw new SecurityException("Admin access required");
        }
    }

    private boolean isUserAdmin(Long userId) {
        return userRepository.findById(userId)
                .map(user -> RankType.ADMIN.equals(user.getRank()))
                .orElse(false);
    }

    private void validateRentalEnded(Rental rental) throws ValidationException {
        if (!RentalStatus.INACTIVE.equals(rental.getStatus())) {
            throw new ValidationException("Bill can only be generated for ended rentals");
        }
    }

}
