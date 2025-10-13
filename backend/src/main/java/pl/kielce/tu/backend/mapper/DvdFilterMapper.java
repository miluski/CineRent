package pl.kielce.tu.backend.mapper;

import java.util.List;

import org.springframework.stereotype.Component;

import pl.kielce.tu.backend.model.dto.DvdFilterDto;

@Component
public class DvdFilterMapper {

    public DvdFilterDto mapToFilterDto(String searchPhrase, List<String> genreNames, List<Long> genreIds) {
        return DvdFilterDto.builder()
                .searchPhrase(normalizeSearchPhrase(searchPhrase))
                .genreNames(normalizeGenreNames(genreNames))
                .genreIds(normalizeGenreIds(genreIds))
                .build();
    }

    public boolean hasAnyFilter(DvdFilterDto filterDto) {
        return hasSearchPhrase(filterDto) || hasGenreFilter(filterDto);
    }

    private String normalizeSearchPhrase(String searchPhrase) {
        return isBlankOrNull(searchPhrase) ? null : searchPhrase.trim();
    }

    private List<String> normalizeGenreNames(List<String> genreNames) {
        return isEmptyOrNull(genreNames) ? null : genreNames;
    }

    private List<Long> normalizeGenreIds(List<Long> genreIds) {
        return isEmptyOrNull(genreIds) ? null : genreIds;
    }

    private boolean hasSearchPhrase(DvdFilterDto filterDto) {
        return filterDto.getSearchPhrase() != null;
    }

    private boolean hasGenreFilter(DvdFilterDto filterDto) {
        return filterDto.getGenreNames() != null || filterDto.getGenreIds() != null;
    }

    private boolean isBlankOrNull(String value) {
        return value == null || value.trim().isEmpty();
    }

    private boolean isEmptyOrNull(List<?> list) {
        return list == null || list.isEmpty();
    }

}
