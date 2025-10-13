package pl.kielce.tu.backend.model.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pl.kielce.tu.backend.model.constant.BillType;

@Data
@Embeddable
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {

    @Column(name = "invoice_id", unique = true)
    private String invoiceId;

    @Column(name = "dvd_title", nullable = false)
    private String dvdTitle;

    @Column(name = "rental_period_days", nullable = false)
    private Integer rentalPeriodDays;

    @Column(name = "price_per_day", nullable = false, precision = 10, scale = 2)
    private BigDecimal pricePerDay;

    @Column(name = "late_fee", nullable = false, precision = 10, scale = 2)
    private BigDecimal lateFee;

    @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "generated_at", nullable = false)
    private LocalDateTime generatedAt;

    @Column(name = "pdf_url")
    private String pdfUrl;

    @Enumerated
    @Builder.Default
    @Column(name = "bill_type", nullable = false)
    private BillType billType = BillType.INVOICE;

}
