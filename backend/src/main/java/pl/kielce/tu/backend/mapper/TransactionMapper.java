package pl.kielce.tu.backend.mapper;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.stereotype.Component;

import pl.kielce.tu.backend.model.constant.BillType;
import pl.kielce.tu.backend.model.entity.Rental;
import pl.kielce.tu.backend.model.entity.Transaction;

@Component
public class TransactionMapper {

    public Transaction toEntity(Rental rental, long rentalDays, BigDecimal baseAmount,
            BigDecimal lateFee, BigDecimal totalAmount) {
        return buildTransaction(rental, rentalDays, baseAmount, lateFee, totalAmount);
    }

    private Transaction buildTransaction(Rental rental, long rentalDays, BigDecimal baseAmount,
            BigDecimal lateFee, BigDecimal totalAmount) {
        return Transaction.builder()
                .invoiceId(generateInvoiceId())
                .billType(BillType.RECEIPT)
                .dvdTitle(rental.getDvd().getTitle())
                .rentalPeriodDays((int) rentalDays)
                .pricePerDay(calculatePricePerDay(baseAmount, rentalDays))
                .lateFee(lateFee)
                .totalAmount(totalAmount)
                .generatedAt(LocalDateTime.now())
                .build();
    }

    private BigDecimal calculatePricePerDay(BigDecimal baseAmount, long rentalDays) {
        return baseAmount.divide(BigDecimal.valueOf(rentalDays));
    }

    private String generateInvoiceId() {
        return "INV-" + System.currentTimeMillis() + "-" +
                UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

}
