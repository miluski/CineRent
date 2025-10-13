package pl.kielce.tu.backend.service.dvd.filter.strategy;

import java.util.List;

import org.springframework.stereotype.Component;

import pl.kielce.tu.backend.model.dto.DvdFilterDto;
import pl.kielce.tu.backend.model.entity.Dvd;

@Component
public class SearchPhraseFilterStrategy implements DvdFilterStrategy {

    @Override
    public List<Dvd> applyFilter(List<Dvd> dvds, DvdFilterDto filterDto) {
        String searchPhrase = filterDto.getSearchPhrase().toLowerCase();
        return dvds.stream()
                .filter(dvd -> matchesSearchPhrase(dvd, searchPhrase))
                .toList();
    }

    @Override
    public boolean canApply(DvdFilterDto filterDto) {
        return filterDto.getSearchPhrase() != null;
    }

    private boolean matchesSearchPhrase(Dvd dvd, String searchPhrase) {
        return matchesInTitle(dvd, searchPhrase) || matchesInDescription(dvd, searchPhrase);
    }

    private boolean matchesInTitle(Dvd dvd, String searchPhrase) {
        return dvd.getTitle().toLowerCase().contains(searchPhrase);
    }

    private boolean matchesInDescription(Dvd dvd, String searchPhrase) {
        return dvd.getDescription().toLowerCase().contains(searchPhrase);
    }

}
