package pl.kielce.tu.backend.mapper;

import org.springframework.stereotype.Component;

import pl.kielce.tu.backend.model.constant.FilterConstants;
import pl.kielce.tu.backend.model.constant.RentalStatus;

@Component
public class RentalFilterMapper {

    public RentalStatus mapFilterToStatus(String filter) {
        if (filter == null) {
            return null;
        }

        if (isHistoricalFilter(filter)) {
            return RentalStatus.INACTIVE;
        }

        return parseEnumStatus(filter);
    }

    public Long parseRentalId(String id) {
        validateIdFormat(id);
        return Long.parseLong(id);
    }

    private boolean isHistoricalFilter(String filter) {
        return FilterConstants.HISTORICAL.getValue().equalsIgnoreCase(filter);
    }

    private RentalStatus parseEnumStatus(String filter) {
        try {
            return RentalStatus.valueOf(filter.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private void validateIdFormat(String id) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("Rental ID cannot be null or empty");
        }

        try {
            Long.parseLong(id);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid rental ID format");
        }
    }
}
