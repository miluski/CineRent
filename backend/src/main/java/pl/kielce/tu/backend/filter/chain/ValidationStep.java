package pl.kielce.tu.backend.filter.chain;

import java.io.IOException;

import pl.kielce.tu.backend.filter.strategy.ValidationResult;

@FunctionalInterface
public interface ValidationStep {

    ValidationResult execute(ValidationResult previousResult) throws IOException;

    default ValidationResult execute() throws IOException {
        return execute(null);
    }
}
