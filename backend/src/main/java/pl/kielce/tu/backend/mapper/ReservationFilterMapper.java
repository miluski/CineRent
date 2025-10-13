package pl.kielce.tu.backend.mapper;

import org.springframework.stereotype.Component;

import pl.kielce.tu.backend.model.constant.ReservationStatus;

@Component
public class ReservationFilterMapper {

    public ReservationStatus mapFilterToStatus(String filter) {
        if (filter == null) {
            return null;
        }

        return parseEnumStatus(filter);
    }

    public Long parseReservationId(String id) {
        validateIdFormat(id);
        return Long.parseLong(id);
    }

    private ReservationStatus parseEnumStatus(String filter) {
        try {
            return ReservationStatus.valueOf(filter.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private void validateIdFormat(String id) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("Reservation ID cannot be null or empty");
        }

        try {
            Long.parseLong(id);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid reservation ID format");
        }
    }
}
