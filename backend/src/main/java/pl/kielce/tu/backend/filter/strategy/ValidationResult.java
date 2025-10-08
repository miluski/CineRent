package pl.kielce.tu.backend.filter.strategy;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ValidationResult {

    private final boolean success;
    private final Object data;

}
