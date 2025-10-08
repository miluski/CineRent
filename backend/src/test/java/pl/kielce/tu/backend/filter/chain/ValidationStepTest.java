package pl.kielce.tu.backend.filter.chain;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Test;

import pl.kielce.tu.backend.filter.strategy.ValidationResult;

class ValidationStepTest {

    @Test
    void defaultExecutePassesNullAndReturnsResult() throws Exception {
        ValidationResult returned = mock(ValidationResult.class);
        AtomicReference<ValidationResult> captured = new AtomicReference<>();

        ValidationStep step = previous -> {
            captured.set(previous);
            return returned;
        };

        ValidationResult result = step.execute();

        assertNull(captured.get(), "default execute() should call execute(null)");
        assertSame(returned, result, "execute() should return value from implemented method");
    }

    @Test
    void executeForwardsPreviousResult() throws Exception {
        ValidationResult previous = mock(ValidationResult.class);
        ValidationResult returned = mock(ValidationResult.class);
        AtomicReference<ValidationResult> captured = new AtomicReference<>();

        ValidationStep step = prev -> {
            captured.set(prev);
            return returned;
        };

        ValidationResult result = step.execute(previous);

        assertSame(previous, captured.get(), "execute(previous) should forward the given previous result");
        assertSame(returned, result, "execute(previous) should return value from implemented method");
    }

    @Test
    void executePropagatesIOExceptionFromImplementation() {
        ValidationStep step = previous -> {
            throw new IOException("simulated");
        };

        assertThrows(IOException.class, step::execute,
                "execute() should propagate IOException thrown by implementation");
    }
}
