package pl.kielce.tu.backend.service.dvd;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import pl.kielce.tu.backend.exception.ValidationException;
import pl.kielce.tu.backend.mapper.DvdMapper;
import pl.kielce.tu.backend.model.dto.DvdDto;
import pl.kielce.tu.backend.model.entity.Dvd;
import pl.kielce.tu.backend.service.resource.ResourceService;

@ExtendWith(MockitoExtension.class)
class DvdUpdateServiceTest {

    @Mock
    private DvdMapper dvdMapper;

    @Mock
    private ResourceService resourceService;

    @Mock
    private DvdValidationService validationService;

    @Mock
    private DvdDto dvdDto;

    @InjectMocks
    private DvdUpdateService dvdUpdateService;

    @Test
    void applyUpdates_appliesAllProvidedFields() throws Exception {
        Dvd dvd = new Dvd();
        when(dvdDto.getTitle()).thenReturn("New Title");
        when(dvdDto.getReleaseYear()).thenReturn(2021);
        when(dvdDto.getDescription()).thenReturn("New description");
        when(dvdDto.getDurationMinutes()).thenReturn(130);
        when(dvdDto.getCopiesAvailable()).thenReturn(5);
        Float price = 4.99f;
        when(dvdDto.getRentalPricePerDay()).thenReturn(price);
        when(dvdDto.getAvailable()).thenReturn(Boolean.TRUE);

        List<Long> genresIds = List.of(1L);
        when(dvdDto.getGenresIdentifiers()).thenReturn(genresIds);

        List<String> directors = List.of("Dir A");
        when(dvdDto.getDirectors()).thenReturn(directors);

        when(dvdDto.getPosterImage()).thenReturn("poster-data");

        Dvd tempDvd = new Dvd();
        tempDvd.setGenres(Collections.emptyList());

        when(dvdMapper.toDvd(any(DvdDto.class))).thenReturn(tempDvd);
        when(resourceService.savePosterImage("poster-data")).thenReturn("saved.png");
        when(resourceService.generatePosterUrl("saved.png")).thenReturn("http://cdn/saved.png");

        dvdUpdateService.applyUpdates(dvd, dvdDto);

        assertEquals("New Title", dvd.getTitle());
        assertEquals(2021, dvd.getReleaseYear());
        assertEquals("New description", dvd.getDescription());
        assertEquals(130, dvd.getDurationMinutes());
        assertEquals(5, dvd.getCopiesAvalaible());
        assertEquals(price, dvd.getRentalPricePerDay());
        assertTrue(dvd.getAvalaible());
        assertSame(tempDvd.getGenres(), dvd.getGenres());
        assertSame(directors, dvd.getDirectors());
        assertEquals("http://cdn/saved.png", dvd.getPosterUrl());
        verify(validationService).validateTitle("New Title");
        verify(validationService).validateYear(2021);
        verify(validationService).validateDescription("New description");
        verify(validationService).validateDuration(130);
        verify(validationService).validateCopies(5);
        verify(validationService).validatePrice(price);
        verify(validationService).validateGenres(genresIds);
        verify(validationService).validateDirectors(directors);
        verify(resourceService).savePosterImage("poster-data");
        verify(resourceService).generatePosterUrl("saved.png");
    }

    @Test
    void applyUpdates_ignoresNullsAndDoesNotCallValidationsOrResources() throws Exception {
        Dvd dvd = new Dvd();
        when(dvdDto.getTitle()).thenReturn(null);
        when(dvdDto.getReleaseYear()).thenReturn(null);
        when(dvdDto.getDescription()).thenReturn(null);
        when(dvdDto.getDurationMinutes()).thenReturn(null);
        when(dvdDto.getCopiesAvailable()).thenReturn(null);
        when(dvdDto.getRentalPricePerDay()).thenReturn(null);
        when(dvdDto.getAvailable()).thenReturn(null);
        when(dvdDto.getGenresIdentifiers()).thenReturn(null);
        when(dvdDto.getDirectors()).thenReturn(null);
        when(dvdDto.getPosterImage()).thenReturn(null);
        dvdUpdateService.applyUpdates(dvd, dvdDto);
        verifyNoInteractions(validationService);
        verifyNoInteractions(resourceService);
        assertNull(dvd.getTitle());
        assertNull(dvd.getDescription());
        assertNull(dvd.getPosterUrl());
    }

    @Test
    void applyUpdates_propagatesValidationException() throws Exception {
        Dvd dvd = new Dvd();
        when(dvdDto.getTitle()).thenReturn("bad title");
        doThrow(new ValidationException("invalid")).when(validationService).validateTitle("bad title");
        ValidationException ex = assertThrows(ValidationException.class,
                () -> dvdUpdateService.applyUpdates(dvd, dvdDto));
        assertEquals("invalid", ex.getMessage());
        verifyNoInteractions(resourceService);
    }

}
