package pl.kielce.tu.backend.repository;

import java.sql.Date;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import pl.kielce.tu.backend.model.constant.ReservationStatus;
import pl.kielce.tu.backend.model.entity.Reservation;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    List<Reservation> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<Reservation> findByUserIdAndStatusOrderByCreatedAtDesc(Long userId, ReservationStatus status);

    @Query("""
            SELECT r FROM Reservation r WHERE r.user.id = :userId \
            AND (:status IS NULL OR r.status = :status) \
            ORDER BY r.createdAt DESC""")
    Page<Reservation> findByUserIdWithOptionalStatus(@Param("userId") Long userId,
            @Param("status") ReservationStatus status,
            Pageable pageable);

    @Query("""
            SELECT r FROM Reservation r WHERE r.user.id = :userId \
            AND r.dvd.title LIKE %:dvdTitle% \
            ORDER BY r.createdAt DESC""")
    List<Reservation> findByUserIdAndDvdTitleContaining(@Param("userId") Long userId,
            @Param("dvdTitle") String dvdTitle);

    @Query("""
            SELECT r FROM Reservation r WHERE r.user.id = :userId \
            AND r.rentalStart >= :startDate \
            AND r.rentalEnd <= :endDate \
            ORDER BY r.createdAt DESC""")
    List<Reservation> findByUserIdAndDateRange(@Param("userId") Long userId,
            @Param("startDate") Date startDate,
            @Param("endDate") Date endDate);

    @Query("SELECT r FROM Reservation r WHERE r.status = :status")
    List<Reservation> findByStatus(@Param("status") ReservationStatus status);

    @Query("SELECT COUNT(r) FROM Reservation r WHERE r.dvd.id = :dvdId AND r.status IN :statuses")
    Long countByDvdIdAndStatusIn(@Param("dvdId") Long dvdId, @Param("statuses") List<ReservationStatus> statuses);

    @Query("""
            SELECT r FROM Reservation r WHERE r.dvd.id = :dvdId \
            AND r.status = :status \
            AND r.rentalStart <= :date AND r.rentalEnd >= :date""")
    List<Reservation> findConflictingReservations(@Param("dvdId") Long dvdId,
            @Param("status") ReservationStatus status,
            @Param("date") Date date);
            
}
