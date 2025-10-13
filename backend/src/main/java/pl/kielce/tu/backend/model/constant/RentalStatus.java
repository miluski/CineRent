package pl.kielce.tu.backend.model.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum RentalStatus {
    ACTIVE("ACTIVE"),
    RETURN_REQUESTED("RETURN_REQUESTED"),
    INACTIVE("INACTIVE");

    private final String value;
}
