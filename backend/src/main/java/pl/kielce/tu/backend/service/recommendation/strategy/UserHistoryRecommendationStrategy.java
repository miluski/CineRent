package pl.kielce.tu.backend.service.recommendation.strategy;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import pl.kielce.tu.backend.model.constant.RecommendationReason;
import pl.kielce.tu.backend.model.constant.RentalStatus;
import pl.kielce.tu.backend.model.entity.Dvd;
import pl.kielce.tu.backend.model.entity.Genre;
import pl.kielce.tu.backend.model.entity.Rental;
import pl.kielce.tu.backend.model.entity.User;
import pl.kielce.tu.backend.repository.DvdRepository;
import pl.kielce.tu.backend.repository.RentalRepository;

@Component
@RequiredArgsConstructor
public class UserHistoryRecommendationStrategy extends BaseRecommendationStrategy {

    private final DvdRepository dvdRepository;
    private final RentalRepository rentalRepository;

    @Override
    public List<Dvd> executeRecommendation(User user) {
        List<Genre> userGenres = getUserRentedGenres(user);
        if (userGenres.isEmpty()) {
            return List.of();
        }

        var page = dvdRepository.findByGenresInAndAvalaibleTrue(userGenres, createPageable());
        return extractContent(page);
    }

    @Override
    public int getPriority() {
        return 1;
    }

    @Override
    public String getReason() {
        return RecommendationReason.USER_HISTORY.getMessage();
    }

    private List<Genre> getUserRentedGenres(User user) {
        List<Rental> userRentals = findUserInactiveRentals(user.getId());
        if (userRentals.isEmpty()) {
            return List.of();
        }

        return extractGenresFromRentals(userRentals);
    }

    private List<Rental> findUserInactiveRentals(Long userId) {
        var rentals = rentalRepository.findByUserIdAndStatusOrderByCreatedAtDesc(userId, RentalStatus.INACTIVE);
        return rentals != null ? rentals : List.of();
    }

    private List<Genre> extractGenresFromRentals(List<Rental> rentals) {
        return rentals.stream()
                .filter(this::hasValidDvdWithGenres)
                .flatMap(this::extractGenresFromRental)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
    }

    private boolean hasValidDvdWithGenres(Rental rental) {
        return rental != null
                && rental.getDvd() != null
                && rental.getDvd().getGenres() != null;
    }

    private java.util.stream.Stream<Genre> extractGenresFromRental(Rental rental) {
        return rental.getDvd().getGenres().stream();
    }

}
