package pl.kielce.tu.backend.mapper;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Component;

import pl.kielce.tu.backend.model.constant.BillType;
import pl.kielce.tu.backend.model.dto.TransactionDto;
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

    public TransactionDto toDto(Rental rental) {
        if (rental.getTransaction() == null) {
            return null;
        }
        return buildTransactionDto(rental);
    }

    public List<TransactionDto> toDtoList(List<Rental> rentals) {
        return rentals.stream()
                .filter(rental -> rental.getTransaction() != null)
                .map(this::toDto)
                .toList();
    }

    private TransactionDto buildTransactionDto(Rental rental) {
        Transaction transaction = rental.getTransaction();
        return TransactionDto.builder()
                .id(rental.getId())
                .invoiceId(transaction.getInvoiceId())
                .dvdTitle(transaction.getDvdTitle())
                .rentalPeriodDays(transaction.getRentalPeriodDays())
                .pricePerDay(transaction.getPricePerDay())
                .lateFee(transaction.getLateFee())
                .totalAmount(transaction.getTotalAmount())
                .generatedAt(transaction.getGeneratedAt())
                .pdfUrl(transaction.getPdfUrl())
                .billType(transaction.getBillType())
                .rentalId(rental.getId())
                .build();
    }

    private String generateInvoiceId() {
        return "INV-" + System.currentTimeMillis() + "-" +
                UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

}
