package pl.kielce.tu.backend.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import pl.kielce.tu.backend.model.dto.GenreDto;
import pl.kielce.tu.backend.model.entity.Genre;
import pl.kielce.tu.backend.repository.GenreRepository;

@ExtendWith(MockitoExtension.class)
class GenreMapperTest {

    @Mock
    private GenreRepository genreRepository;

    private GenreMapper service;

    @BeforeEach
    void setUp() {
        service = new GenreMapper(genreRepository);
    }

    @Test
    void mapGenresToNames_nullInput_returnsEmptyList() {
        List<String> result = service.mapGenresToNames(null);
        assertTrue(result.isEmpty());
    }

    @Test
    void mapGenresToNames_emptyInput_returnsEmptyList() {
        List<String> result = service.mapGenresToNames(Collections.emptyList());
        assertTrue(result.isEmpty());
    }

    @Test
    void mapGenresToNames_withGenres_returnsNames() {
        Genre g1 = mock(Genre.class);
        Genre g2 = mock(Genre.class);
        when(g1.getName()).thenReturn("Horror");
        when(g2.getName()).thenReturn("Comedy");

        List<String> result = service.mapGenresToNames(Arrays.asList(g1, g2));
        assertEquals(Arrays.asList("Horror", "Comedy"), result);
    }

    @Test
    void mapGenreIdsToGenres_nullInput_returnsEmptyList() {
        List<Genre> result = service.mapGenreIdsToGenres(null);
        assertTrue(result.isEmpty());
    }

    @Test
    void mapGenreIdsToGenres_emptyInput_returnsEmptyList() {
        List<Genre> result = service.mapGenreIdsToGenres(Collections.emptyList());
        assertTrue(result.isEmpty());
    }

    @Test
    void mapGenreIdsToGenres_withExistingAndMissingIds_returnsOnlyExisting() {
        Genre g1 = mock(Genre.class);
        Genre g2 = mock(Genre.class);

        when(genreRepository.findById(1L)).thenReturn(Optional.of(g1));
        when(genreRepository.findById(2L)).thenReturn(Optional.empty());
        when(genreRepository.findById(3L)).thenReturn(Optional.of(g2));

        List<Genre> result = service.mapGenreIdsToGenres(Arrays.asList(1L, 2L, 3L));
        assertEquals(Arrays.asList(g1, g2), result);
    }

    @Test
    void toGenre_withValidGenreDto_returnsGenre() {
        GenreDto genreDto = GenreDto.builder()
                .id(1L)
                .name("Science Fiction")
                .build();
        Genre result = service.toGenre(genreDto);
        assertEquals("Science Fiction", result.getName());
        assertEquals(null, result.getId());
    }

    @Test
    void toGenre_withNullName_returnsGenreWithNullName() {
        GenreDto genreDto = GenreDto.builder()
                .id(1L)
                .name(null)
                .build();
        Genre result = service.toGenre(genreDto);
        assertEquals(null, result.getName());
        assertEquals(null, result.getId());
    }

    @Test
    void toDto_withValidGenre_returnsGenreDto() {
        Genre genre = Genre.builder()
                .id(1L)
                .name("Horror")
                .build();
        GenreDto result = service.toDto(genre);
        assertEquals(1L, result.getId());
        assertEquals("Horror", result.getName());
    }

    @Test
    void toDto_withNullId_returnsGenreDtoWithNullId() {
        Genre genre = Genre.builder()
                .id(null)
                .name("Comedy")
                .build();
        GenreDto result = service.toDto(genre);
        assertEquals(null, result.getId());
        assertEquals("Comedy", result.getName());
    }

    @Test
    void toDto_withNullName_returnsGenreDtoWithNullName() {
        Genre genre = Genre.builder()
                .id(2L)
                .name(null)
                .build();
        GenreDto result = service.toDto(genre);
        assertEquals(2L, result.getId());
        assertEquals(null, result.getName());
    }
}
