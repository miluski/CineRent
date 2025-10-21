package pl.kielce.tu.backend.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import pl.kielce.tu.backend.model.entity.Genre;
import pl.kielce.tu.backend.model.entity.User;
import pl.kielce.tu.backend.repository.GenreRepository;
import pl.kielce.tu.backend.repository.UserRepository;
import pl.kielce.tu.backend.service.user.UserGenreService;

@DataJpaTest
@ActiveProfiles("test")
class GenreDeletionIntegrationTest {

    @Autowired
    private GenreRepository genreRepository;

    @Autowired
    private UserRepository userRepository;

    private UserGenreService userGenreService;

    @Test
    void removeGenreFromAllUsers_integratedWithDatabase_successfullyRemovesReferences() {
        userGenreService = new UserGenreService(userRepository);
        Genre actionGenre = Genre.builder().name("Action").build();
        Genre comedyGenre = Genre.builder().name("Comedy").build();

        actionGenre = genreRepository.save(actionGenre);
        comedyGenre = genreRepository.save(comedyGenre);

        User user1 = User.builder()
                .nickname("testuser1")
                .email("testuser1@test.com")
                .password("password123")
                .age(25)
                .isVerified(true)
                .preferredGenres(new ArrayList<>(Arrays.asList(actionGenre, comedyGenre)))
                .build();

        User user2 = User.builder()
                .nickname("testuser2")
                .email("testuser2@test.com")
                .password("password456")
                .age(30)
                .isVerified(true)
                .preferredGenres(new ArrayList<>(Arrays.asList(actionGenre)))
                .build();

        user1 = userRepository.save(user1);
        user2 = userRepository.save(user2);

        assertEquals(2, user1.getPreferredGenres().size());
        assertEquals(1, user2.getPreferredGenres().size());

        userGenreService.removeGenreFromAllUsers(actionGenre.getId());

        User updatedUser1 = userRepository.findById(user1.getId()).orElseThrow();
        User updatedUser2 = userRepository.findById(user2.getId()).orElseThrow();

        assertEquals(1, updatedUser1.getPreferredGenres().size());
        assertEquals(comedyGenre.getId(), updatedUser1.getPreferredGenres().get(0).getId());

        assertEquals(0, updatedUser2.getPreferredGenres().size());

        genreRepository.deleteById(actionGenre.getId());
        assertTrue(genreRepository.findById(actionGenre.getId()).isEmpty());
    }

    @Test
    void countUsersWithGenre_integratedWithDatabase_returnsCorrectCount() {
        userGenreService = new UserGenreService(userRepository);
        Genre dramaGenre = Genre.builder().name("Drama").build();
        dramaGenre = genreRepository.save(dramaGenre);

        User user1 = User.builder()
                .nickname("dramauser1")
                .email("dramauser1@test.com")
                .password("password123")
                .age(25)
                .isVerified(true)
                .preferredGenres(new ArrayList<>(Arrays.asList(dramaGenre)))
                .build();

        User user2 = User.builder()
                .nickname("dramauser2")
                .email("dramauser2@test.com")
                .password("password456")
                .age(30)
                .isVerified(true)
                .preferredGenres(new ArrayList<>(Arrays.asList(dramaGenre)))
                .build();

        userRepository.save(user1);
        userRepository.save(user2);

        long count = userGenreService.countUsersWithGenre(dramaGenre.getId());

        assertEquals(2, count);
    }
}
