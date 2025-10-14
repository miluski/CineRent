package pl.kielce.tu.backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import pl.kielce.tu.backend.model.entity.Rental;

@Repository
public interface TransactionRepository extends JpaRepository<Rental, Long> {

    @Query("""
            SELECT r FROM Rental r WHERE r.user.id = :userId \
            AND r.transaction IS NOT NULL \
            ORDER BY r.transaction.generatedAt DESC""")
    Page<Rental> findByUserIdWithTransactions(@Param("userId") Long userId, Pageable pageable);

    @Query("""
            SELECT r FROM Rental r WHERE r.transaction IS NOT NULL \
            ORDER BY r.transaction.generatedAt DESC""")
    Page<Rental> findAllWithTransactions(Pageable pageable);

    @Query("""
            SELECT r FROM Rental r WHERE r.id = :rentalId \
            AND r.transaction IS NOT NULL \
            AND r.user.id = :userId""")
    Optional<Rental> findByIdAndUserIdWithTransaction(@Param("rentalId") Long rentalId, @Param("userId") Long userId);

    @Query("""
            SELECT r FROM Rental r WHERE r.id = :rentalId \
            AND r.transaction IS NOT NULL""")
    Optional<Rental> findByIdWithTransaction(@Param("rentalId") Long rentalId);

    List<Rental> findByUserIdAndTransactionIsNotNull(Long userId);

    @Query("""
            SELECT r FROM Rental r WHERE r.user.id = :userId \
            AND r.transaction IS NOT NULL \
            ORDER BY r.transaction.generatedAt DESC""")
    List<Rental> findByUserIdOrderByGeneratedAtDesc(@Param("userId") Long userId);

    @Query("""
            SELECT r FROM Rental r WHERE r.transaction IS NOT NULL \
            ORDER BY r.transaction.generatedAt DESC""")
    List<Rental> findAllOrderByGeneratedAtDesc();
}
