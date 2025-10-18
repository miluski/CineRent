package pl.kielce.tu.backend.service.rental.factory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import pl.kielce.tu.backend.model.constant.BillType;
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
        LocalDateTime rentalStart = LocalDateTime.now();
        LocalDateTime rentalEnd = rentalStart.plusDays(7);

        Mockito.when(reservation.getUser()).thenReturn(user);
        Mockito.when(reservation.getDvd()).thenReturn(dvd);
        Mockito.when(reservation.getCount()).thenReturn(3);
        Mockito.when(reservation.getRentalStart()).thenReturn(rentalStart);
        Mockito.when(reservation.getRentalEnd()).thenReturn(rentalEnd);
        Mockito.when(dvd.getRentalPricePerDay()).thenReturn(2.50f);
        Mockito.when(dvd.getTitle()).thenReturn("Test Movie");

        RentalFactory factory = new RentalFactory();
        Rental rental = factory.createFromReservation(reservation);

        assertSame(user, rental.getUser(), "User should be propagated from reservation");
        assertSame(dvd, rental.getDvd(), "Dvd should be propagated from reservation");
        assertEquals(3, rental.getCount(), "Count should be propagated from reservation");
        assertEquals(RentalStatus.ACTIVE, rental.getStatus(), "Status should be ACTIVE");
        assertNotNull(rental.getCreatedAt(), "createdAt should be set");
        assertEquals(rentalStart, rental.getRentalStart(), "rentalStart should be from reservation");
        assertEquals(rentalEnd, rental.getRentalEnd(), "rentalEnd should be from reservation");

        assertNotNull(rental.getTransaction(), "Transaction should be created");
        assertNotNull(rental.getTransaction().getInvoiceId(), "Invoice ID should be generated");
        assertEquals("Test Movie", rental.getTransaction().getDvdTitle(), "DVD title should match");
        assertEquals(7, rental.getTransaction().getRentalPeriodDays(), "Rental period should be 7 days");
        assertTrue(rental.getTransaction().getInvoiceId().startsWith("INV-"), "Invoice ID should start with INV-");
    }

    @Test
    void createFromReservation_handlesDifferentCounts() {
        Reservation reservation = Mockito.mock(Reservation.class);
        User user = Mockito.mock(User.class);
        Dvd dvd = Mockito.mock(Dvd.class);
        LocalDateTime rentalStart = LocalDateTime.now();
        LocalDateTime rentalEnd = rentalStart.plusDays(5);

        Mockito.when(reservation.getUser()).thenReturn(user);
        Mockito.when(reservation.getDvd()).thenReturn(dvd);
        Mockito.when(reservation.getCount()).thenReturn(1);
        Mockito.when(reservation.getRentalStart()).thenReturn(rentalStart);
        Mockito.when(reservation.getRentalEnd()).thenReturn(rentalEnd);
        Mockito.when(dvd.getRentalPricePerDay()).thenReturn(3.99f);
        Mockito.when(dvd.getTitle()).thenReturn("Another Movie");

        RentalFactory factory = new RentalFactory();
        Rental rental = factory.createFromReservation(reservation);
        assertEquals(1, rental.getCount());
        assertEquals(RentalStatus.ACTIVE, rental.getStatus());
        assertEquals(rentalStart, rental.getRentalStart());
        assertEquals(rentalEnd, rental.getRentalEnd());
    }

    @Test
    void createFromReservation_createsTransactionWithCorrectCalculations() {
        Reservation reservation = Mockito.mock(Reservation.class);
        User user = Mockito.mock(User.class);
        Dvd dvd = Mockito.mock(Dvd.class);
        LocalDateTime rentalStart = LocalDateTime.now();
        LocalDateTime rentalEnd = rentalStart.plusDays(10);

        Mockito.when(reservation.getUser()).thenReturn(user);
        Mockito.when(reservation.getDvd()).thenReturn(dvd);
        Mockito.when(reservation.getCount()).thenReturn(2);
        Mockito.when(reservation.getRentalStart()).thenReturn(rentalStart);
        Mockito.when(reservation.getRentalEnd()).thenReturn(rentalEnd);
        Mockito.when(dvd.getRentalPricePerDay()).thenReturn(5.00f);
        Mockito.when(dvd.getTitle()).thenReturn("Expensive Movie");

        RentalFactory factory = new RentalFactory();
        Rental rental = factory.createFromReservation(reservation);

        assertNotNull(rental.getTransaction());
        assertEquals("Expensive Movie", rental.getTransaction().getDvdTitle());
        assertEquals(10, rental.getTransaction().getRentalPeriodDays());
        assertEquals(0, rental.getTransaction().getLateFee().compareTo(BigDecimal.ZERO));
        assertNotNull(rental.getTransaction().getGeneratedAt());

        BigDecimal expectedTotal = BigDecimal.valueOf(5.00)
                .multiply(BigDecimal.valueOf(10))
                .multiply(BigDecimal.valueOf(2));
        assertEquals(0, expectedTotal.compareTo(rental.getTransaction().getTotalAmount()));
    }

    @Test
    void createFromReservation_setsDefaultBillType() {
        Reservation reservation = Mockito.mock(Reservation.class);
        User user = Mockito.mock(User.class);
        Dvd dvd = Mockito.mock(Dvd.class);
        LocalDateTime rentalStart = LocalDateTime.now();
        LocalDateTime rentalEnd = rentalStart.plusDays(3);

        Mockito.when(reservation.getUser()).thenReturn(user);
        Mockito.when(reservation.getDvd()).thenReturn(dvd);
        Mockito.when(reservation.getCount()).thenReturn(1);
        Mockito.when(reservation.getRentalStart()).thenReturn(rentalStart);
        Mockito.when(reservation.getRentalEnd()).thenReturn(rentalEnd);
        Mockito.when(dvd.getRentalPricePerDay()).thenReturn(1.99f);
        Mockito.when(dvd.getTitle()).thenReturn("Budget Movie");

        RentalFactory factory = new RentalFactory();
        Rental rental = factory.createFromReservation(reservation);

        assertNotNull(rental.getTransaction());
        assertEquals(BillType.INVOICE,
                rental.getTransaction().getBillType());
    }

    @Test
    void createFromReservation_generatesUniqueInvoiceIds() {
        Reservation reservation = Mockito.mock(Reservation.class);
        User user = Mockito.mock(User.class);
        Dvd dvd = Mockito.mock(Dvd.class);
        LocalDateTime rentalStart = LocalDateTime.now();
        LocalDateTime rentalEnd = rentalStart.plusDays(14);

        Mockito.when(reservation.getUser()).thenReturn(user);
        Mockito.when(reservation.getDvd()).thenReturn(dvd);
        Mockito.when(reservation.getCount()).thenReturn(1);
        Mockito.when(reservation.getRentalStart()).thenReturn(rentalStart);
        Mockito.when(reservation.getRentalEnd()).thenReturn(rentalEnd);
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

    @Test
    void createFromReservation_handlesSameDayRental() {
        Reservation reservation = Mockito.mock(Reservation.class);
        User user = Mockito.mock(User.class);
        Dvd dvd = Mockito.mock(Dvd.class);
        LocalDateTime rentalStart = LocalDateTime.now();
        LocalDateTime rentalEnd = rentalStart;

        Mockito.when(reservation.getUser()).thenReturn(user);
        Mockito.when(reservation.getDvd()).thenReturn(dvd);
        Mockito.when(reservation.getCount()).thenReturn(2);
        Mockito.when(reservation.getRentalStart()).thenReturn(rentalStart);
        Mockito.when(reservation.getRentalEnd()).thenReturn(rentalEnd);
        Mockito.when(dvd.getRentalPricePerDay()).thenReturn(10.00f);
        Mockito.when(dvd.getTitle()).thenReturn("Same Day Movie");

        RentalFactory factory = new RentalFactory();
        Rental rental = factory.createFromReservation(reservation);

        assertNotNull(rental.getTransaction());
        assertEquals(0, rental.getTransaction().getRentalPeriodDays(),
                "Same-time rental should be 0 days");
        BigDecimal expectedTotal = BigDecimal.valueOf(10.00)
                .multiply(BigDecimal.valueOf(0))
                .multiply(BigDecimal.valueOf(2));
        assertEquals(0, expectedTotal.compareTo(rental.getTransaction().getTotalAmount()),
                "Total amount should be 0 for same-time rental");
    }
}
