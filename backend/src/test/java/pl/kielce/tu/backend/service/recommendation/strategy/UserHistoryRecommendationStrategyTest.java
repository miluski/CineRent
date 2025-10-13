package pl.kielce.tu.backend.service.recommendation.strategy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import pl.kielce.tu.backend.model.constant.RentalStatus;
import pl.kielce.tu.backend.model.entity.Dvd;
import pl.kielce.tu.backend.model.entity.Genre;
import pl.kielce.tu.backend.model.entity.Rental;
import pl.kielce.tu.backend.model.entity.User;
import pl.kielce.tu.backend.repository.DvdRepository;
import pl.kielce.tu.backend.repository.RentalRepository;
import pl.kielce.tu.backend.util.UserContextLogger;

@ExtendWith(MockitoExtension.class)
class UserHistoryRecommendationStrategyTest {

    @Mock
    private DvdRepository dvdRepository;

    @Mock
    private RentalRepository rentalRepository;

    @Mock
    private UserContextLogger userContextLogger;

    private UserHistoryRecommendationStrategy strategy;

    @BeforeEach
    void setUp() {
        strategy = new UserHistoryRecommendationStrategy(dvdRepository, rentalRepository);
    }

    @Test
    void shouldRecommend_returnDvdsBasedOnUserHistory_whenUserHasRentalHistory() {
        User user = createTestUser();
        List<Rental> userRentals = createTestRentals();
        List<Genre> userGenres = extractGenresFromRentals(userRentals);
        List<Dvd> expectedDvds = createTestDvds();
        Page<Dvd> page = new PageImpl<>(expectedDvds);

        when(rentalRepository.findByUserIdAndStatusOrderByCreatedAtDesc(eq(user.getId()), eq(RentalStatus.INACTIVE)))
                .thenReturn(userRentals);
        when(dvdRepository.findByGenresInAndAvalaibleTrue(eq(userGenres), any(Pageable.class)))
                .thenReturn(page);
        List<Dvd> result = strategy.recommend(user, userContextLogger);

        assertEquals(2, result.size());
        assertEquals(expectedDvds, result);
    }

    @Test
    void shouldRecommend_returnEmptyList_whenUserHasNoRentalHistory() {
        User user = createTestUser();

        when(rentalRepository.findByUserIdAndStatusOrderByCreatedAtDesc(eq(user.getId()), eq(RentalStatus.INACTIVE)))
                .thenReturn(Collections.emptyList());
        List<Dvd> result = strategy.recommend(user, userContextLogger);
        assertTrue(result.isEmpty());
    }

    @Test
    void shouldRecommend_returnEmptyList_whenNoDvdsMatchUserGenres() {
        User user = createTestUser();
        List<Rental> userRentals = createTestRentals();
        List<Genre> userGenres = extractGenresFromRentals(userRentals);
        Page<Dvd> emptyPage = new PageImpl<>(Collections.emptyList());

        when(rentalRepository.findByUserIdAndStatusOrderByCreatedAtDesc(eq(user.getId()), eq(RentalStatus.INACTIVE)))
                .thenReturn(userRentals);
        when(dvdRepository.findByGenresInAndAvalaibleTrue(eq(userGenres), any(Pageable.class)))
                .thenReturn(emptyPage);
        List<Dvd> result = strategy.recommend(user, userContextLogger);
        assertTrue(result.isEmpty());
    }

    @Test
    void shouldGetPriority_returnCorrectValue() {
        int priority = strategy.getPriority();
        assertEquals(1, priority);
    }

    @Test
    void shouldGetReason_returnCorrectMessage() {
        String reason = strategy.getReason();
        assertEquals("Rekomendacje bazowane na podstawie Twojej aktywności", reason);
    }

    private User createTestUser() {
        User user = new User();
        user.setId(1L);
        user.setAge(30);
        return user;
    }

    private List<Rental> createTestRentals() {
        Genre action = new Genre();
        action.setId(1L);
        action.setName("Action");

        Genre drama = new Genre();
        drama.setId(2L);
        drama.setName("Drama");

        Dvd dvd1 = new Dvd();
        dvd1.setId(1L);
        dvd1.setTitle("Action Movie");
        dvd1.setGenres(Arrays.asList(action));

        Dvd dvd2 = new Dvd();
        dvd2.setId(2L);
        dvd2.setTitle("Drama Movie");
        dvd2.setGenres(Arrays.asList(drama));

        Rental rental1 = new Rental();
        rental1.setId(1L);
        rental1.setDvd(dvd1);

        Rental rental2 = new Rental();
        rental2.setId(2L);
        rental2.setDvd(dvd2);

        return Arrays.asList(rental1, rental2);
    }

    private List<Genre> extractGenresFromRentals(List<Rental> rentals) {
        return rentals.stream()
                .flatMap(rental -> rental.getDvd().getGenres().stream())
                .distinct()
                .collect(java.util.stream.Collectors.toList());
    }

    private List<Dvd> createTestDvds() {
        Dvd dvd1 = new Dvd();
        dvd1.setId(3L);
        dvd1.setTitle("Recommended Action Movie");

        Dvd dvd2 = new Dvd();
        dvd2.setId(4L);
        dvd2.setTitle("Recommended Drama Movie");

        return Arrays.asList(dvd1, dvd2);
    }
}
