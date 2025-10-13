package pl.kielce.tu.backend.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import pl.kielce.tu.backend.model.dto.DvdFilterDto;

@ExtendWith(MockitoExtension.class)
class DvdFilterMapperTest {

    @InjectMocks
    private DvdFilterMapper dvdFilterMapper;

    @Test
    void shouldMapToFilterDto_whenAllParametersProvided() {
        String searchPhrase = "matrix";
        List<String> genreNames = Arrays.asList("Action", "Sci-Fi");
        List<Long> genreIds = Arrays.asList(1L, 2L);
        DvdFilterDto result = dvdFilterMapper.mapToFilterDto(searchPhrase, genreNames, genreIds);
        assertThat(result.getSearchPhrase()).isEqualTo("matrix");
        assertThat(result.getGenreNames()).containsExactly("Action", "Sci-Fi");
        assertThat(result.getGenreIds()).containsExactly(1L, 2L);
    }

    @Test
    void shouldMapToFilterDto_whenSearchPhraseIsBlank() {
        String searchPhrase = "   ";
        List<String> genreNames = Arrays.asList("Action");
        List<Long> genreIds = Arrays.asList(1L);
        DvdFilterDto result = dvdFilterMapper.mapToFilterDto(searchPhrase, genreNames, genreIds);
        assertThat(result.getSearchPhrase()).isNull();
        assertThat(result.getGenreNames()).containsExactly("Action");
        assertThat(result.getGenreIds()).containsExactly(1L);
    }

    @Test
    void shouldMapToFilterDto_whenListsAreEmpty() {
        String searchPhrase = "test";
        List<String> genreNames = Collections.emptyList();
        List<Long> genreIds = Collections.emptyList();
        DvdFilterDto result = dvdFilterMapper.mapToFilterDto(searchPhrase, genreNames, genreIds);
        assertThat(result.getSearchPhrase()).isEqualTo("test");
        assertThat(result.getGenreNames()).isNull();
        assertThat(result.getGenreIds()).isNull();
    }

    @Test
    void shouldReturnTrue_whenHasSearchPhrase() {
        DvdFilterDto filterDto = DvdFilterDto.builder()
                .searchPhrase("test")
                .build();
        boolean result = dvdFilterMapper.hasAnyFilter(filterDto);
        assertThat(result).isTrue();
    }

    @Test
    void shouldReturnTrue_whenHasGenreNames() {
        DvdFilterDto filterDto = DvdFilterDto.builder()
                .genreNames(Arrays.asList("Action"))
                .build();
        boolean result = dvdFilterMapper.hasAnyFilter(filterDto);
        assertThat(result).isTrue();
    }

    @Test
    void shouldReturnTrue_whenHasGenreIds() {
        DvdFilterDto filterDto = DvdFilterDto.builder()
                .genreIds(Arrays.asList(1L))
                .build();
        boolean result = dvdFilterMapper.hasAnyFilter(filterDto);
        assertThat(result).isTrue();
    }

    @Test
    void shouldReturnFalse_whenNoFilters() {
        DvdFilterDto filterDto = DvdFilterDto.builder().build();
        boolean result = dvdFilterMapper.hasAnyFilter(filterDto);
        assertThat(result).isFalse();
    }

}
