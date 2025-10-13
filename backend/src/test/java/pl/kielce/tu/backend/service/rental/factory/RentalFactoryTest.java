package pl.kielce.tu.backend.service.rental.factory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.Date;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import pl.kielce.tu.backend.model.constant.CalculationConstants;
import pl.kielce.tu.backend.model.constant.RentalStatus;
import pl.kielce.tu.backend.model.entity.Dvd;
import pl.kielce.tu.backend.model.entity.Rental;
import pl.kielce.tu.backend.model.entity.Reservation;
import pl.kielce.tu.backend.model.entity.User;

class RentalFactoryTest {

    @Test
    void createFromReservation_setsExpectedFields() {
        Reservation reservation = Mockito.mock(Reservation.class);
        User user = Mockito.mock(User.class);
        Dvd dvd = Mockito.mock(Dvd.class);
        Mockito.when(reservation.getUser()).thenReturn(user);
        Mockito.when(reservation.getDvd()).thenReturn(dvd);
        Mockito.when(reservation.getCount()).thenReturn(3);
        Mockito.when(dvd.getRentalPricePerDay()).thenReturn(2.50f);
        Mockito.when(dvd.getTitle()).thenReturn("Test Movie");

        RentalFactory factory = new RentalFactory();

        long before = System.currentTimeMillis();
        Rental rental = factory.createFromReservation(reservation);

        long after = System.currentTimeMillis();
        assertSame(user, rental.getUser(), "User should be propagated from reservation");
        assertSame(dvd, rental.getDvd(), "Dvd should be propagated from reservation");
        assertEquals(3, rental.getCount(), "Count should be propagated from reservation");
        assertEquals(RentalStatus.ACTIVE, rental.getStatus(), "Status should be ACTIVE");
        assertNotNull(rental.getCreatedAt(), "createdAt should be set");
        Date start = rental.getRentalStart();
        assertNotNull(start, "rentalStart should be set");
        assertTrue(start.getTime() >= before && start.getTime() <= after,
                "rentalStart should be between before and after timestamps");

        long rentalDays = CalculationConstants.RENTAL_PERIOD_DAYS.getValue().longValue();
        long expectedEndMillis = start.getTime() + rentalDays * 24L * 60L * 60L * 1000L;
        assertEquals(expectedEndMillis, rental.getRentalEnd().getTime(), "rentalEnd should be start + rental period");

        assertNotNull(rental.getTransaction(), "Transaction should be created");
        assertNotNull(rental.getTransaction().getInvoiceId(), "Invoice ID should be generated");
        assertEquals("Test Movie", rental.getTransaction().getDvdTitle(), "DVD title should match");
        assertTrue(rental.getTransaction().getInvoiceId().startsWith("INV-"), "Invoice ID should start with INV-");
    }

    @Test
    void createFromReservation_handlesDifferentCounts() {
        Reservation reservation = Mockito.mock(Reservation.class);
        User user = Mockito.mock(User.class);
        Dvd dvd = Mockito.mock(Dvd.class);
        Mockito.when(reservation.getUser()).thenReturn(user);
        Mockito.when(reservation.getDvd()).thenReturn(dvd);
        Mockito.when(reservation.getCount()).thenReturn(1);
        Mockito.when(dvd.getRentalPricePerDay()).thenReturn(3.99f);
        Mockito.when(dvd.getTitle()).thenReturn("Another Movie");

        RentalFactory factory = new RentalFactory();
        Rental rental = factory.createFromReservation(reservation);
        assertEquals(1, rental.getCount());
        assertEquals(RentalStatus.ACTIVE, rental.getStatus());
        assertNotNull(rental.getRentalStart());
        assertNotNull(rental.getRentalEnd());
    }

    @Test
    void createFromReservation_createsTransactionWithCorrectCalculations() {
        Reservation reservation = Mockito.mock(Reservation.class);
        User user = Mockito.mock(User.class);
        Dvd dvd = Mockito.mock(Dvd.class);
        Mockito.when(reservation.getUser()).thenReturn(user);
        Mockito.when(reservation.getDvd()).thenReturn(dvd);
        Mockito.when(reservation.getCount()).thenReturn(2);
        Mockito.when(dvd.getRentalPricePerDay()).thenReturn(5.00f);
        Mockito.when(dvd.getTitle()).thenReturn("Expensive Movie");

        RentalFactory factory = new RentalFactory();
        Rental rental = factory.createFromReservation(reservation);

        assertNotNull(rental.getTransaction());
        assertEquals("Expensive Movie", rental.getTransaction().getDvdTitle());
        assertEquals(CalculationConstants.RENTAL_PERIOD_DAYS.getValue().intValue(),
                rental.getTransaction().getRentalPeriodDays());
        assertEquals(0, rental.getTransaction().getLateFee().compareTo(java.math.BigDecimal.ZERO));
        assertNotNull(rental.getTransaction().getGeneratedAt());

        java.math.BigDecimal expectedTotal = java.math.BigDecimal.valueOf(5.00)
                .multiply(CalculationConstants.RENTAL_PERIOD_DAYS.getValue())
                .multiply(java.math.BigDecimal.valueOf(2));
        assertEquals(0, expectedTotal.compareTo(rental.getTransaction().getTotalAmount()));
    }

    @Test
    void createFromReservation_setsDefaultBillType() {
        Reservation reservation = Mockito.mock(Reservation.class);
        User user = Mockito.mock(User.class);
        Dvd dvd = Mockito.mock(Dvd.class);
        Mockito.when(reservation.getUser()).thenReturn(user);
        Mockito.when(reservation.getDvd()).thenReturn(dvd);
        Mockito.when(reservation.getCount()).thenReturn(1);
        Mockito.when(dvd.getRentalPricePerDay()).thenReturn(1.99f);
        Mockito.when(dvd.getTitle()).thenReturn("Budget Movie");

        RentalFactory factory = new RentalFactory();
        Rental rental = factory.createFromReservation(reservation);

        assertNotNull(rental.getTransaction());
        assertEquals(pl.kielce.tu.backend.model.constant.BillType.INVOICE,
                rental.getTransaction().getBillType());
    }

    @Test
    void createFromReservation_generatesUniqueInvoiceIds() {
        Reservation reservation = Mockito.mock(Reservation.class);
        User user = Mockito.mock(User.class);
        Dvd dvd = Mockito.mock(Dvd.class);
        Mockito.when(reservation.getUser()).thenReturn(user);
        Mockito.when(reservation.getDvd()).thenReturn(dvd);
        Mockito.when(reservation.getCount()).thenReturn(1);
        Mockito.when(dvd.getRentalPricePerDay()).thenReturn(2.00f);
        Mockito.when(dvd.getTitle()).thenReturn("Test Movie");

        RentalFactory factory = new RentalFactory();
        Rental rental1 = factory.createFromReservation(reservation);
        Rental rental2 = factory.createFromReservation(reservation);

        assertNotNull(rental1.getTransaction().getInvoiceId());
        assertNotNull(rental2.getTransaction().getInvoiceId());
        assertTrue(!rental1.getTransaction().getInvoiceId().equals(rental2.getTransaction().getInvoiceId()),
                "Invoice IDs should be unique");
    }
}
