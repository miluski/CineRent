package pl.kielce.tu.backend.repository.specification;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import pl.kielce.tu.backend.model.dto.DvdFilterDto;
import pl.kielce.tu.backend.model.entity.Dvd;
import pl.kielce.tu.backend.model.entity.Genre;

@Component
public class DvdSpecification {

    public Specification<Dvd> withFilters(DvdFilterDto filterDto) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            addSearchPhraseFilter(filterDto, root, criteriaBuilder, predicates);
            addGenreFilters(filterDto, root, criteriaBuilder, predicates);
            enableDistinctResults(query);

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    private void addSearchPhraseFilter(DvdFilterDto filterDto, Root<Dvd> root,
            CriteriaBuilder criteriaBuilder, List<Predicate> predicates) {
        if (isSearchPhrasePresent(filterDto)) {
            String searchPattern = createSearchPattern(filterDto.getSearchPhrase());
            Predicate searchPredicate = createSearchPredicate(root, criteriaBuilder, searchPattern);
            predicates.add(searchPredicate);
        }
    }

    private boolean isSearchPhrasePresent(DvdFilterDto filterDto) {
        return filterDto.getSearchPhrase() != null && !filterDto.getSearchPhrase().trim().isEmpty();
    }

    private String createSearchPattern(String searchPhrase) {
        return "%" + searchPhrase.toLowerCase() + "%";
    }

    private Predicate createSearchPredicate(Root<Dvd> root, CriteriaBuilder criteriaBuilder, String pattern) {
        Predicate titleMatch = criteriaBuilder.like(criteriaBuilder.lower(root.get("title")), pattern);
        Predicate descriptionMatch = criteriaBuilder.like(criteriaBuilder.lower(root.get("description")), pattern);
        return criteriaBuilder.or(titleMatch, descriptionMatch);
    }

    private void addGenreFilters(DvdFilterDto filterDto, Root<Dvd> root,
            CriteriaBuilder criteriaBuilder, List<Predicate> predicates) {
        if (hasGenreFilters(filterDto)) {
            Join<Dvd, Genre> genreJoin = createGenreJoin(root);
            addGenreIdFilter(filterDto, genreJoin, predicates);
            addGenreNameFilter(filterDto, genreJoin, criteriaBuilder, predicates);
        }
    }

    private boolean hasGenreFilters(DvdFilterDto filterDto) {
        return hasGenreIds(filterDto) || hasGenreNames(filterDto);
    }

    private boolean hasGenreIds(DvdFilterDto filterDto) {
        return filterDto.getGenreIds() != null && !filterDto.getGenreIds().isEmpty();
    }

    private boolean hasGenreNames(DvdFilterDto filterDto) {
        return filterDto.getGenreNames() != null && !filterDto.getGenreNames().isEmpty();
    }

    private Join<Dvd, Genre> createGenreJoin(Root<Dvd> root) {
        return root.join("genres", JoinType.LEFT);
    }

    private void addGenreIdFilter(DvdFilterDto filterDto, Join<Dvd, Genre> genreJoin, List<Predicate> predicates) {
        if (hasGenreIds(filterDto)) {
            predicates.add(genreJoin.get("id").in(filterDto.getGenreIds()));
        }
    }

    private void addGenreNameFilter(DvdFilterDto filterDto, Join<Dvd, Genre> genreJoin,
            CriteriaBuilder criteriaBuilder, List<Predicate> predicates) {
        if (hasGenreNames(filterDto)) {
            List<String> lowerCaseNames = convertToLowerCase(filterDto.getGenreNames());
            predicates.add(criteriaBuilder.lower(genreJoin.get("name")).in(lowerCaseNames));
        }
    }

    private List<String> convertToLowerCase(List<String> names) {
        return names.stream().map(String::toLowerCase).toList();
    }

    private void enableDistinctResults(CriteriaQuery<?> query) {
        if (query != null) {
            query.distinct(true);
        }
    }
}
