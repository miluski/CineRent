package pl.kielce.tu.backend.service.rental.factory;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.stereotype.Component;

import pl.kielce.tu.backend.model.constant.BillType;
import pl.kielce.tu.backend.model.constant.CalculationConstants;
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

        return buildRental(user, dvd, reservation.getCount());
    }

    private Rental buildRental(User user, Dvd dvd, int count) {
        Date currentDate = new Date(System.currentTimeMillis());
        Date endDate = calculateEndDate(currentDate);
        Transaction transaction = createTransaction(dvd, count);

        return Rental.builder()
                .user(user)
                .dvd(dvd)
                .count(count)
                .status(RentalStatus.ACTIVE)
                .rentalStart(currentDate)
                .rentalEnd(endDate)
                .createdAt(LocalDateTime.now())
                .transaction(transaction)
                .build();
    }

    private Transaction createTransaction(Dvd dvd, int count) {
        BigDecimal pricePerDay = BigDecimal.valueOf(dvd.getRentalPricePerDay());
        int rentalPeriodDays = CalculationConstants.RENTAL_PERIOD_DAYS.getValue().intValue();
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

    private String generateInvoiceId() {
        return "INV-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private Date calculateEndDate(Date startDate) {
        long rentalDays = CalculationConstants.RENTAL_PERIOD_DAYS.getValue().longValue();
        long rentalPeriodMillis = rentalDays * 24L * 60L * 60L * 1000L;
        return new Date(startDate.getTime() + rentalPeriodMillis);
    }

}
