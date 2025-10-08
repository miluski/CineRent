package pl.kielce.tu.backend.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

import pl.kielce.tu.backend.model.dto.UserDto;
import pl.kielce.tu.backend.model.entity.User;

class UserMapperTest {

    private final UserMapper mapper = new UserMapper();

    @Test
    void toUser_mapsNicknameAndPassword() {
        UserDto dto = UserDto.builder()
                .nickname("john_doe")
                .password("s3cr3t")
                .build();

        User user = mapper.toUser(dto);

        assertNotNull(user);
        assertEquals("john_doe", user.getNickname());
        assertEquals("s3cr3t", user.getPassword());
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
}


