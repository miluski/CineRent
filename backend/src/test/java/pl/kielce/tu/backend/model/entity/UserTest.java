package pl.kielce.tu.backend.model.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import pl.kielce.tu.backend.model.constant.RankType;

class UserTest {

    @Test
    void builderSetsAllFields() {
        User user = User.builder()
                .id(1L)
                .nickname("nick")
                .password("secret")
                .rank(RankType.ADMIN)
                .build();

        assertEquals(1L, user.getId());
        assertEquals("nick", user.getNickname());
        assertEquals("secret", user.getPassword());
        assertEquals(RankType.ADMIN, user.getRank());
    }

    @Test
    void builderDefaultRankIsUserWhenNotProvided() {
        User user = User.builder()
                .id(2L)
                .nickname("joe")
                .password("pwd")
                .build();

        assertNotNull(user.getRank());
        assertEquals(RankType.USER, user.getRank());
    }

    @Test
    void equalsAndHashCodeForSameFieldValues() {
        User u1 = new User(3L, "alice", "pw", RankType.USER);
        User u2 = new User(3L, "alice", "pw", RankType.USER);

        assertEquals(u1, u2);
        assertEquals(u1.hashCode(), u2.hashCode());
    }

    @Test
    void toStringContainsReadableFields() {
        User user = User.builder()
                .id(4L)
                .nickname("bob")
                .password("hidden")
                .build();

        String s = user.toString();
        assertTrue(s.contains("bob"));
        assertTrue(s.contains("hidden"));
    }
}
