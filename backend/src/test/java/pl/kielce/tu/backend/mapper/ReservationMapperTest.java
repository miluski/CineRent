package pl.kielce.tu.backend.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

import pl.kielce.tu.backend.model.dto.ReservationDto;
import pl.kielce.tu.backend.model.entity.Dvd;
import pl.kielce.tu.backend.model.entity.Reservation;

class ReservationMapperTest {

    private final ReservationMapper mapper = new ReservationMapper();

    @Test
    void toDto_nullReservation_returnsNull() {
        assertNull(mapper.toDto(null));
    }

    @Test
    void toDto_mapsAllFieldsCorrectly() {
        Reservation reservation = new Reservation();
        reservation.setId(1L);
        LocalDateTime start = LocalDateTime.of(2023, 1, 2, 10, 0);
        LocalDateTime end = LocalDateTime.of(2023, 1, 5, 18, 0);
        LocalDateTime created = LocalDateTime.of(2022, 12, 31, 12, 0);
        reservation.setRentalStart(start);
        reservation.setRentalEnd(end);
        reservation.setCreatedAt(created);
        Dvd dvd = new Dvd();
        dvd.setId(2L);
        dvd.setTitle("Example Movie");
        reservation.setDvd(dvd);
        reservation.setCount(3);
        reservation.setStatus(null);

        ReservationDto dto = mapper.toDto(reservation);

        assertNotNull(dto);
        assertEquals(reservation.getId(), dto.getId());
        assertEquals(reservation.getRentalStart(), dto.getRentalStart());
        assertEquals(reservation.getRentalEnd(), dto.getRentalEnd());
        assertEquals(reservation.getCreatedAt(), dto.getCreatedAt());
        assertEquals(dvd.getId(), dto.getDvdId());
        assertEquals(dvd.getTitle(), dto.getDvdTitle());
        assertEquals(reservation.getCount(), dto.getCount());
        assertNull(dto.getStatus());
    }

    @Test
    void toDto_handlesNullDvdGracefully() {
        Reservation reservation = new Reservation();
        reservation.setId(5L);
        reservation.setDvd(null);
        reservation.setCount(1);

        ReservationDto dto = mapper.toDto(reservation);

        assertNotNull(dto);
        assertEquals(reservation.getId(), dto.getId());
        assertNull(dto.getDvdId());
        assertNull(dto.getDvdTitle());
        assertEquals(reservation.getCount(), dto.getCount());
    }

    @Test
    void toDtoList_nullList_returnsNull() {
        assertNull(mapper.toDtoList(null));
    }

    @Test
    void toDtoList_mapsListCorrectly() {
        Reservation r1 = new Reservation();
        r1.setId(10L);
        Dvd d1 = new Dvd();
        d1.setId(11L);
        d1.setTitle("A");
        r1.setDvd(d1);

        Reservation r2 = new Reservation();
        r2.setId(20L);
        Dvd d2 = new Dvd();
        d2.setId(21L);
        d2.setTitle("B");
        r2.setDvd(d2);

        List<Reservation> input = Arrays.asList(r1, r2);
        List<ReservationDto> result = mapper.toDtoList(input);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(r1.getId(), result.get(0).getId());
        assertEquals(d1.getId(), result.get(0).getDvdId());
        assertEquals(d1.getTitle(), result.get(0).getDvdTitle());
        assertEquals(r2.getId(), result.get(1).getId());
        assertEquals(d2.getId(), result.get(1).getDvdId());
        assertEquals(d2.getTitle(), result.get(1).getDvdTitle());
    }
}
