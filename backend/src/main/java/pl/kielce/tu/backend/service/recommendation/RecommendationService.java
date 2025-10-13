package pl.kielce.tu.backend.service.recommendation;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import pl.kielce.tu.backend.extractor.ClaimsExtractor;
import pl.kielce.tu.backend.mapper.RecommendationMapper;
import pl.kielce.tu.backend.model.constant.CookieNames;
import pl.kielce.tu.backend.model.dto.DvdDto;
import pl.kielce.tu.backend.model.entity.Dvd;
import pl.kielce.tu.backend.model.entity.User;
import pl.kielce.tu.backend.repository.UserRepository;
import pl.kielce.tu.backend.service.auth.CookieService;
import pl.kielce.tu.backend.service.recommendation.strategy.RecommendationStrategy;
import pl.kielce.tu.backend.util.UserContextLogger;

@Service
@RequiredArgsConstructor
public class RecommendationService {

    @Value("${jwt.secret}")
    private String jwtSecret;

    private final CookieService cookieService;
    private final UserRepository userRepository;
    private final ClaimsExtractor claimsExtractor;
    private final UserContextLogger userContextLogger;
    private final List<RecommendationStrategy> strategies;
    private final RecommendationMapper recommendationMapper;

    public ResponseEntity<List<DvdDto>> handleGetDvdRecommendations(HttpServletRequest request) {
        try {
            Long userId = extractUserIdFromRequest(request);
            User user = findUserById(userId);
            List<DvdDto> recommendations = generateRecommendations(user);
            return ResponseEntity.status(HttpStatus.OK).body(recommendations);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    private Long extractUserIdFromRequest(HttpServletRequest request) {
        String token = cookieService.getTokenFromCookie(request, CookieNames.ACCESS_TOKEN);
        if (token == null) {
            throw new RuntimeException("Authentication token not found");
        }
        return claimsExtractor.extractUserId(token, jwtSecret);
    }

    private User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
    }

    private List<DvdDto> generateRecommendations(User user) {
        List<DvdDto> recommendations = new ArrayList<>();
        for (RecommendationStrategy strategy : getSortedStrategies()) {
            if (hasReachedRecommendationLimit(recommendations)) {
                break;
            }
            processStrategy(strategy, user, recommendations);
        }
        return limitRecommendations(recommendations);
    }

    private boolean hasReachedRecommendationLimit(List<DvdDto> recommendations) {
        return recommendations.size() >= 20;
    }

    private void processStrategy(RecommendationStrategy strategy, User user, List<DvdDto> recommendations) {
        try {
            List<Dvd> dvds = strategy.recommend(user, userContextLogger);
            if (hasValidResults(dvds)) {
                addStrategyRecommendations(dvds, strategy.getReason(), recommendations);
            }
        } catch (Exception e) {
            logStrategyError(strategy, e);
        }
    }

    private boolean hasValidResults(List<Dvd> dvds) {
        return dvds != null && !dvds.isEmpty();
    }

    private void addStrategyRecommendations(List<Dvd> dvds, String reason, List<DvdDto> recommendations) {
        List<DvdDto> strategyRecommendations = recommendationMapper.mapToRecommendationDtos(dvds, reason,
                userContextLogger);
        if (strategyRecommendations != null) {
            addUniqueRecommendations(strategyRecommendations, recommendations);
        }
    }

    private void addUniqueRecommendations(List<DvdDto> newRecommendations, List<DvdDto> existingRecommendations) {
        List<Long> existingIds = extractExistingIds(existingRecommendations);
        List<DvdDto> uniqueRecommendations = filterUniqueRecommendations(newRecommendations, existingIds);
        existingRecommendations.addAll(uniqueRecommendations);
    }

    private List<Long> extractExistingIds(List<DvdDto> existingRecommendations) {
        return existingRecommendations.stream()
                .map(DvdDto::getId)
                .collect(Collectors.toList());
    }

    private List<DvdDto> filterUniqueRecommendations(List<DvdDto> newRecommendations, List<Long> existingIds) {
        return newRecommendations.stream()
                .filter(dto -> !existingIds.contains(dto.getId()))
                .collect(Collectors.toList());
    }

    private void logStrategyError(RecommendationStrategy strategy, Exception e) {
        userContextLogger.logUserOperation("STRATEGY_ERROR",
                "Strategy " + strategy.getClass().getSimpleName() + " failed: " + e.getMessage());
    }

    private List<DvdDto> limitRecommendations(List<DvdDto> recommendations) {
        return recommendations.stream()
                .limit(20)
                .collect(Collectors.toList());
    }

    private List<RecommendationStrategy> getSortedStrategies() {
        return strategies.stream()
                .sorted(Comparator.comparingInt(RecommendationStrategy::getPriority))
                .collect(Collectors.toList());
    }

}
