package pl.kielce.tu.backend.model.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

import pl.kielce.tu.backend.model.constant.ReservationStatus;

class ReservationTest {

    @Test
    void builderShouldSetDefaultStatusToPending() {
        Reservation reservation = Reservation.builder().build();
        assertEquals(ReservationStatus.PENDING, reservation.getStatus());
    }

    @Test
    void onCreateShouldSetCreatedAtWhenNull() {
        Reservation reservation = Reservation.builder().createdAt(null).build();

        LocalDateTime before = LocalDateTime.now();
        reservation.onCreate();
        LocalDateTime after = LocalDateTime.now();

        LocalDateTime created = reservation.getCreatedAt();
        assertNotNull(created, "createdAt should be set by onCreate()");
        assertFalse(created.isBefore(before), "createdAt should not be before the time just before onCreate()");
        assertFalse(created.isAfter(after), "createdAt should not be after the time just after onCreate()");
    }

    @Test
    void onCreateShouldNotOverrideExistingCreatedAt() {
        LocalDateTime fixed = LocalDateTime.of(2020, 1, 1, 12, 0);
        Reservation reservation = Reservation.builder().createdAt(fixed).build();

        reservation.onCreate();

        assertEquals(fixed, reservation.getCreatedAt(), "onCreate() should not override an already set createdAt");
    }
}
