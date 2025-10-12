package pl.kielce.tu.backend.mapper;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import pl.kielce.tu.backend.model.dto.GenreDto;
import pl.kielce.tu.backend.model.entity.Genre;
import pl.kielce.tu.backend.repository.GenreRepository;

@Service
@RequiredArgsConstructor
public class GenreMapper {

    private final GenreRepository genreRepository;

    public Genre toGenre(GenreDto genreDto) {
        return Genre
                .builder()
                .name(genreDto.getName())
                .build();
    }

    public GenreDto toDto(Genre genre) {
        return GenreDto
                .builder()
                .id(genre.getId())
                .name(genre.getName())
                .build();
    }

    public List<String> mapGenresToNames(List<Genre> genres) {
        if (genres == null || genres.isEmpty()) {
            return Collections.emptyList();
        }

        return genres.stream()
                .map(Genre::getName)
                .collect(Collectors.toList());
    }

    public List<Genre> mapGenreIdsToGenres(List<Long> genreIds) {
        if (genreIds == null || genreIds.isEmpty()) {
            return Collections.emptyList();
        }

        return genreIds.stream()
                .map(genreRepository::findById)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

}
