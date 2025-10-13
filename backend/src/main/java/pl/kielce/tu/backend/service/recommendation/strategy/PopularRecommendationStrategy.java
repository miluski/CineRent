package pl.kielce.tu.backend.service.recommendation.strategy;

import java.util.List;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import pl.kielce.tu.backend.model.constant.RecommendationReason;
import pl.kielce.tu.backend.model.entity.Dvd;
import pl.kielce.tu.backend.model.entity.User;
import pl.kielce.tu.backend.repository.DvdRepository;
import pl.kielce.tu.backend.repository.RentalRepository;

@Component
@RequiredArgsConstructor
public class PopularRecommendationStrategy extends BaseRecommendationStrategy {

    private final DvdRepository dvdRepository;
    private final RentalRepository rentalRepository;

    @Override
    public List<Dvd> executeRecommendation(User user) {
        long totalRentals = rentalRepository.count();
        if (totalRentals == 0) {
            return List.of();
        }

        var page = dvdRepository.findMostPopularAvailableDvds(createPageable());
        return extractContent(page);
    }

    @Override
    public int getPriority() {
        return 4;
    }

    @Override
    public String getReason() {
        return RecommendationReason.POPULAR.getMessage();
    }

}
