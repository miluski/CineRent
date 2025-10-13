package pl.kielce.tu.backend.service.recommendation.strategy;

import java.util.List;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import pl.kielce.tu.backend.model.constant.RecommendationReason;
import pl.kielce.tu.backend.model.entity.Dvd;
import pl.kielce.tu.backend.model.entity.User;
import pl.kielce.tu.backend.repository.DvdRepository;

@Component
@RequiredArgsConstructor
public class PreferredGenreRecommendationStrategy extends BaseRecommendationStrategy {

    private final DvdRepository dvdRepository;

    @Override
    public List<Dvd> executeRecommendation(User user) {
        if (user.getPreferredGenres() == null || user.getPreferredGenres().isEmpty()) {
            return List.of();
        }
        var page = dvdRepository.findByPreferredGenresAndAvailable(user.getPreferredGenres(), createPageable());
        return extractContent(page);
    }

    @Override
    public int getPriority() {
        return 3;
    }

    @Override
    public String getReason() {
        return RecommendationReason.PREFERRED_GENRES.getMessage();
    }

}
