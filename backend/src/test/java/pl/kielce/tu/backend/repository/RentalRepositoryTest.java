package pl.kielce.tu.backend.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.Query;

import pl.kielce.tu.backend.model.constant.RentalStatus;

class RentalRepositoryTest {

    @Test
    void shouldHave_findByUserIdOrderByCreatedAtDesc() throws NoSuchMethodException {
        Method m = RentalRepository.class.getMethod("findByUserIdOrderByCreatedAtDesc", Long.class);
        assertNotNull(m);
        assertTrue(List.class.isAssignableFrom(m.getReturnType()), "Return type should be List");
    }

    @Test
    void shouldHave_findByUserIdAndStatusOrderByCreatedAtDesc() throws NoSuchMethodException {
        Method m = RentalRepository.class.getMethod("findByUserIdAndStatusOrderByCreatedAtDesc", Long.class,
                RentalStatus.class);
        assertNotNull(m);
        assertTrue(List.class.isAssignableFrom(m.getReturnType()), "Return type should be List");
    }

    @Test
    void shouldHave_findByUserIdWithOptionalStatus_withQueryAndPageable() throws NoSuchMethodException {
        Method m = RentalRepository.class.getMethod("findByUserIdWithOptionalStatus", Long.class, RentalStatus.class,
                org.springframework.data.domain.Pageable.class);
        assertNotNull(m);
        assertTrue(Page.class.isAssignableFrom(m.getReturnType()), "Return type should be Page");
        Query q = m.getAnnotation(Query.class);
        assertNotNull(q, "Method should be annotated with @Query");
        String value = q.value();
        assertTrue(value.contains("r.user.id = :userId"), "Query should filter by userId");
        assertTrue(value.contains("(:status IS NULL OR r.status = :status)"), "Query should allow optional status");
        assertTrue(value.contains("ORDER BY r.createdAt DESC"), "Query should order by createdAt desc");
    }

    @Test
    void shouldHave_findByUserIdAndDvdTitleContaining_withQuery() throws NoSuchMethodException {
        Method m = RentalRepository.class.getMethod("findByUserIdAndDvdTitleContaining", Long.class, String.class);
        assertNotNull(m);
        assertTrue(List.class.isAssignableFrom(m.getReturnType()), "Return type should be List");
        Query q = m.getAnnotation(Query.class);
        assertNotNull(q);
        String value = q.value();
        assertTrue(value.contains("r.user.id = :userId"), "Query should filter by userId");
        assertTrue(value.contains("r.dvd.title LIKE %:dvdTitle%") || value.contains("r.dvd.title LIKE %:dvdTitle%"),
                "Query should search DVD title using LIKE");
        assertTrue(value.contains("ORDER BY r.createdAt DESC"));
    }

    @Test
    void shouldHave_findByUserIdAndDateRange_withQuery() throws NoSuchMethodException {
        Method m = RentalRepository.class.getMethod("findByUserIdAndDateRange", Long.class, LocalDateTime.class,
                LocalDateTime.class);
        assertNotNull(m);
        assertTrue(List.class.isAssignableFrom(m.getReturnType()), "Return type should be List");
        Query q = m.getAnnotation(Query.class);
        assertNotNull(q);
        String value = q.value();
        assertTrue(value.contains("r.user.id = :userId"));
        assertTrue(value.contains("r.rentalStart >= :startDate"));
        assertTrue(value.contains("r.rentalEnd <= :endDate"));
        assertTrue(value.contains("ORDER BY r.createdAt DESC"));
    }

    @Test
    void shouldHave_findByStatus_withExactQuery() throws NoSuchMethodException {
        Method m = RentalRepository.class.getMethod("findByStatus", RentalStatus.class);
        assertNotNull(m);
        assertTrue(List.class.isAssignableFrom(m.getReturnType()));
        Query q = m.getAnnotation(Query.class);
        assertNotNull(q);
        assertEquals("SELECT r FROM Rental r WHERE r.status = :status", q.value().trim());
    }

    @Test
    void shouldHave_countByDvdIdAndStatus_withExactQueryAndReturnType() throws NoSuchMethodException {
        Method m = RentalRepository.class.getMethod("countByDvdIdAndStatus", Long.class, RentalStatus.class);
        assertNotNull(m);
        assertEquals(Long.class, m.getReturnType(), "Return type should be Long");
        Query q = m.getAnnotation(Query.class);
        assertNotNull(q);
        assertEquals("SELECT COUNT(r) FROM Rental r WHERE r.dvd.id = :dvdId AND r.status = :status", q.value().trim());
    }
}
