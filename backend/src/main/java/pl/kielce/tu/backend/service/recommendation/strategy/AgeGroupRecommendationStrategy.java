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
public class AgeGroupRecommendationStrategy extends BaseRecommendationStrategy {

    private final DvdRepository dvdRepository;
    private final RentalRepository rentalRepository;

    @Override
    public List<Dvd> executeRecommendation(User user) {
        if (user.getAge() == null) {
            return List.of();
        }
        return findPopularDvdsInAgeGroup(user);
    }

    @Override
    public int getPriority() {
        return 2;
    }

    @Override
    public String getReason() {
        return RecommendationReason.AGE_GROUP.getMessage();
    }

    private List<Dvd> findPopularDvdsInAgeGroup(User user) {
        long totalRentals = rentalRepository.count();
        if (totalRentals == 0) {
            return List.of();
        }
        Integer userAge = user.getAge();
        Integer minAge = userAge - 5;
        Integer maxAge = userAge + 5;
        var page = dvdRepository.findMostPopularDvdsByAgeGroup(minAge, maxAge, createPageable());
        return extractContent(page);
    }

}
