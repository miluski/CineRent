package pl.kielce.tu.backend.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.data.domain.Page;

import pl.kielce.tu.backend.model.dto.PagedResponseDto;

class PageMapperTest {

    private PageMapper pageMapper;
    private Page<String> stringPage;
    private Page<TestEntity> entityPage;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        pageMapper = new PageMapper();
        Page<String> mockedStringPage = Mockito.mock(Page.class);
        stringPage = mockedStringPage;
        List<String> stringContent = Arrays.asList("test1", "test2", "test3");
        when(stringPage.getContent()).thenReturn(stringContent);
        when(stringPage.getTotalElements()).thenReturn(10L);
        when(stringPage.getTotalPages()).thenReturn(4);
        when(stringPage.getNumber()).thenReturn(0);
        when(stringPage.getSize()).thenReturn(3);
        when(stringPage.isFirst()).thenReturn(true);
        when(stringPage.isLast()).thenReturn(false);
        when(stringPage.hasNext()).thenReturn(true);
        when(stringPage.hasPrevious()).thenReturn(false);
        Page<TestEntity> mockedEntityPage = Mockito.mock(Page.class);
        entityPage = mockedEntityPage;
        List<TestEntity> entityContent = Arrays.asList(
                new TestEntity(1, "Entity 1"),
                new TestEntity(2, "Entity 2"));
        when(entityPage.getContent()).thenReturn(entityContent);
        when(entityPage.getTotalElements()).thenReturn(5L);
        when(entityPage.getTotalPages()).thenReturn(3);
        when(entityPage.getNumber()).thenReturn(1);
        when(entityPage.getSize()).thenReturn(2);
        when(entityPage.isFirst()).thenReturn(false);
        when(entityPage.isLast()).thenReturn(false);
        when(entityPage.hasNext()).thenReturn(true);
        when(entityPage.hasPrevious()).thenReturn(true);
    }

    @Test
    void toPagedResponse_WithoutMapper_ReturnsCorrectPagedResponse() {

        PagedResponseDto<String> response = pageMapper.toPagedResponse(stringPage);

        assertEquals(3, response.getContent().size());
        assertEquals("test1", response.getContent().get(0));
        assertEquals("test2", response.getContent().get(1));
        assertEquals("test3", response.getContent().get(2));

        verifyPageMetadata(response, 10L, 4, 0, 3, true, false, true, false);
    }

    @Test
    void toPagedResponse_WithMapper_ReturnsCorrectPagedResponse() {

        Function<TestEntity, TestDto> mapper = entity -> new TestDto(entity.getId(), entity.getName().toUpperCase());

        PagedResponseDto<TestDto> response = pageMapper.toPagedResponse(entityPage, mapper);

        assertEquals(2, response.getContent().size());
        assertEquals(1, response.getContent().get(0).getId());
        assertEquals("ENTITY 1", response.getContent().get(0).getName());
        assertEquals(2, response.getContent().get(1).getId());
        assertEquals("ENTITY 2", response.getContent().get(1).getName());

        verifyPageMetadata(response, 5L, 3, 1, 2, false, false, true, true);
    }

    private <T> void verifyPageMetadata(PagedResponseDto<T> response,
            long totalElements, int totalPages,
            int currentPage, int pageSize,
            boolean isFirst, boolean isLast,
            boolean hasNext, boolean hasPrevious) {
        assertEquals(totalElements, response.getTotalElements());
        assertEquals(totalPages, response.getTotalPages());
        assertEquals(currentPage, response.getCurrentPage());
        assertEquals(pageSize, response.getPageSize());
        assertEquals(isFirst, response.isFirst());
        assertEquals(isLast, response.isLast());
        assertEquals(hasNext, response.isHasNext());
        assertEquals(hasPrevious, response.isHasPrevious());
    }

    private static class TestEntity {
        private final int id;
        private final String name;

        public TestEntity(int id, String name) {
            this.id = id;
            this.name = name;
        }

        public int getId() {
            return id;
        }

        public String getName() {
            return name;
        }
    }

    private static class TestDto {
        private final int id;
        private final String name;

        public TestDto(int id, String name) {
            this.id = id;
            this.name = name;
        }

        public int getId() {
            return id;
        }

        public String getName() {
            return name;
        }
    }
}
