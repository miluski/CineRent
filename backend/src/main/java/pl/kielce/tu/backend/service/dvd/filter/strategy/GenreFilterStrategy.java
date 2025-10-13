package pl.kielce.tu.backend.service.dvd.filter.strategy;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import pl.kielce.tu.backend.model.dto.DvdFilterDto;
import pl.kielce.tu.backend.model.entity.Dvd;
import pl.kielce.tu.backend.model.entity.Genre;
import pl.kielce.tu.backend.repository.GenreRepository;

@Component
@RequiredArgsConstructor
public class GenreFilterStrategy implements DvdFilterStrategy {

    private final GenreRepository genreRepository;

    @Override
    public List<Dvd> applyFilter(List<Dvd> dvds, DvdFilterDto filterDto) {
        Set<Genre> targetGenres = collectTargetGenres(filterDto);
        return dvds.stream()
                .filter(dvd -> hasMatchingGenre(dvd, targetGenres))
                .toList();
    }

    @Override
    public boolean canApply(DvdFilterDto filterDto) {
        return hasGenreNames(filterDto) || hasGenreIds(filterDto);
    }

    private Set<Genre> collectTargetGenres(DvdFilterDto filterDto) {
        Set<Genre> targetGenres = new HashSet<>(collectGenresByNames(filterDto));
        targetGenres.addAll(collectGenresById(filterDto));
        return targetGenres;
    }

    private Set<Genre> collectGenresByNames(DvdFilterDto filterDto) {
        if (!hasGenreNames(filterDto)) {
            return Set.of();
        }
        return genreRepository.findByNameIn(filterDto.getGenreNames()).stream()
                .collect(Collectors.toSet());
    }

    private Set<Genre> collectGenresById(DvdFilterDto filterDto) {
        if (!hasGenreIds(filterDto)) {
            return Set.of();
        }
        return genreRepository.findAllById(filterDto.getGenreIds()).stream()
                .collect(Collectors.toSet());
    }

    private boolean hasMatchingGenre(Dvd dvd, Set<Genre> targetGenres) {
        return dvd.getGenres().stream()
                .anyMatch(targetGenres::contains);
    }

    private boolean hasGenreNames(DvdFilterDto filterDto) {
        return filterDto.getGenreNames() != null;
    }

    private boolean hasGenreIds(DvdFilterDto filterDto) {
        return filterDto.getGenreIds() != null;
    }

}
