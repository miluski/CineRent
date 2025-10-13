package pl.kielce.tu.backend.service.dvd.filter.strategy;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import pl.kielce.tu.backend.model.dto.DvdFilterDto;
import pl.kielce.tu.backend.model.entity.Dvd;

@ExtendWith(MockitoExtension.class)
class SearchPhraseFilterStrategyTest {

    @InjectMocks
    private SearchPhraseFilterStrategy searchPhraseFilterStrategy;

    @Test
    void shouldApplyFilter_whenSearchPhraseMatchesTitle() {
        Dvd dvd1 = createDvd("Matrix", "A computer hacker learns about reality");
        Dvd dvd2 = createDvd("Inception", "A thief who steals corporate secrets");
        List<Dvd> dvds = Arrays.asList(dvd1, dvd2);

        DvdFilterDto filterDto = DvdFilterDto.builder()
                .searchPhrase("matrix")
                .build();
        List<Dvd> result = searchPhraseFilterStrategy.applyFilter(dvds, filterDto);
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("Matrix");
    }

    @Test
    void shouldApplyFilter_whenSearchPhraseMatchesDescription() {
        Dvd dvd1 = createDvd("Matrix", "A computer hacker learns about reality");
        Dvd dvd2 = createDvd("Inception", "A thief who steals corporate secrets");
        List<Dvd> dvds = Arrays.asList(dvd1, dvd2);

        DvdFilterDto filterDto = DvdFilterDto.builder()
                .searchPhrase("thief")
                .build();
        List<Dvd> result = searchPhraseFilterStrategy.applyFilter(dvds, filterDto);
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("Inception");
    }

    @Test
    void shouldApplyFilter_whenSearchPhraseCaseInsensitive() {
        Dvd dvd1 = createDvd("Matrix", "A computer hacker learns about reality");
        List<Dvd> dvds = Arrays.asList(dvd1);

        DvdFilterDto filterDto = DvdFilterDto.builder()
                .searchPhrase("MATRIX")
                .build();
        List<Dvd> result = searchPhraseFilterStrategy.applyFilter(dvds, filterDto);
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("Matrix");
    }

    @Test
    void shouldReturnEmpty_whenNoMatchFound() {
        Dvd dvd1 = createDvd("Matrix", "A computer hacker learns about reality");
        List<Dvd> dvds = Arrays.asList(dvd1);

        DvdFilterDto filterDto = DvdFilterDto.builder()
                .searchPhrase("avatar")
                .build();
        List<Dvd> result = searchPhraseFilterStrategy.applyFilter(dvds, filterDto);
        assertThat(result).isEmpty();
    }

    @Test
    void shouldReturnTrue_whenCanApplyWithSearchPhrase() {
        DvdFilterDto filterDto = DvdFilterDto.builder()
                .searchPhrase("test")
                .build();
        boolean result = searchPhraseFilterStrategy.canApply(filterDto);
        assertThat(result).isTrue();
    }

    @Test
    void shouldReturnFalse_whenCannotApplyWithoutSearchPhrase() {
        DvdFilterDto filterDto = DvdFilterDto.builder().build();
        boolean result = searchPhraseFilterStrategy.canApply(filterDto);
        assertThat(result).isFalse();
    }

    private Dvd createDvd(String title, String description) {
        return Dvd.builder()
                .title(title)
                .description(description)
                .build();
    }

}
