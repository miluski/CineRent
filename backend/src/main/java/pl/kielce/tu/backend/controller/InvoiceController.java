package pl.kielce.tu.backend.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import pl.kielce.tu.backend.model.dto.BillRequestDto;
import pl.kielce.tu.backend.model.dto.TransactionDto;
import pl.kielce.tu.backend.service.invoice.InvoiceService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/transactions")
@Tag(name = "Invoice Management", description = "Operations for managing transactions and generating bills")
public class InvoiceController {

    private final InvoiceService invoiceService;

    @GetMapping
    @Operation(summary = "Get user transactions", description = """
            Retrieves all transactions for the authenticated user. \
            Returns transaction history sorted by generation date (newest first). \
            Only shows transactions belonging to the current user based on JWT token.""", security = {
            @SecurityRequirement(name = "accessToken") })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Transactions retrieved successfully", content = @Content(schema = @Schema(example = """
                    [
                      {
                        "id": 1,
                        "invoiceId": "INV-1697890123-ABC12345",
                        "dvdTitle": "Matrix",
                        "rentalPeriodDays": 7,
                        "pricePerDay": 4.00,
                        "lateFee": 0.00,
                        "totalAmount": 28.00,
                        "generatedAt": "2025-10-01T14:30:00",
                        "pdfUrl": null,
                        "billType": "RECEIPT",
                        "rentalId": 1
                      }
                    ]"""))),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token"),
            @ApiResponse(responseCode = "500", description = "Internal server error occurred")
    })
    public ResponseEntity<List<TransactionDto>> getUserTransactions(HttpServletRequest request) {
        return invoiceService.handleGetUserTransactions(request);
    }

    @GetMapping("all")
    @Operation(summary = "Get all transactions (Admin only)", description = """
            Retrieves all transactions from all users. \
            Returns complete transaction history sorted by generation date (newest first). \
            This endpoint is restricted to admin users only.""", security = {
            @SecurityRequirement(name = "accessToken") })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "All transactions retrieved successfully", content = @Content(schema = @Schema(example = """
                    [
                      {
                        "id": 1,
                        "invoiceId": "INV-1697890123-ABC12345",
                        "dvdTitle": "Matrix",
                        "rentalPeriodDays": 7,
                        "pricePerDay": 4.00,
                        "lateFee": 2.00,
                        "totalAmount": 30.00,
                        "generatedAt": "2025-10-01T14:30:00",
                        "pdfUrl": null,
                        "billType": "INVOICE",
                        "rentalId": 1
                      }
                    ]"""))),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Admin access required"),
            @ApiResponse(responseCode = "500", description = "Internal server error occurred")
    })
    public ResponseEntity<List<TransactionDto>> getAllTransactions(HttpServletRequest request) {
        return invoiceService.handleGetAllTransactions(request);
    }

    @PostMapping("/bill/{id}")
    @Operation(summary = "Generate and download bill PDF", description = """
            Generates a PDF bill (invoice or receipt) for a specific transaction. \
            Users can only access their own bills, while admins can access any bill. \
            The PDF is generated in Polish language following Polish standards for receipts and invoices. \
            Bill includes rental period information and late fees if applicable.""", security = {
            @SecurityRequirement(name = "accessToken") })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "PDF generated successfully", content = @Content(mediaType = "application/pdf")),
            @ApiResponse(responseCode = "400", description = "Bad request - Invalid rental ID, rental not ended, or access denied to bill"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token"),
            @ApiResponse(responseCode = "404", description = "Rental not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error occurred")
    })
    public ResponseEntity<byte[]> generateBill(
            @Parameter(description = "Bill/Transaction ID", example = "1") @PathVariable Long id,
            @Parameter(description = "Bill generation request with bill type") @Valid @RequestBody BillRequestDto billRequest,
            HttpServletRequest request) {
        return invoiceService.handleGenerateBill(id, billRequest, request);
    }
}
