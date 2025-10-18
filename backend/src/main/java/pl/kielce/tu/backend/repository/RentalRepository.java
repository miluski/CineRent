package pl.kielce.tu.backend.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import pl.kielce.tu.backend.model.constant.RentalStatus;
import pl.kielce.tu.backend.model.entity.Rental;

@Repository
public interface RentalRepository extends JpaRepository<Rental, Long> {

    List<Rental> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<Rental> findByUserIdAndStatusOrderByCreatedAtDesc(Long userId, RentalStatus status);

    @Query("""
            SELECT r FROM Rental r WHERE r.user.id = :userId \
            AND (:status IS NULL OR r.status = :status) \
            ORDER BY r.createdAt DESC""")
    Page<Rental> findByUserIdWithOptionalStatus(@Param("userId") Long userId,
            @Param("status") RentalStatus status,
            Pageable pageable);

    @Query("""
            SELECT r FROM Rental r WHERE r.user.id = :userId \
            AND r.dvd.title LIKE %:dvdTitle% \
            ORDER BY r.createdAt DESC""")
    List<Rental> findByUserIdAndDvdTitleContaining(@Param("userId") Long userId,
            @Param("dvdTitle") String dvdTitle);

    @Query("""
            SELECT r FROM Rental r WHERE r.user.id = :userId \
            AND r.rentalStart >= :startDate \
            AND r.rentalEnd <= :endDate \
            ORDER BY r.createdAt DESC""")
    List<Rental> findByUserIdAndDateRange(@Param("userId") Long userId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    @Query("SELECT r FROM Rental r WHERE r.status = :status")
    List<Rental> findByStatus(@Param("status") RentalStatus status);

    @Query("SELECT COUNT(r) FROM Rental r WHERE r.dvd.id = :dvdId AND r.status = :status")
    Long countByDvdIdAndStatus(@Param("dvdId") Long dvdId, @Param("status") RentalStatus status);

    @Query("""
            SELECT r FROM Rental r WHERE r.status = pl.kielce.tu.backend.model.constant.RentalStatus.ACTIVE \
            AND r.rentalEnd < :currentDateTime""")
    List<Rental> findExpiredActiveRentals(@Param("currentDateTime") LocalDateTime currentDateTime);

}
