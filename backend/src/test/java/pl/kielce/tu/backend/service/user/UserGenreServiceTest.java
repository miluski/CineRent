package pl.kielce.tu.backend.service.user;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import pl.kielce.tu.backend.model.entity.Genre;
import pl.kielce.tu.backend.model.entity.User;
import pl.kielce.tu.backend.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class UserGenreServiceTest {

    @Mock
    private UserRepository userRepository;

    private UserGenreService userGenreService;

    @BeforeEach
    void setUp() {
        userGenreService = new UserGenreService(userRepository);
    }

    @Test
    void removeGenreFromAllUsers_withUsersHavingGenre_removesGenreFromAllUsers() {
        Long genreId = 1L;
        Genre genreToRemove = Genre.builder().id(genreId).name("Action").build();
        Genre otherGenre = Genre.builder().id(2L).name("Comedy").build();

        User user1 = User.builder()
                .id(1L)
                .nickname("user1")
                .preferredGenres(new ArrayList<>(Arrays.asList(genreToRemove, otherGenre)))
                .build();

        User user2 = User.builder()
                .id(2L)
                .nickname("user2")
                .preferredGenres(new ArrayList<>(Arrays.asList(genreToRemove)))
                .build();

        when(userRepository.findUsersByPreferredGenreId(genreId))
                .thenReturn(Arrays.asList(user1, user2));

        userGenreService.removeGenreFromAllUsers(genreId);

        verify(userRepository, times(2)).save(any(User.class));
        assertEquals(1, user1.getPreferredGenres().size());
        assertEquals(otherGenre, user1.getPreferredGenres().get(0));
        assertEquals(0, user2.getPreferredGenres().size());
    }

    @Test
    void removeGenreFromAllUsers_withNoUsersHavingGenre_doesNothing() {
        Long genreId = 1L;
        when(userRepository.findUsersByPreferredGenreId(genreId))
                .thenReturn(Collections.emptyList());

        userGenreService.removeGenreFromAllUsers(genreId);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void removeGenreFromAllUsers_withNullPreferredGenres_handlesGracefully() {
        Long genreId = 1L;
        User userWithNullGenres = User.builder()
                .id(1L)
                .nickname("user1")
                .preferredGenres(null)
                .build();

        when(userRepository.findUsersByPreferredGenreId(genreId))
                .thenReturn(Arrays.asList(userWithNullGenres));
        userGenreService.removeGenreFromAllUsers(genreId);

        verify(userRepository, never()).save(userWithNullGenres);
    }

    @Test
    void countUsersWithGenre_withUsersHavingGenre_returnsCorrectCount() {
        Long genreId = 1L;
        User user1 = mock(User.class);
        User user2 = mock(User.class);
        User user3 = mock(User.class);

        when(userRepository.findUsersByPreferredGenreId(genreId))
                .thenReturn(Arrays.asList(user1, user2, user3));

        long count = userGenreService.countUsersWithGenre(genreId);

        assertEquals(3, count);
    }

    @Test
    void countUsersWithGenre_withNoUsersHavingGenre_returnsZero() {
        Long genreId = 1L;
        when(userRepository.findUsersByPreferredGenreId(genreId))
                .thenReturn(Collections.emptyList());

        long count = userGenreService.countUsersWithGenre(genreId);

        assertEquals(0, count);
    }

    @Test
    void removeGenreFromAllUsers_withMultipleGenresForUser_removesOnlySpecifiedGenre() {
        Long genreIdToRemove = 1L;
        Genre genreToRemove = Genre.builder().id(genreIdToRemove).name("Action").build();
        Genre genreToKeep1 = Genre.builder().id(2L).name("Comedy").build();
        Genre genreToKeep2 = Genre.builder().id(3L).name("Drama").build();

        User user = User.builder()
                .id(1L)
                .nickname("user1")
                .preferredGenres(new ArrayList<>(Arrays.asList(genreToRemove, genreToKeep1, genreToKeep2)))
                .build();

        when(userRepository.findUsersByPreferredGenreId(genreIdToRemove))
                .thenReturn(Arrays.asList(user));

        userGenreService.removeGenreFromAllUsers(genreIdToRemove);

        verify(userRepository, times(1)).save(user);
        assertEquals(2, user.getPreferredGenres().size());
        assertEquals(genreToKeep1, user.getPreferredGenres().get(0));
        assertEquals(genreToKeep2, user.getPreferredGenres().get(1));
    }
}
