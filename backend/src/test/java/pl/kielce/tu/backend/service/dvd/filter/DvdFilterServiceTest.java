package pl.kielce.tu.backend.service.dvd.filter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import pl.kielce.tu.backend.model.dto.DvdFilterDto;
import pl.kielce.tu.backend.model.entity.Dvd;
import pl.kielce.tu.backend.service.dvd.filter.strategy.DvdFilterStrategy;

@ExtendWith(MockitoExtension.class)
public class DvdFilterServiceTest {

    @Mock
    private DvdFilterDto filterDto;

    @Mock
    private Dvd dvd1;

    @Mock
    private Dvd dvd2;

    @Test
    void shouldReturnSameListWhenNoStrategies() {
        List<DvdFilterStrategy> strategies = Collections.emptyList();
        DvdFilterService service = new DvdFilterService(strategies);

        List<Dvd> input = Arrays.asList(dvd1, dvd2);

        List<Dvd> result = service.applyFilters(input, filterDto);

        assertSame(input, result);
    }

    @Test
    void shouldApplySingleStrategyWhenApplicable() {
        DvdFilterStrategy strategy = mock(DvdFilterStrategy.class);
        when(strategy.canApply(filterDto)).thenReturn(true);

        List<Dvd> input = Arrays.asList(dvd1, dvd2);
        List<Dvd> filtered = Arrays.asList(dvd2);

        when(strategy.applyFilter(input, filterDto)).thenReturn(filtered);

        DvdFilterService service = new DvdFilterService(Arrays.asList(strategy));

        List<Dvd> result = service.applyFilters(input, filterDto);

        assertEquals(filtered, result);
        verify(strategy, times(1)).canApply(filterDto);
        verify(strategy, times(1)).applyFilter(input, filterDto);
    }

    @Test
    void shouldApplyOnlyApplicableStrategiesInOrder() {
        DvdFilterStrategy s1 = mock(DvdFilterStrategy.class);
        DvdFilterStrategy s2 = mock(DvdFilterStrategy.class);
        DvdFilterStrategy s3 = mock(DvdFilterStrategy.class);

        when(s1.canApply(filterDto)).thenReturn(true);
        when(s2.canApply(filterDto)).thenReturn(false);
        when(s3.canApply(filterDto)).thenReturn(true);

        List<Dvd> input = Arrays.asList(dvd1);
        List<Dvd> afterS1 = Arrays.asList(dvd1, dvd2);
        List<Dvd> afterS3 = Arrays.asList(dvd2);

        when(s1.applyFilter(input, filterDto)).thenReturn(afterS1);
        when(s3.applyFilter(afterS1, filterDto)).thenReturn(afterS3);

        DvdFilterService service = new DvdFilterService(Arrays.asList(s1, s2, s3));

        List<Dvd> result = service.applyFilters(input, filterDto);

        assertEquals(afterS3, result);

        InOrder inOrder = inOrder(s1, s2, s3);
        inOrder.verify(s1).canApply(filterDto);
        inOrder.verify(s1).applyFilter(input, filterDto);

        inOrder.verify(s2).canApply(filterDto);
        verify(s2, never()).applyFilter(anyList(), any());

        inOrder.verify(s3).canApply(filterDto);
        inOrder.verify(s3).applyFilter(afterS1, filterDto);
    }
}
