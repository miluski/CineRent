package pl.kielce.tu.backend.service.rental.transaction;

import java.math.BigDecimal;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import pl.kielce.tu.backend.mapper.TransactionMapper;
import pl.kielce.tu.backend.model.entity.Rental;
import pl.kielce.tu.backend.model.entity.Transaction;
import pl.kielce.tu.backend.service.rental.calculation.LateFeeCalculationService;
import pl.kielce.tu.backend.service.rental.calculation.RentalCalculationService;
import pl.kielce.tu.backend.util.UserContextLogger;

@Component
@RequiredArgsConstructor
public class TransactionGeneratorService {

    private final TransactionMapper transactionMapper;
    private final UserContextLogger userContextLogger;
    private final LateFeeCalculationService lateFeeService;
    private final RentalCalculationService calculationService;

    public Transaction generateTransaction(Rental rental) {
        userContextLogger.logUserOperation("TRANSACTION_GENERATION_STARTED",
                "Rental ID: " + rental.getId());
        long rentalDays = calculationService.calculateRentalDays(rental);
        BigDecimal baseAmount = calculationService.calculateBaseAmount(rental, rentalDays);
        BigDecimal lateFee = lateFeeService.calculateLateFee(rental);
        BigDecimal totalAmount = calculationService.calculateTotalAmount(baseAmount, lateFee);
        Transaction transaction = transactionMapper.toEntity(rental, rentalDays, baseAmount, lateFee, totalAmount);
        userContextLogger.logUserOperation("TRANSACTION_GENERATION_COMPLETED",
                String.format("Rental ID: %d, Total: %s, Late Fee: %s, Days: %d",
                        rental.getId(), totalAmount, lateFee, rentalDays));
        return transaction;
    }

}
