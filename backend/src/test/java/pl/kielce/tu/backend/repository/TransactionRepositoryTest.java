package pl.kielce.tu.backend.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import pl.kielce.tu.backend.model.entity.Rental;
import pl.kielce.tu.backend.model.entity.Transaction;
import pl.kielce.tu.backend.model.entity.User;

@DataJpaTest
@EntityScan(basePackages = "pl.kielce.tu.backend.model.entity")
@AutoConfigureTestDatabase(replace = Replace.ANY)
class TransactionRepositoryTest {

    @Autowired
    private TransactionRepository repository;

    @Autowired
    private org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager em;

    private User createUser(String name) {
        User u = new User();
        u.setNickname(name);
        u.setPassword("password");
        u.setAge(25);
        u.setRank(pl.kielce.tu.backend.model.constant.RankType.USER);
        em.persist(u);
        em.flush();
        return u;
    }

    private Transaction createTransaction(LocalDateTime generatedAt) {
        return Transaction.builder()
                .invoiceId("INV-" + System.nanoTime())
                .dvdTitle("Test DVD")
                .rentalPeriodDays(7)
                .pricePerDay(java.math.BigDecimal.valueOf(5.00))
                .lateFee(java.math.BigDecimal.ZERO)
                .totalAmount(java.math.BigDecimal.valueOf(35.00))
                .generatedAt(generatedAt)
                .billType(pl.kielce.tu.backend.model.constant.BillType.RECEIPT)
                .build();
    }

    private Rental createRental(User user, Transaction tx) {
        // Create a test DVD
        pl.kielce.tu.backend.model.entity.Dvd dvd = new pl.kielce.tu.backend.model.entity.Dvd();
        dvd.setTitle("Test DVD");
        dvd.setDescription("Test Description");
        dvd.setReleaseYear(2020);
        dvd.setDurationMinutes(120);
        dvd.setAvalaible(true);
        dvd.setCopiesAvalaible(5);
        dvd.setRentalPricePerDay(5.00f);
        dvd.setPosterUrl("test-poster.jpg");
        dvd.setDirectors(java.util.List.of("Test Director"));
        dvd.setAddedAt(java.time.LocalDateTime.now());
        em.persist(dvd);

        Rental r = Rental.builder()
                .user(user)
                .dvd(dvd)
                .transaction(tx)
                .rentalStart(java.sql.Date.valueOf("2024-01-01"))
                .rentalEnd(java.sql.Date.valueOf("2024-01-07"))
                .createdAt(java.time.LocalDateTime.now())
                .count(1)
                .status(pl.kielce.tu.backend.model.constant.RentalStatus.INACTIVE)
                .build();

        em.persist(r);
        em.flush();
        return r;
    }

    @Test
    @Transactional
    @Rollback
    void findByUserIdWithTransactions_returnsPagedAndOrderedByGeneratedAtDesc() {
        User u1 = createUser("u1");
        User u2 = createUser("u2");

        Transaction t1 = createTransaction(LocalDateTime.now().minusDays(1));
        Transaction t2 = createTransaction(LocalDateTime.now());
        Transaction t3 = createTransaction(LocalDateTime.now().minusDays(2));

        createRental(u1, t1);
        createRental(u1, t2);
        createRental(u2, t3);

        Page<Rental> page = repository.findByUserIdWithTransactions(u1Id(u1), PageRequest.of(0, 10));
        assertThat(page.getTotalElements()).isEqualTo(2);
        List<Rental> content = page.getContent();
        assertThat(content.get(0).getTransaction().getGeneratedAt())
                .isAfterOrEqualTo(content.get(1).getTransaction().getGeneratedAt());
    }

    @Test
    @Transactional
    @Rollback
    void findAllWithTransactions_returnsPagedAndOrdered() {
        User u = createUser("user");
        Transaction t1 = createTransaction(LocalDateTime.now().minusHours(5));
        Transaction t2 = createTransaction(LocalDateTime.now().plusHours(1));
        createRental(u, t1);
        createRental(u, t2);

        Page<Rental> page = repository.findAllWithTransactions(PageRequest.of(0, 10));
        assertThat(page.getTotalElements()).isGreaterThanOrEqualTo(2);
        List<Rental> content = page.getContent();
        assertThat(content.get(0).getTransaction().getGeneratedAt())
                .isAfterOrEqualTo(content.get(1).getTransaction().getGeneratedAt());
    }

    @Test
    @Transactional
    @Rollback
    void findByIdAndUserIdWithTransaction_and_findByIdWithTransaction_behaviour() {
        User u = createUser("owner");
        Transaction tx = createTransaction(LocalDateTime.now());
        Rental r = createRental(u, tx);

        Optional<Rental> found = repository.findByIdAndUserIdWithTransaction(rId(r), uId(u));
        assertThat(found).isPresent();

        Optional<Rental> foundById = repository.findByIdWithTransaction(rId(r));
        assertThat(foundById).isPresent();
        Optional<Rental> notFound = repository.findByIdAndUserIdWithTransaction(rId(r), -999L);
        assertThat(notFound).isEmpty();
    }

    @Test
    @Transactional
    @Rollback
    void findByUserIdAndTransactionIsNotNull_and_ordered_list_methods() {
        User u = createUser("listUser");
        Transaction t1 = createTransaction(LocalDateTime.now().minusMinutes(10));
        Transaction t2 = createTransaction(LocalDateTime.now().plusMinutes(10));
        createRental(u, t1);
        createRental(u, t2);

        List<Rental> list = repository.findByUserIdAndTransactionIsNotNull(uId(u));
        assertThat(list).isNotEmpty();

        List<Rental> orderedByDesc = repository.findByUserIdOrderByGeneratedAtDesc(uId(u));
        assertThat(orderedByDesc).isNotEmpty();
        if (orderedByDesc.size() >= 2) {
            assertThat(orderedByDesc.get(0).getTransaction().getGeneratedAt())
                    .isAfterOrEqualTo(orderedByDesc.get(1).getTransaction().getGeneratedAt());
        }

        List<Rental> allOrdered = repository.findAllOrderByGeneratedAtDesc();
        assertThat(allOrdered).isNotEmpty();
        if (allOrdered.size() >= 2) {
            assertThat(allOrdered.get(0).getTransaction().getGeneratedAt())
                    .isAfterOrEqualTo(allOrdered.get(1).getTransaction().getGeneratedAt());
        }
    }

    private Long rId(Rental r) {
        return r.getId();
    }

    private Long uId(User u) {
        return u.getId();
    }

    private Long u1Id(User u) {
        return u.getId();
    }
}
