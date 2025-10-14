package pl.kielce.tu.backend.service.invoice.factory;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import pl.kielce.tu.backend.model.constant.BillType;
import pl.kielce.tu.backend.service.invoice.strategy.BillPdfStrategy;

class BillPdfStrategyFactoryTest {

    @Test
    void getStrategy_singleStrategy_returnsIt() {
        BillPdfStrategy strategy = Mockito.mock(BillPdfStrategy.class);
        BillType type = BillType.values()[0];
        Mockito.when(strategy.getSupportedBillType()).thenReturn(type);

        BillPdfStrategyFactory factory = new BillPdfStrategyFactory(List.of(strategy));
        BillPdfStrategy result = factory.getStrategy(type);

        assertSame(strategy, result);
    }

    @Test
    void getStrategy_duplicateSupportedBillType_throwsException() {
        BillPdfStrategy s1 = Mockito.mock(BillPdfStrategy.class);
        BillPdfStrategy s2 = Mockito.mock(BillPdfStrategy.class);
        BillType type = BillType.values()[0];
        Mockito.when(s1.getSupportedBillType()).thenReturn(type);
        Mockito.when(s2.getSupportedBillType()).thenReturn(type);

        BillPdfStrategyFactory factory = new BillPdfStrategyFactory(List.of(s1, s2));

        assertThrows(IllegalStateException.class, () -> factory.getStrategy(type));
    }
}


