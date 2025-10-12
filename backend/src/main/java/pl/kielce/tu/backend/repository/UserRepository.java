package pl.kielce.tu.backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import pl.kielce.tu.backend.model.entity.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByNickname(String nickname);

    @Query("SELECT u FROM User u JOIN u.preferredGenres g WHERE g.id = :genreId")
    List<User> findUsersByPreferredGenreId(@Param("genreId") Long genreId);

}
