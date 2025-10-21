package pl.kielce.tu.backend.mapper;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import pl.kielce.tu.backend.model.dto.PagedResponseDto;

@Component
public class PageMapper {

    public <T, R> PagedResponseDto<R> toPagedResponse(Page<T> page, Function<T, R> mapper) {
        List<R> content = page.getContent()
                .stream()
                .map(mapper)
                .collect(Collectors.toList());

        return buildPagedResponse(page, content);
    }

    public <R> PagedResponseDto<R> toPagedResponse(Page<R> page) {
        return buildPagedResponse(page, page.getContent());
    }

    private <R> PagedResponseDto<R> buildPagedResponse(Page<?> page, List<R> content) {
        return PagedResponseDto.<R>builder()
                .content(content)
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .currentPage(page.getNumber())
                .pageSize(page.getSize())
                .first(page.isFirst())
                .last(page.isLast())
                .hasNext(page.hasNext())
                .hasPrevious(page.hasPrevious())
                .build();
    }

}
