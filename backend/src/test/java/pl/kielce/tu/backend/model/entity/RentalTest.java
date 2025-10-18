package pl.kielce.tu.backend.model.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

import pl.kielce.tu.backend.model.constant.RentalStatus;

class RentalTest {

    @Test
    void builderDefaultStatus() {
        Rental rental = Rental.builder().build();
        assertNotNull(rental);
        assertEquals(RentalStatus.ACTIVE, rental.getStatus());
    }

    @Test
    void gettersAndSetters() {
        Rental r = new Rental();
        LocalDateTime start = LocalDateTime.of(2025, 1, 1, 10, 0);
        LocalDateTime end = LocalDateTime.of(2025, 1, 10, 18, 0);
        LocalDateTime ret = LocalDateTime.of(2025, 1, 9, 16, 30);
        LocalDateTime created = LocalDateTime.of(2025, 1, 1, 12, 0);
        Integer count = 3;

        r.setId(10L);
        r.setRentalStart(start);
        r.setRentalEnd(end);
        r.setReturnDate(ret);
        r.setCreatedAt(created);
        r.setCount(count);
        r.setStatus(RentalStatus.ACTIVE);
        r.setDvd(null);
        r.setUser(null);
        r.setTransaction(null);

        assertEquals(Long.valueOf(10L), r.getId());
        assertEquals(start, r.getRentalStart());
        assertEquals(end, r.getRentalEnd());
        assertEquals(ret, r.getReturnDate());
        assertEquals(created, r.getCreatedAt());
        assertEquals(count, r.getCount());
        assertEquals(RentalStatus.ACTIVE, r.getStatus());
        assertNull(r.getDvd());
        assertNull(r.getUser());
        assertNull(r.getTransaction());
    }

    @Test
    void equalsAndHashCode_consistentForSameFieldValues() {
        LocalDateTime start = LocalDateTime.of(2025, 2, 1, 10, 0);
        LocalDateTime end = LocalDateTime.of(2025, 2, 5, 18, 0);
        LocalDateTime created = LocalDateTime.of(2025, 2, 1, 10, 0);

        Rental a = Rental.builder()
                .id(1L)
                .rentalStart(start)
                .rentalEnd(end)
                .returnDate(null)
                .createdAt(created)
                .count(1)
                .status(RentalStatus.ACTIVE)
                .dvd(null)
                .user(null)
                .transaction(null)
                .build();

        Rental b = Rental.builder()
                .id(1L)
                .rentalStart(start)
                .rentalEnd(end)
                .returnDate(null)
                .createdAt(created)
                .count(1)
                .status(RentalStatus.ACTIVE)
                .dvd(null)
                .user(null)
                .transaction(null)
                .build();

        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }
}
