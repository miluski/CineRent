package pl.kielce.tu.backend.mapper;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import pl.kielce.tu.backend.model.dto.UserDto;
import pl.kielce.tu.backend.model.entity.User;

@Component
@RequiredArgsConstructor
public class UserMapper {

    private final GenreMapper genreMappingService;

    public User toUser(UserDto userDto) {
        return User
                .builder()
                .nickname(userDto.getNickname())
                .password(userDto.getPassword())
                .age(userDto.getAge())
                .preferredGenres(genreMappingService.mapGenreIdsToGenres(userDto.getPreferredGenresIdentifiers()))
                .build();
    }

    public UserDto toDto(User user) {
        return UserDto
                .builder()
                .nickname(user.getNickname())
                .age(user.getAge())
                .preferredGenres(genreMappingService.mapGenresToNames(user.getPreferredGenres()))
                .build();
    }

}
