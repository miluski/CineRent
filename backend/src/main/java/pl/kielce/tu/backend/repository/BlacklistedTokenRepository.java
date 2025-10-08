package pl.kielce.tu.backend.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import pl.kielce.tu.backend.model.entity.BlacklistedToken;

@Repository
public interface BlacklistedTokenRepository extends JpaRepository<BlacklistedToken, Long> {

    @Query("SELECT bt FROM BlacklistedToken bt WHERE bt.tokenValue = :tokenValue")
    Optional<BlacklistedToken> findByToken(@Param("tokenValue") String tokenValue);

}
