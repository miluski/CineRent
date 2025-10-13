package pl.kielce.tu.backend.service.dvd.filter.strategy;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import pl.kielce.tu.backend.model.dto.DvdFilterDto;
import pl.kielce.tu.backend.model.entity.Dvd;
import pl.kielce.tu.backend.model.entity.Genre;
import pl.kielce.tu.backend.repository.GenreRepository;

@ExtendWith(MockitoExtension.class)
class GenreFilterStrategyTest {

    @Mock
    private GenreRepository genreRepository;

    @InjectMocks
    private GenreFilterStrategy genreFilterStrategy;

    @Test
    void shouldApplyFilter_whenFilterByGenreNames() {
        Genre actionGenre = createGenre(1L, "Action");
        Genre sciFiGenre = createGenre(2L, "Sci-Fi");

        Dvd dvd1 = createDvdWithGenres("Matrix", Arrays.asList(actionGenre, sciFiGenre));
        Dvd dvd2 = createDvdWithGenres("Romance Movie", Arrays.asList(createGenre(3L, "Romance")));
        List<Dvd> dvds = Arrays.asList(dvd1, dvd2);

        DvdFilterDto filterDto = DvdFilterDto.builder()
                .genreNames(Arrays.asList("Action"))
                .build();

        when(genreRepository.findByNameIn(Arrays.asList("Action")))
                .thenReturn(Arrays.asList(actionGenre));
        List<Dvd> result = genreFilterStrategy.applyFilter(dvds, filterDto);
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("Matrix");
    }

    @Test
    void shouldApplyFilter_whenFilterByGenreIds() {
        Genre actionGenre = createGenre(1L, "Action");
        Genre sciFiGenre = createGenre(2L, "Sci-Fi");

        Dvd dvd1 = createDvdWithGenres("Matrix", Arrays.asList(actionGenre, sciFiGenre));
        Dvd dvd2 = createDvdWithGenres("Romance Movie", Arrays.asList(createGenre(3L, "Romance")));
        List<Dvd> dvds = Arrays.asList(dvd1, dvd2);

        DvdFilterDto filterDto = DvdFilterDto.builder()
                .genreIds(Arrays.asList(1L))
                .build();

        when(genreRepository.findAllById(Arrays.asList(1L)))
                .thenReturn(Arrays.asList(actionGenre));
        List<Dvd> result = genreFilterStrategy.applyFilter(dvds, filterDto);
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("Matrix");
    }

    @Test
    void shouldApplyFilter_whenFilterByBothNamesAndIds() {
        Genre actionGenre = createGenre(1L, "Action");
        Genre sciFiGenre = createGenre(2L, "Sci-Fi");
        Genre comedyGenre = createGenre(4L, "Comedy");

        Dvd dvd1 = createDvdWithGenres("Matrix", Arrays.asList(actionGenre, sciFiGenre));
        Dvd dvd2 = createDvdWithGenres("Comedy Movie", Arrays.asList(comedyGenre));
        List<Dvd> dvds = Arrays.asList(dvd1, dvd2);

        DvdFilterDto filterDto = DvdFilterDto.builder()
                .genreNames(Arrays.asList("Action"))
                .genreIds(Arrays.asList(4L))
                .build();

        when(genreRepository.findByNameIn(Arrays.asList("Action")))
                .thenReturn(Arrays.asList(actionGenre));
        when(genreRepository.findAllById(Arrays.asList(4L)))
                .thenReturn(Arrays.asList(comedyGenre));
        List<Dvd> result = genreFilterStrategy.applyFilter(dvds, filterDto);
        assertThat(result).hasSize(2);
        assertThat(result).extracting(Dvd::getTitle).containsExactlyInAnyOrder("Matrix", "Comedy Movie");
    }

    @Test
    void shouldReturnTrue_whenCanApplyWithGenreNames() {
        DvdFilterDto filterDto = DvdFilterDto.builder()
                .genreNames(Arrays.asList("Action"))
                .build();
        boolean result = genreFilterStrategy.canApply(filterDto);
        assertThat(result).isTrue();
    }

    @Test
    void shouldReturnTrue_whenCanApplyWithGenreIds() {
        DvdFilterDto filterDto = DvdFilterDto.builder()
                .genreIds(Arrays.asList(1L))
                .build();
        boolean result = genreFilterStrategy.canApply(filterDto);
        assertThat(result).isTrue();
    }

    @Test
    void shouldReturnFalse_whenCannotApplyWithoutGenres() {
        DvdFilterDto filterDto = DvdFilterDto.builder().build();
        boolean result = genreFilterStrategy.canApply(filterDto);
        assertThat(result).isFalse();
    }

    private Genre createGenre(Long id, String name) {
        return Genre.builder()
                .id(id)
                .name(name)
                .build();
    }

    private Dvd createDvdWithGenres(String title, List<Genre> genres) {
        return Dvd.builder()
                .title(title)
                .genres(genres)
                .build();
    }

}
