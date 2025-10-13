package pl.kielce.tu.backend.model.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ReservationStatus {
    PENDING("PENDING"),
    CANCELLED("CANCELLED"),
    ACCEPTED("ACCEPTED"),
    REJECTED("REJECTED");

    private final String value;
}
