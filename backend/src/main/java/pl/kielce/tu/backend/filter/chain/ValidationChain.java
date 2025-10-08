package pl.kielce.tu.backend.filter.chain;

import java.io.IOException;
import java.util.List;

import pl.kielce.tu.backend.filter.strategy.ValidationResult;

public class ValidationChain {
    private final List<ValidationStep> steps;

    public ValidationChain(List<ValidationStep> steps) {
        this.steps = steps;
    }

    public ValidationResult execute() throws IOException {
        ValidationResult result = null;
        for (ValidationStep step : steps) {
            result = (result == null) ? step.execute() : step.execute(result);
            if (!result.isSuccess()) {
                return result;
            }
        }
        return result;
    }

}
