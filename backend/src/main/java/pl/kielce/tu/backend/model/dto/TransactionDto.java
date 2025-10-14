package pl.kielce.tu.backend.model.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pl.kielce.tu.backend.model.constant.BillType;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Transaction details containing rental billing information")
public class TransactionDto {

    @Schema(description = "Unique transaction identifier", example = "1")
    private Long id;

    @Schema(description = "Unique invoice identifier", example = "INV-ABC12345")
    private String invoiceId;

    @Schema(description = "Title of the rented DVD", example = "Matrix")
    private String dvdTitle;

    @Schema(description = "Number of rental days", example = "7")
    private Integer rentalPeriodDays;

    @Schema(description = "Price per day for the rental", example = "5.00")
    private BigDecimal pricePerDay;

    @Schema(description = "Late fee charged for overdue return", example = "10.50")
    private BigDecimal lateFee;

    @Schema(description = "Total amount charged", example = "45.50")
    private BigDecimal totalAmount;

    @Schema(description = "When the transaction was generated", example = "2025-10-14T10:30:00")
    private LocalDateTime generatedAt;

    @Schema(description = "URL to the PDF bill document", example = "https://api.example.com/files/bills/INV-ABC12345.pdf")
    private String pdfUrl;

    @Schema(description = "Type of the bill document", example = "INVOICE")
    private BillType billType;

    @Schema(description = "ID of the related rental", example = "42")
    private Long rentalId;
    
}
