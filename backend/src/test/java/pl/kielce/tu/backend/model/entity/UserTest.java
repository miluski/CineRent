package pl.kielce.tu.backend.model.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

import pl.kielce.tu.backend.model.constant.RankType;

class UserTest {

    @Test
    void builderSetsAllFields() {
        Genre genre1 = Genre.builder().id(1L).name("Action").build();
        Genre genre2 = Genre.builder().id(2L).name("Drama").build();
        List<Genre> genres = Arrays.asList(genre1, genre2);

        User user = User.builder()
                .id(1L)
                .nickname("nick")
                .password("secret")
                .age(25)
                .preferredGenres(genres)
                .rank(RankType.ADMIN)
                .build();

        assertEquals(1L, user.getId());
        assertEquals("nick", user.getNickname());
        assertEquals("secret", user.getPassword());
        assertEquals(25, user.getAge());
        assertEquals(2, user.getPreferredGenres().size());
        assertEquals(RankType.ADMIN, user.getRank());
    }

    @Test
    void builderDefaultRankIsUserWhenNotProvided() {
        User user = User.builder()
                .id(2L)
                .nickname("joe")
                .password("pwd")
                .age(30)
                .build();

        assertNotNull(user.getRank());
        assertEquals(RankType.USER, user.getRank());
    }

    @Test
    void equalsAndHashCodeForSameFieldValues() {
        User u1 = new User(3L, "alice", "alice@test.com", "pw", 28, null, RankType.USER, false, null, null, null);
        User u2 = new User(3L, "alice", "alice@test.com", "pw", 28, null, RankType.USER, false, null, null, null);

        assertEquals(u1, u2);
        assertEquals(u1.hashCode(), u2.hashCode());
    }

    @Test
    void toStringContainsReadableFields() {
        User user = User.builder()
                .id(4L)
                .nickname("bob")
                .password("hidden")
                .age(35)
                .build();

        String s = user.toString();
        assertTrue(s.contains("bob"));
        assertTrue(s.contains("hidden"));
        assertTrue(s.contains("35"));
    }

    @Test
    void userCanHaveEmptyPreferredGenres() {
        User user = User.builder()
                .id(5L)
                .nickname("john")
                .password("pass123")
                .age(22)
                .build();

        assertTrue(user.getPreferredGenres() == null || user.getPreferredGenres().isEmpty());
    }

    @Test
    void userCanHaveMultiplePreferredGenres() {
        Genre sciFi = Genre.builder().id(1L).name("Science-Fiction").build();
        Genre action = Genre.builder().id(2L).name("Akcja").build();
        Genre horror = Genre.builder().id(3L).name("Horror").build();

        User user = User.builder()
                .id(6L)
                .nickname("moviefan")
                .password("secure")
                .age(27)
                .preferredGenres(Arrays.asList(sciFi, action, horror))
                .build();

        assertEquals(3, user.getPreferredGenres().size());
        assertTrue(user.getPreferredGenres().contains(sciFi));
        assertTrue(user.getPreferredGenres().contains(action));
        assertTrue(user.getPreferredGenres().contains(horror));
    }

    @Test
    void userWithAdminRank() {
        User admin = User.builder()
                .id(7L)
                .nickname("administrator")
                .password("adminpass")
                .age(40)
                .rank(RankType.ADMIN)
                .build();

        assertEquals(RankType.ADMIN, admin.getRank());
    }

    @Test
    void userAgeIsRequired() {
        User user = User.builder()
                .id(8L)
                .nickname("testuser")
                .password("testpass")
                .age(18)
                .build();

        assertNotNull(user.getAge());
        assertEquals(18, user.getAge());
    }

    @Test
    void allArgsConstructorCreatesValidUser() {
        Genre genre = Genre.builder().id(1L).name("Drama").build();
        List<Genre> genres = Arrays.asList(genre);

        User user = new User(9L, "constructor", "constructor@test.com", "pwd", 33, genres, RankType.USER, false, null,
                null, null);

        assertEquals(9L, user.getId());
        assertEquals("constructor", user.getNickname());
        assertEquals("constructor@test.com", user.getEmail());
        assertEquals("pwd", user.getPassword());
        assertEquals(33, user.getAge());
        assertEquals(1, user.getPreferredGenres().size());
        assertEquals(RankType.USER, user.getRank());
    }

    @Test
    void userCanHaveAvatarPath() {
        User user = User.builder()
                .id(10L)
                .nickname("avataruser")
                .password("pass")
                .age(25)
                .avatarPath("/uploads/avatars/avatar-123.png")
                .build();

        assertNotNull(user.getAvatarPath());
        assertEquals("/uploads/avatars/avatar-123.png", user.getAvatarPath());
    }

    @Test
    void userCanHaveNullAvatarPath() {
        User user = User.builder()
                .id(11L)
                .nickname("noavatar")
                .password("pass")
                .age(30)
                .build();

        assertNull(user.getAvatarPath());
    }

    @Test
    void builderSetsAvatarPathCorrectly() {
        String avatarPath = "/uploads/avatars/test-avatar.jpg";
        User user = User.builder()
                .id(12L)
                .nickname("testuser")
                .password("secure")
                .age(28)
                .avatarPath(avatarPath)
                .build();

        assertEquals(avatarPath, user.getAvatarPath());
    }
}
