package pl.kielce.tu.backend.service.dvd.filter.strategy;

import java.util.List;

import pl.kielce.tu.backend.model.dto.DvdFilterDto;
import pl.kielce.tu.backend.model.entity.Dvd;

public interface DvdFilterStrategy {

    List<Dvd> applyFilter(List<Dvd> dvds, DvdFilterDto filterDto);

    boolean canApply(DvdFilterDto filterDto);

}
