package pl.kielce.tu.backend.service.rental.factory;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.stereotype.Component;

import pl.kielce.tu.backend.model.constant.BillType;
import pl.kielce.tu.backend.model.constant.RentalStatus;
import pl.kielce.tu.backend.model.entity.Dvd;
import pl.kielce.tu.backend.model.entity.Rental;
import pl.kielce.tu.backend.model.entity.Reservation;
import pl.kielce.tu.backend.model.entity.Transaction;
import pl.kielce.tu.backend.model.entity.User;

@Component
public class RentalFactory {

    public Rental createFromReservation(Reservation reservation) {
        User user = reservation.getUser();
        Dvd dvd = reservation.getDvd();
        LocalDateTime rentalStart = reservation.getRentalStart();
        LocalDateTime rentalEnd = reservation.getRentalEnd();
        int count = reservation.getCount();

        return buildRentalFromReservation(user, dvd, count, rentalStart, rentalEnd);
    }

    private Rental buildRentalFromReservation(User user, Dvd dvd, int count, LocalDateTime rentalStart,
            LocalDateTime rentalEnd) {
        Transaction transaction = createTransaction(dvd, count, rentalStart, rentalEnd);
        return Rental.builder()
                .user(user)
                .dvd(dvd)
                .count(count)
                .status(RentalStatus.ACTIVE)
                .rentalStart(rentalStart)
                .rentalEnd(rentalEnd)
                .createdAt(LocalDateTime.now())
                .transaction(transaction)
                .build();
    }

    private Transaction createTransaction(Dvd dvd, int count, LocalDateTime rentalStart, LocalDateTime rentalEnd) {
        BigDecimal pricePerDay = BigDecimal.valueOf(dvd.getRentalPricePerDay());
        int rentalPeriodDays = calculateRentalPeriodDays(rentalStart, rentalEnd);
        BigDecimal totalAmount = pricePerDay.multiply(BigDecimal.valueOf(rentalPeriodDays * count));

        return Transaction.builder()
                .invoiceId(generateInvoiceId())
                .dvdTitle(dvd.getTitle())
                .rentalPeriodDays(rentalPeriodDays)
                .pricePerDay(pricePerDay)
                .lateFee(BigDecimal.ZERO)
                .totalAmount(totalAmount)
                .generatedAt(LocalDateTime.now())
                .billType(BillType.INVOICE)
                .build();
    }

    private int calculateRentalPeriodDays(LocalDateTime startDate, LocalDateTime endDate) {
        long diffInHours = java.time.Duration.between(startDate, endDate).toHours();
        int days = (int) Math.ceil(diffInHours / 24.0);
        return Math.max(0, days);
    }

    private String generateInvoiceId() {
        return "INV-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

}
