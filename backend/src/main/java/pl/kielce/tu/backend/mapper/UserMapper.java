package pl.kielce.tu.backend.mapper;

import org.springframework.stereotype.Component;

import pl.kielce.tu.backend.model.dto.UserDto;
import pl.kielce.tu.backend.model.entity.User;

@Component
public class UserMapper {

    public User toUser(UserDto userDto) {
        return User
                .builder()
                .nickname(userDto.getNickname())
                .password(userDto.getPassword())
                .build();
    }

    public UserDto toDto(User user) {
        return UserDto
                .builder()
                .nickname(user.getNickname())
                .build();
    }

}
