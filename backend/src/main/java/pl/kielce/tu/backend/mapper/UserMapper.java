package pl.kielce.tu.backend.mapper;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import pl.kielce.tu.backend.model.dto.UserDto;
import pl.kielce.tu.backend.model.entity.Genre;
import pl.kielce.tu.backend.model.entity.User;
import pl.kielce.tu.backend.repository.GenreRepository;

@Component
@RequiredArgsConstructor
public class UserMapper {

    private final GenreRepository genreRepository;

    public User toUser(UserDto userDto) {
        return User
                .builder()
                .nickname(userDto.getNickname())
                .password(userDto.getPassword())
                .age(userDto.getAge())
                .preferredGenres(mapGenreIdsToGenres(userDto.getPreferredGenresIdentifiers()))
                .build();
    }

    public UserDto toDto(User user) {
        return UserDto
                .builder()
                .nickname(user.getNickname())
                .age(user.getAge())
                .preferredGenres(mapGenresToNames(user.getPreferredGenres()))
                .build();
    }

    private List<Genre> mapGenreIdsToGenres(List<Long> genreIds) {
        if (genreIds == null || genreIds.isEmpty()) {
            return Collections.emptyList();
        }

        return genreIds.stream()
                .map(genreRepository::findById)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    private List<String> mapGenresToNames(List<Genre> genres) {
        if (genres == null || genres.isEmpty()) {
            return Collections.emptyList();
        }

        return genres.stream()
                .map(Genre::getName)
                .collect(Collectors.toList());
    }

}
