package pl.kielce.tu.backend.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import pl.kielce.tu.backend.model.entity.Dvd;
import pl.kielce.tu.backend.model.entity.Genre;

@Repository
public interface DvdRepository extends JpaRepository<Dvd, Long>, JpaSpecificationExecutor<Dvd> {

    boolean existsByGenresId(Long genreId);

    @Query("SELECT DISTINCT d FROM Dvd d JOIN d.genres g WHERE g IN :genres AND d.avalaible = true")
    Page<Dvd> findByGenresInAndAvalaibleTrue(@Param("genres") List<Genre> genres, Pageable pageable);

    @Query("SELECT d FROM Dvd d WHERE d.avalaible = true ORDER BY d.addedAt DESC")
    Page<Dvd> findAvailableDvdsOrderByNewest(Pageable pageable);

    @Query("SELECT DISTINCT d FROM Dvd d JOIN d.genres g WHERE g IN :genres AND d.avalaible = true ORDER BY d.addedAt DESC")
    Page<Dvd> findByPreferredGenresAndAvailable(@Param("genres") List<Genre> genres, Pageable pageable);

    @Query("""
            SELECT d FROM Dvd d \
            LEFT JOIN Rental r ON r.dvd.id = d.id \
            WHERE d.avalaible = true \
            GROUP BY d.id \
            ORDER BY COUNT(r) DESC""")
    Page<Dvd> findMostPopularAvailableDvds(Pageable pageable);

    @Query("""
            SELECT d FROM Dvd d \
            JOIN Rental r ON r.dvd.id = d.id \
            JOIN User u ON r.user.id = u.id \
            WHERE d.avalaible = true AND u.age BETWEEN :minAge AND :maxAge \
            GROUP BY d.id \
            ORDER BY COUNT(r) DESC""")
    Page<Dvd> findMostPopularDvdsByAgeGroup(@Param("minAge") Integer minAge,
            @Param("maxAge") Integer maxAge,
            Pageable pageable);

}
