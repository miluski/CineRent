package pl.kielce.tu.backend.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

import pl.kielce.tu.backend.model.dto.RentalDto;
import pl.kielce.tu.backend.model.entity.Dvd;
import pl.kielce.tu.backend.model.entity.Rental;

public class RentalMapperTest {

    private final RentalMapper mapper = new RentalMapper();

    @Test
    void toDto_nullRental_returnsNull() {
        RentalDto dto = mapper.toDto(null);
        assertNull(dto);
    }

    @Test
    void toDto_withDvd_mapsFields() {
        Dvd dvd = new Dvd();
        dvd.setId(20L);
        dvd.setTitle("Some Movie");

        Rental rental = new Rental();
        rental.setId(10L);
        rental.setDvd(dvd);
        rental.setCount(3);

        RentalDto dto = mapper.toDto(rental);

        assertEquals(10L, dto.getId());
        assertEquals(20L, dto.getDvdId());
        assertEquals("Some Movie", dto.getDvdTitle());
        assertEquals(3, dto.getCount());
    }

    @Test
    void toDto_withNullDvd_mapsNullDvdFields() {
        Rental rental = new Rental();
        rental.setId(11L);
        rental.setDvd(null);

        RentalDto dto = mapper.toDto(rental);

        assertEquals(11L, dto.getId());
        assertNull(dto.getDvdId());
        assertNull(dto.getDvdTitle());
    }

    @Test
    void toDtoList_nullList_returnsNull() {
        List<RentalDto> dtos = mapper.toDtoList(null);
        assertNull(dtos);
    }

    @Test
    void toDtoList_mapsAllElements() {
        Dvd dvd1 = new Dvd();
        dvd1.setId(100L);
        dvd1.setTitle("A");

        Rental r1 = new Rental();
        r1.setId(1L);
        r1.setDvd(dvd1);

        Dvd dvd2 = new Dvd();
        dvd2.setId(200L);
        dvd2.setTitle("B");

        Rental r2 = new Rental();
        r2.setId(2L);
        r2.setDvd(dvd2);

        List<RentalDto> dtos = mapper.toDtoList(Arrays.asList(r1, r2));

        assertEquals(2, dtos.size());
        assertEquals(1L, dtos.get(0).getId());
        assertEquals(100L, dtos.get(0).getDvdId());
        assertEquals(2L, dtos.get(1).getId());
        assertEquals(200L, dtos.get(1).getDvdId());
    }
}
