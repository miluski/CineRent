package pl.kielce.tu.backend.model.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum DvdStatuses {

    AVALAIBLE("AVALAIBLE"),
    UNAVALAIBLE("UNAVALAIBLE");

    private final String value;
}
