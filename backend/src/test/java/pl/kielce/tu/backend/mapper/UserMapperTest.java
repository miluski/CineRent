package pl.kielce.tu.backend.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;

import org.junit.jupiter.api.Test;

import pl.kielce.tu.backend.model.dto.UserDto;
import pl.kielce.tu.backend.model.entity.Genre;
import pl.kielce.tu.backend.model.entity.User;

class UserMapperTest {

    private final GenreMapper genreMappingService = mock(GenreMapper.class);
    private final UserMapper mapper = new UserMapper(genreMappingService);

    @Test
    void toUser_mapsNicknameAndPassword() {
        UserDto dto = UserDto.builder()
                .nickname("john_doe")
                .password("s3cr3t")
                .build();

        when(genreMappingService.mapGenreIdsToGenres(null))
                .thenReturn(Collections.emptyList());

        User user = mapper.toUser(dto);

        assertNotNull(user);
        assertEquals("john_doe", user.getNickname());
        assertEquals("s3cr3t", user.getPassword());
    }

    @Test
    void toUser_mapsPreferredGenres_whenGenreIdsProvided() {
        Genre comedy = Genre.builder().id(1L).name("Komedia").build();
        Genre action = Genre.builder().id(2L).name("Akcja").build();

        UserDto dto = UserDto.builder()
                .nickname("test_user")
                .password("password")
                .age(25)
                .preferredGenresIdentifiers(Arrays.asList(1L, 2L))
                .build();

        when(genreMappingService.mapGenreIdsToGenres(Arrays.asList(1L, 2L)))
                .thenReturn(Arrays.asList(comedy, action));

        User user = mapper.toUser(dto);

        assertNotNull(user);
        assertEquals("test_user", user.getNickname());
        assertEquals("password", user.getPassword());
        assertEquals(25, user.getAge());
        assertNotNull(user.getPreferredGenres());
        assertEquals(2, user.getPreferredGenres().size());
        assertEquals("Komedia", user.getPreferredGenres().get(0).getName());
        assertEquals("Akcja", user.getPreferredGenres().get(1).getName());
    }

    @Test
    void toDto_mapsNicknameOnly_andLeavesPasswordNull() {
        User user = User.builder()
                .nickname("alice")
                .password("hidden")
                .build();

        UserDto dto = mapper.toDto(user);

        assertNotNull(dto);
        assertEquals("alice", dto.getNickname());
        assertNull(dto.getPassword());
    }

    @Test
    void toDto_mapsPreferredGenres_whenGenresExist() {
        Genre comedy = Genre.builder().id(1L).name("Komedia").build();
        Genre sciFi = Genre.builder().id(2L).name("Sci-Fi").build();
        Genre action = Genre.builder().id(3L).name("Akcja").build();

        User user = User.builder()
                .nickname("FilmLover99")
                .age(24)
                .preferredGenres(Arrays.asList(comedy, sciFi, action))
                .build();

        when(genreMappingService.mapGenresToNames(Arrays.asList(comedy, sciFi, action)))
                .thenReturn(Arrays.asList("Komedia", "Sci-Fi", "Akcja"));

        UserDto dto = mapper.toDto(user);

        assertNotNull(dto);
        assertEquals("FilmLover99", dto.getNickname());
        assertEquals(24, dto.getAge());
        assertNotNull(dto.getPreferredGenres());
        assertEquals(3, dto.getPreferredGenres().size());
        assertTrue(dto.getPreferredGenres().contains("Komedia"));
        assertTrue(dto.getPreferredGenres().contains("Sci-Fi"));
        assertTrue(dto.getPreferredGenres().contains("Akcja"));
    }

    @Test
    void toDto_returnsEmptyLists_whenNoGenres() {
        User user = User.builder()
                .nickname("NoGenres")
                .age(30)
                .preferredGenres(Collections.emptyList())
                .build();

        when(genreMappingService.mapGenresToNames(Collections.emptyList()))
                .thenReturn(Collections.emptyList());

        UserDto dto = mapper.toDto(user);

        assertNotNull(dto);
        assertNotNull(dto.getPreferredGenres());
        assertTrue(dto.getPreferredGenres().isEmpty());
    }

    @Test
    void toDto_returnsEmptyLists_whenGenresNull() {
        User user = User.builder()
                .nickname("NullGenres")
                .age(25)
                .preferredGenres(null)
                .build();

        when(genreMappingService.mapGenresToNames(null))
                .thenReturn(Collections.emptyList());

        UserDto dto = mapper.toDto(user);

        assertNotNull(dto);
        assertNotNull(dto.getPreferredGenres());
        assertTrue(dto.getPreferredGenres().isEmpty());
    }
}
