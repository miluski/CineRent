package pl.kielce.tu.backend.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import pl.kielce.tu.backend.model.constant.BillType;
import pl.kielce.tu.backend.model.constant.RentalStatus;
import pl.kielce.tu.backend.model.entity.Dvd;
import pl.kielce.tu.backend.model.entity.Rental;
import pl.kielce.tu.backend.model.entity.Transaction;
import pl.kielce.tu.backend.model.entity.User;

@DataJpaTest
@DisplayName("RentalRepository - Expired Rentals Tests")
class RentalRepositoryExpiredRentalsTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private RentalRepository rentalRepository;

    private User testUser;
    private Dvd testDvd;

    @BeforeEach
    void setUp() {
        testUser = createAndPersistUser();
        testDvd = createAndPersistDvd();
    }

    @Test
    @DisplayName("Should find expired active rentals")
    void shouldFindExpiredActiveRentals() {
        Rental expiredRental = createRental(
                LocalDateTime.now().minusDays(7),
                LocalDateTime.now().minusDays(1),
                RentalStatus.ACTIVE);
        entityManager.persist(expiredRental);
        entityManager.flush();
        List<Rental> result = rentalRepository.findExpiredActiveRentals(
                LocalDateTime.now());
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(expiredRental.getId(), result.get(0).getId());
    }

    @Test
    @DisplayName("Should not find active rentals with future end date")
    void shouldNotFindActiveRentalsWithFutureEndDate() {

        Rental activeRental = createRental(
                LocalDateTime.now(),
                LocalDateTime.now().plusDays(7),
                RentalStatus.ACTIVE);
        entityManager.persist(activeRental);
        entityManager.flush();

        List<Rental> result = rentalRepository.findExpiredActiveRentals(
                LocalDateTime.now());

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Should not find expired rentals with non-active status")
    void shouldNotFindExpiredRentalsWithNonActiveStatus() {

        Rental inactiveRental = createRental(
                LocalDateTime.now().minusDays(7),
                LocalDateTime.now().minusDays(1),
                RentalStatus.INACTIVE);
        entityManager.persist(inactiveRental);
        entityManager.flush();

        List<Rental> result = rentalRepository.findExpiredActiveRentals(
                LocalDateTime.now());

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    private User createAndPersistUser() {
        User user = User.builder()
                .nickname("testuser")
                .email("testuser@test.com")
                .password("password")
                .age(25)
                .isVerified(true)
                .build();
        return entityManager.persist(user);
    }

    private Dvd createAndPersistDvd() {
        Dvd dvd = Dvd.builder()
                .title("Test DVD")
                .description("Test Description")
                .directors(List.of("Test Director"))
                .releaseYear(2020)
                .durationMinutes(120)
                .posterUrl("http://example.com/poster.jpg")
                .copiesAvalaible(10)
                .rentalPricePerDay(5.0f)
                .build();
        return entityManager.persist(dvd);
    }

    private Rental createRental(LocalDateTime start, LocalDateTime end, RentalStatus status) {
        Transaction transaction = Transaction.builder()
                .invoiceId("INV-TEST")
                .dvdTitle(testDvd.getTitle())
                .rentalPeriodDays(7)
                .pricePerDay(BigDecimal.valueOf(5.0))
                .lateFee(BigDecimal.ZERO)
                .totalAmount(BigDecimal.valueOf(35.0))
                .generatedAt(LocalDateTime.now())
                .billType(BillType.INVOICE)
                .build();

        return Rental.builder()
                .user(testUser)
                .dvd(testDvd)
                .rentalStart(start)
                .rentalEnd(end)
                .status(status)
                .count(1)
                .createdAt(LocalDateTime.now())
                .transaction(transaction)
                .build();
    }
}
