package pl.kielce.tu.backend.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import pl.kielce.tu.backend.model.constant.DvdStatuses;
import pl.kielce.tu.backend.model.dto.DvdDto;
import pl.kielce.tu.backend.model.entity.Dvd;
import pl.kielce.tu.backend.model.entity.Genre;

@ExtendWith(MockitoExtension.class)
class DvdMapperTest {

    @Mock
    private GenreMapper genreMappingService;

    @InjectMocks
    private DvdMapper dvdMapper;

    @Test
    void toDto_null_returnsNull() {
        assertNull(dvdMapper.toDto(null));
    }

    @Test
    void toDto_mapsFieldsAndDeterminesStatus_available() {
        Dvd dvd = Dvd.builder()
                .id(123L)
                .title("My Movie")
                .genres(Collections.emptyList())
                .avalaible(true)
                .copiesAvalaible(3)
                .build();

        when(genreMappingService.mapGenresToNames(dvd.getGenres())).thenReturn(Arrays.asList("Action"));

        DvdDto dto = dvdMapper.toDto(dvd);

        assertNotNull(dto);
        assertEquals(123L, dto.getId());
        assertEquals("My Movie", dto.getTitle());
        assertEquals(Arrays.asList("Action"), dto.getGenres());
        assertEquals(DvdStatuses.AVALAIBLE.getValue(), dto.getStatus());
    }

    @Test
    void toDto_statusUnavailable_whenNoCopiesOrNotAvailable() {
        Dvd dvdNoCopies = Dvd.builder()
                .id(1L)
                .title("No Copies")
                .genres(Collections.emptyList())
                .avalaible(true)
                .copiesAvalaible(0)
                .build();

        when(genreMappingService.mapGenresToNames(Collections.emptyList())).thenReturn(Collections.emptyList());

        DvdDto dto = dvdMapper.toDto(dvdNoCopies);
        assertEquals(DvdStatuses.UNAVALAIBLE.getValue(), dto.getStatus());

        Dvd dvdNotAvailable = Dvd.builder()
                .id(2L)
                .title("Not Available")
                .genres(Collections.emptyList())
                .avalaible(false)
                .copiesAvalaible(5)
                .build();

        DvdDto dto2 = dvdMapper.toDto(dvdNotAvailable);
        assertEquals(DvdStatuses.UNAVALAIBLE.getValue(), dto2.getStatus());
    }

    @Test
    void toEnhancedDto_null_returnsNull() {
        assertNull(dvdMapper.toEnhancedDto(null));
    }

    @Test
    void toEnhancedDto_mapsAllFields() {
        LocalDateTime now = LocalDateTime.now();
        Dvd dvd = Dvd.builder()
                .id(50L)
                .title("Enhanced")
                .genres(Collections.emptyList())
                .releaseYear(1999)
                .directors(Arrays.asList("Director A"))
                .description("Desc")
                .durationMinutes(120)
                .avalaible(true)
                .copiesAvalaible(2)
                .rentalPricePerDay(2.50f)
                .posterUrl("http://img")
                .addedAt(now)
                .build();

        when(genreMappingService.mapGenresToNames(dvd.getGenres())).thenReturn(Arrays.asList("Drama"));

        DvdDto dto = dvdMapper.toEnhancedDto(dvd);

        assertNotNull(dto);
        assertEquals(50L, dto.getId());
        assertEquals("Enhanced", dto.getTitle());
        assertEquals(Arrays.asList("Drama"), dto.getGenres());
        assertEquals(1999, dto.getReleaseYear());
        assertEquals(Arrays.asList("Director A"), dto.getDirectors());
        assertEquals("Desc", dto.getDescription());
        assertEquals(120, dto.getDurationMinutes());
        assertTrue(dto.getAvailable());
        assertEquals(2, dto.getCopiesAvailable());
        assertEquals(2.50f, dto.getRentalPricePerDay());
        assertEquals("http://img", dto.getPosterUrl());
        assertEquals(now, dto.getAddedAt());
    }

    @Test
    void toDvd_null_returnsNull() {
        assertNull(dvdMapper.toDvd(null));
    }

    @Test
    void toDvd_mapsFieldsAndCallsGenreMapping() {
        Genre action = Genre.builder().id(1L).name("Action").build();
        Genre drama = Genre.builder().id(2L).name("Drama").build();

        DvdDto dto = DvdDto.builder()
                .id(77L)
                .title("FromDto")
                .genresIdentifiers(Arrays.asList(1L, 2L))
                .releaseYear(2001)
                .directors(Arrays.asList("Dir"))
                .description("desc")
                .durationMinutes(90)
                .available(true)
                .copiesAvailable(4)
                .rentalPricePerDay(3.00f)
                .posterUrl("url")
                .addedAt(LocalDateTime.of(2020, 1, 1, 0, 0))
                .build();

        when(genreMappingService.mapGenreIdsToGenres(dto.getGenresIdentifiers()))
                .thenReturn(Arrays.asList(action, drama));

        Dvd dvd = dvdMapper.toDvd(dto);

        assertNotNull(dvd);
        assertEquals(77L, dvd.getId());
        assertEquals("FromDto", dvd.getTitle());
        assertNotNull(dvd.getGenres());
        assertEquals(2, dvd.getGenres().size());
        assertEquals("Action", dvd.getGenres().get(0).getName());
        assertEquals("Drama", dvd.getGenres().get(1).getName());
        assertEquals(2001, dvd.getReleaseYear());
        assertEquals(Arrays.asList("Dir"), dvd.getDirectors());
        assertEquals("desc", dvd.getDescription());
        assertEquals(90, dvd.getDurationMinutes());
        assertTrue(dvd.getAvalaible());
        assertEquals(4, dvd.getCopiesAvalaible());
        assertEquals(3.00f, dvd.getRentalPricePerDay());
        assertEquals("url", dvd.getPosterUrl());
        assertEquals(LocalDateTime.of(2020, 1, 1, 0, 0), dvd.getAddedAt());
    }
}
