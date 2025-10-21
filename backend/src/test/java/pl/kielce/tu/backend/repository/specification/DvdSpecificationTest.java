package pl.kielce.tu.backend.repository.specification;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import pl.kielce.tu.backend.model.dto.DvdFilterDto;
import pl.kielce.tu.backend.model.entity.Dvd;

class DvdSpecificationTest {

    private DvdSpecification dvdSpecification;
    private Root<Dvd> root;
    private CriteriaQuery<?> query;
    private CriteriaBuilder criteriaBuilder;

    @SuppressWarnings("unchecked")
    @BeforeEach
    void setUp() {
        dvdSpecification = new DvdSpecification();
        root = mock(Root.class);
        query = mock(CriteriaQuery.class);
        criteriaBuilder = mock(CriteriaBuilder.class);
    }

    @SuppressWarnings("unchecked")
    @Test
    void withFilters_withSearchPhrase_createsPredicateWithTitleAndDescriptionSearch() {
        Path<Object> titlePath = mock(Path.class);
        Path<Object> descriptionPath = mock(Path.class);
        Expression<String> lowerTitle = mock(Expression.class);
        Expression<String> lowerDescription = mock(Expression.class);
        Predicate titlePredicate = mock(Predicate.class);
        Predicate descriptionPredicate = mock(Predicate.class);
        Predicate orPredicate = mock(Predicate.class);

        when(root.get("title")).thenReturn(titlePath);
        when(root.get("description")).thenReturn(descriptionPath);
        when(criteriaBuilder.lower(any())).thenReturn(lowerTitle, lowerDescription);
        when(criteriaBuilder.like(any(Expression.class), anyString())).thenReturn(titlePredicate, descriptionPredicate);
        when(criteriaBuilder.or(titlePredicate, descriptionPredicate)).thenReturn(orPredicate);
        when(criteriaBuilder.and(any(Predicate[].class))).thenReturn(mock(Predicate.class));

        DvdFilterDto filterDto = DvdFilterDto.builder()
                .searchPhrase("matrix")
                .build();

        Specification<Dvd> specification = dvdSpecification.withFilters(filterDto);
        specification.toPredicate(root, query, criteriaBuilder);

        verify(criteriaBuilder, times(2)).like(any(Expression.class), anyString());
        verify(criteriaBuilder).or(titlePredicate, descriptionPredicate);
        verify(query).distinct(true);
    }

    @SuppressWarnings("unchecked")
    @Test
    void withFilters_withGenreIds_createsPredicateWithGenreJoinAndIdFilter() {
        Join<Object, Object> genreJoin = mock(Join.class);
        Path<Object> genreIdPath = mock(Path.class);
        Predicate genreIdPredicate = mock(Predicate.class);

        when(root.join("genres", JoinType.LEFT)).thenReturn(genreJoin);
        when(genreJoin.get("id")).thenReturn(genreIdPath);
        when(genreIdPath.in(Arrays.asList(1L, 2L, 3L))).thenReturn(genreIdPredicate);
        when(criteriaBuilder.and(any(Predicate[].class))).thenReturn(mock(Predicate.class));

        DvdFilterDto filterDto = DvdFilterDto.builder()
                .genreIds(Arrays.asList(1L, 2L, 3L))
                .build();

        Specification<Dvd> specification = dvdSpecification.withFilters(filterDto);
        specification.toPredicate(root, query, criteriaBuilder);

        verify(root).join("genres", JoinType.LEFT);
        verify(genreIdPath).in(Arrays.asList(1L, 2L, 3L));
        verify(query).distinct(true);
    }

    @SuppressWarnings("unchecked")
    @Test
    void withFilters_withGenreNames_createsPredicateWithGenreJoinAndNameFilter() {
        Join<Object, Object> genreJoin = mock(Join.class);
        Path<Object> genreNamePath = mock(Path.class);
        Expression<String> lowerGenreName = mock(Expression.class);
        Predicate genreNamePredicate = mock(Predicate.class);

        when(root.join("genres", JoinType.LEFT)).thenReturn(genreJoin);
        when(genreJoin.get("name")).thenReturn(genreNamePath);
        when(criteriaBuilder.lower(any())).thenReturn(lowerGenreName);
        when(lowerGenreName.in(Arrays.asList("action", "sci-fi"))).thenReturn(genreNamePredicate);
        when(criteriaBuilder.and(any(Predicate[].class))).thenReturn(mock(Predicate.class));

        DvdFilterDto filterDto = DvdFilterDto.builder()
                .genreNames(Arrays.asList("Action", "Sci-Fi"))
                .build();

        Specification<Dvd> specification = dvdSpecification.withFilters(filterDto);
        specification.toPredicate(root, query, criteriaBuilder);

        verify(root).join("genres", JoinType.LEFT);
        verify(criteriaBuilder).lower(any());
        verify(lowerGenreName).in(Arrays.asList("action", "sci-fi"));
        verify(query).distinct(true);
    }

    @SuppressWarnings("unchecked")
    @Test
    void withFilters_withAllFilters_combinesAllPredicates() {
        Path<Object> titlePath = mock(Path.class);
        Path<Object> descriptionPath = mock(Path.class);
        Expression<String> lowerExpression = mock(Expression.class);
        Join<Object, Object> genreJoin = mock(Join.class);
        Path<Object> genreIdPath = mock(Path.class);
        Path<Object> genreNamePath = mock(Path.class);
        Predicate searchPredicate = mock(Predicate.class);
        Predicate genreIdPredicate = mock(Predicate.class);
        Predicate genreNamePredicate = mock(Predicate.class);

        when(root.get("title")).thenReturn(titlePath);
        when(root.get("description")).thenReturn(descriptionPath);
        when(criteriaBuilder.lower(any())).thenReturn(lowerExpression);
        when(criteriaBuilder.like(any(Expression.class), anyString())).thenReturn(mock(Predicate.class));
        when(criteriaBuilder.or(any(Predicate.class), any(Predicate.class))).thenReturn(searchPredicate);
        when(root.join("genres", JoinType.LEFT)).thenReturn(genreJoin);
        when(genreJoin.get("id")).thenReturn(genreIdPath);
        when(genreJoin.get("name")).thenReturn(genreNamePath);
        when(genreIdPath.in(any(List.class))).thenReturn(genreIdPredicate);
        when(lowerExpression.in(any(List.class))).thenReturn(genreNamePredicate);
        when(criteriaBuilder.and(any(Predicate[].class))).thenReturn(mock(Predicate.class));

        DvdFilterDto filterDto = DvdFilterDto.builder()
                .searchPhrase("matrix")
                .genreIds(Arrays.asList(1L, 2L))
                .genreNames(Arrays.asList("Action"))
                .build();

        Specification<Dvd> specification = dvdSpecification.withFilters(filterDto);
        specification.toPredicate(root, query, criteriaBuilder);

        verify(criteriaBuilder, times(2)).like(any(Expression.class), anyString());
        verify(root).join("genres", JoinType.LEFT);
        verify(genreIdPath).in(any(List.class));
        verify(lowerExpression).in(any(List.class));
        verify(query).distinct(true);
    }

    @Test
    void withFilters_withEmptyFilter_createsSpecificationWithEmptyPredicates() {
        when(criteriaBuilder.and(any(Predicate[].class))).thenReturn(mock(Predicate.class));

        DvdFilterDto filterDto = DvdFilterDto.builder().build();

        Specification<Dvd> specification = dvdSpecification.withFilters(filterDto);
        Predicate result = specification.toPredicate(root, query, criteriaBuilder);

        assertNotNull(result);
        verify(root, never()).get(anyString());
        verify(root, never()).join(anyString(), any(JoinType.class));
        verify(query).distinct(true);
    }

    @SuppressWarnings("unchecked")
    @Test
    void withFilters_withNullSearchPhrase_skipsSearchPredicate() {
        Join<Object, Object> genreJoin = mock(Join.class);
        Path<Object> genreIdPath = mock(Path.class);

        when(root.join("genres", JoinType.LEFT)).thenReturn(genreJoin);
        when(genreJoin.get("id")).thenReturn(genreIdPath);
        when(genreIdPath.in(any(List.class))).thenReturn(mock(Predicate.class));
        when(criteriaBuilder.and(any(Predicate[].class))).thenReturn(mock(Predicate.class));

        DvdFilterDto filterDto = DvdFilterDto.builder()
                .searchPhrase(null)
                .genreIds(Arrays.asList(1L))
                .build();

        Specification<Dvd> specification = dvdSpecification.withFilters(filterDto);
        specification.toPredicate(root, query, criteriaBuilder);

        verify(criteriaBuilder, never()).like(any(), anyString());
        verify(root).join("genres", JoinType.LEFT);
    }

    @Test
    void withFilters_withEmptySearchPhrase_skipsSearchPredicate() {
        when(criteriaBuilder.and(any(Predicate[].class))).thenReturn(mock(Predicate.class));

        DvdFilterDto filterDto = DvdFilterDto.builder()
                .searchPhrase("")
                .build();

        Specification<Dvd> specification = dvdSpecification.withFilters(filterDto);
        specification.toPredicate(root, query, criteriaBuilder);

        verify(criteriaBuilder, never()).like(any(), anyString());
        verify(root, never()).join(anyString(), any(JoinType.class));
    }

    @Test
    void withFilters_withWhitespaceSearchPhrase_skipsSearchPredicate() {
        when(criteriaBuilder.and(any(Predicate[].class))).thenReturn(mock(Predicate.class));

        DvdFilterDto filterDto = DvdFilterDto.builder()
                .searchPhrase("   ")
                .build();

        Specification<Dvd> specification = dvdSpecification.withFilters(filterDto);
        specification.toPredicate(root, query, criteriaBuilder);

        verify(criteriaBuilder, never()).like(any(), anyString());
        verify(root, never()).join(anyString(), any(JoinType.class));
    }

    @Test
    void withFilters_withEmptyGenreLists_skipsGenreFilters() {
        when(criteriaBuilder.and(any(Predicate[].class))).thenReturn(mock(Predicate.class));

        DvdFilterDto filterDto = DvdFilterDto.builder()
                .genreIds(List.of())
                .genreNames(List.of())
                .build();

        Specification<Dvd> specification = dvdSpecification.withFilters(filterDto);
        specification.toPredicate(root, query, criteriaBuilder);

        verify(root, never()).join(anyString(), any(JoinType.class));
    }

    @SuppressWarnings("unchecked")
    @Test
    void withFilters_withBothGenreIdsAndNames_appliesBothFilters() {
        Join<Object, Object> genreJoin = mock(Join.class);
        Path<Object> genreIdPath = mock(Path.class);
        Path<Object> genreNamePath = mock(Path.class);
        Expression<String> lowerGenreName = mock(Expression.class);

        when(root.join("genres", JoinType.LEFT)).thenReturn(genreJoin);
        when(genreJoin.get("id")).thenReturn(genreIdPath);
        when(genreJoin.get("name")).thenReturn(genreNamePath);
        when(criteriaBuilder.lower(any())).thenReturn(lowerGenreName);
        when(genreIdPath.in(any(List.class))).thenReturn(mock(Predicate.class));
        when(lowerGenreName.in(any(List.class))).thenReturn(mock(Predicate.class));
        when(criteriaBuilder.and(any(Predicate[].class))).thenReturn(mock(Predicate.class));

        DvdFilterDto filterDto = DvdFilterDto.builder()
                .genreIds(Arrays.asList(1L, 2L))
                .genreNames(Arrays.asList("Action", "Drama"))
                .build();

        Specification<Dvd> specification = dvdSpecification.withFilters(filterDto);
        specification.toPredicate(root, query, criteriaBuilder);

        verify(genreIdPath).in(Arrays.asList(1L, 2L));
        verify(lowerGenreName).in(Arrays.asList("action", "drama"));
    }

    @SuppressWarnings("unchecked")
    @Test
    void withFilters_withMixedCaseGenreNames_convertsToLowerCase() {
        Join<Object, Object> genreJoin = mock(Join.class);
        Path<Object> genreNamePath = mock(Path.class);
        Expression<String> lowerGenreName = mock(Expression.class);

        when(root.join("genres", JoinType.LEFT)).thenReturn(genreJoin);
        when(genreJoin.get("name")).thenReturn(genreNamePath);
        when(criteriaBuilder.lower(any())).thenReturn(lowerGenreName);
        when(lowerGenreName.in(any(List.class))).thenReturn(mock(Predicate.class));
        when(criteriaBuilder.and(any(Predicate[].class))).thenReturn(mock(Predicate.class));

        DvdFilterDto filterDto = DvdFilterDto.builder()
                .genreNames(Arrays.asList("ACTION", "ScI-Fi", "drama"))
                .build();

        Specification<Dvd> specification = dvdSpecification.withFilters(filterDto);
        specification.toPredicate(root, query, criteriaBuilder);

        verify(lowerGenreName).in(Arrays.asList("action", "sci-fi", "drama"));
    }

    @SuppressWarnings("unchecked")
    @Test
    void withFilters_withSearchPhrase_convertsToLowerCasePattern() {
        Path<Object> titlePath = mock(Path.class);
        Path<Object> descriptionPath = mock(Path.class);
        Expression<String> lowerExpression = mock(Expression.class);

        when(root.get("title")).thenReturn(titlePath);
        when(root.get("description")).thenReturn(descriptionPath);
        when(criteriaBuilder.lower(any())).thenReturn(lowerExpression);
        when(criteriaBuilder.like(any(Expression.class), anyString())).thenReturn(mock(Predicate.class));
        when(criteriaBuilder.or(any(Predicate.class), any(Predicate.class))).thenReturn(mock(Predicate.class));
        when(criteriaBuilder.and(any(Predicate[].class))).thenReturn(mock(Predicate.class));

        DvdFilterDto filterDto = DvdFilterDto.builder()
                .searchPhrase("MATRIX")
                .build();

        Specification<Dvd> specification = dvdSpecification.withFilters(filterDto);
        specification.toPredicate(root, query, criteriaBuilder);

        verify(criteriaBuilder, times(2)).like(any(Expression.class), anyString());
    }

    @Test
    void withFilters_alwaysEnablesDistinctResults() {
        when(criteriaBuilder.and(any(Predicate[].class))).thenReturn(mock(Predicate.class));

        DvdFilterDto filterDto = DvdFilterDto.builder()
                .searchPhrase("test")
                .build();

        Specification<Dvd> specification = dvdSpecification.withFilters(filterDto);
        specification.toPredicate(root, query, criteriaBuilder);

        verify(query).distinct(true);
    }

    @Test
    void withFilters_withNullQuery_handlesGracefully() {
        when(criteriaBuilder.and(any(Predicate[].class))).thenReturn(mock(Predicate.class));

        DvdFilterDto filterDto = DvdFilterDto.builder().build();

        Specification<Dvd> specification = dvdSpecification.withFilters(filterDto);
        Predicate result = specification.toPredicate(root, null, criteriaBuilder);

        assertNotNull(result);
    }

}
