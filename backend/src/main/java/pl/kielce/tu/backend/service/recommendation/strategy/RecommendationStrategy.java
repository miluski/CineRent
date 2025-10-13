package pl.kielce.tu.backend.service.recommendation.strategy;

import java.util.List;

import pl.kielce.tu.backend.model.entity.Dvd;
import pl.kielce.tu.backend.model.entity.User;
import pl.kielce.tu.backend.util.UserContextLogger;

public interface RecommendationStrategy {

    List<Dvd> recommend(User user, UserContextLogger logger);

    int getPriority();

    String getReason();
}
