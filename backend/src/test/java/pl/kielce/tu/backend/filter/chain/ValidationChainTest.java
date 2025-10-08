package pl.kielce.tu.backend.filter.chain;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import pl.kielce.tu.backend.filter.strategy.ValidationResult;

@ExtendWith(MockitoExtension.class)
public class ValidationChainTest {

    @Mock
    ValidationStep step1;

    @Mock
    ValidationStep step2;

    @Mock
    ValidationResult successResult;

    @Mock
    ValidationResult failureResult;

    @Test
    void execute_withNoSteps_returnsNull() throws IOException {
        ValidationChain chain = new ValidationChain(Collections.emptyList());
        assertNull(chain.execute());
    }

    @Test
    void execute_singleStep_successful_returnsThatResult() throws IOException {
        when(step1.execute()).thenReturn(successResult);
        when(successResult.isSuccess()).thenReturn(true);

        ValidationChain chain = new ValidationChain(List.of(step1));
        ValidationResult result = chain.execute();

        assertSame(successResult, result);
        verify(step1).execute();
        verifyNoMoreInteractions(step1);
    }

    @Test
    void execute_stopsWhenStepFails_andDoesNotCallFollowingSteps() throws IOException {
        when(step1.execute()).thenReturn(failureResult);
        when(failureResult.isSuccess()).thenReturn(false);

        ValidationChain chain = new ValidationChain(List.of(step1, step2));
        ValidationResult result = chain.execute();

        assertSame(failureResult, result);
        verify(step1).execute();
        verifyNoInteractions(step2);
    }

    @Test
    void execute_passesPreviousResultToNextStep() throws IOException {
        when(step1.execute()).thenReturn(successResult);
        when(successResult.isSuccess()).thenReturn(true);
        when(step2.execute(successResult)).thenReturn(successResult);
        when(successResult.isSuccess()).thenReturn(true);

        ValidationChain chain = new ValidationChain(List.of(step1, step2));
        ValidationResult result = chain.execute();

        assertSame(successResult, result);
        verify(step1).execute();
        verify(step2).execute(successResult);
    }
}
