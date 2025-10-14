package pl.kielce.tu.backend.service.invoice.factory;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import pl.kielce.tu.backend.model.constant.BillType;
import pl.kielce.tu.backend.service.invoice.strategy.BillPdfStrategy;

@Component
@RequiredArgsConstructor
public class BillPdfStrategyFactory {

    private final List<BillPdfStrategy> strategies;

    public BillPdfStrategy getStrategy(BillType billType) {
        return getStrategyMap().get(billType);
    }

    private Map<BillType, BillPdfStrategy> getStrategyMap() {
        return strategies.stream()
                .collect(Collectors.toMap(
                        BillPdfStrategy::getSupportedBillType,
                        Function.identity()));
    }
    
}
