package pl.kielce.tu.backend.service.dvd.filter;

import java.util.List;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import pl.kielce.tu.backend.model.dto.DvdFilterDto;
import pl.kielce.tu.backend.model.entity.Dvd;
import pl.kielce.tu.backend.service.dvd.filter.strategy.DvdFilterStrategy;

@Service
@RequiredArgsConstructor
public class DvdFilterService {

    private final List<DvdFilterStrategy> filterStrategies;

    public List<Dvd> applyFilters(List<Dvd> dvds, DvdFilterDto filterDto) {
        return filterStrategies.stream()
                .filter(strategy -> strategy.canApply(filterDto))
                .reduce(dvds, (filteredDvds, strategy) -> strategy.applyFilter(filteredDvds, filterDto),
                        (list1, list2) -> list2);
    }

}
