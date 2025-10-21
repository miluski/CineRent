package pl.kielce.tu.backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import pl.kielce.tu.backend.model.entity.DvdReminder;

@Repository
public interface DvdReminderRepository extends JpaRepository<DvdReminder, Long> {

    @Query("""
                SELECT dr FROM DvdReminder dr
                JOIN dr.dvd d
                WHERE d.avalaible = true AND d.copiesAvalaible > 0
            """)
    List<DvdReminder> findRemindersForAvailableDvds();

    @Query("""
                SELECT dr FROM DvdReminder dr
                WHERE dr.user.id = :userId AND dr.dvd.id = :dvdId
            """)
    Optional<DvdReminder> findByUserIdAndDvdId(Long userId, Long dvdId);

    boolean existsByUserIdAndDvdId(Long userId, Long dvdId);

}
