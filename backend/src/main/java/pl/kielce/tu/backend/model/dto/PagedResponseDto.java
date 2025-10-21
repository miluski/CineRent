package pl.kielce.tu.backend.model.dto;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Generic paginated response wrapper containing page metadata and content")
public class PagedResponseDto<T> {

    @Schema(description = "List of items in the current page", example = "[]")
    private List<T> content;

    @Schema(description = "Total number of elements across all pages", example = "150")
    private long totalElements;

    @Schema(description = "Total number of pages", example = "8")
    private int totalPages;

    @Schema(description = "Current page number (zero-indexed)", example = "0")
    private int currentPage;

    @Schema(description = "Number of elements per page", example = "20")
    private int pageSize;

    @Schema(description = "Whether this is the first page", example = "true")
    private boolean first;

    @Schema(description = "Whether this is the last page", example = "false")
    private boolean last;

    @Schema(description = "Whether there are more pages after this one", example = "true")
    private boolean hasNext;

    @Schema(description = "Whether there are pages before this one", example = "false")
    private boolean hasPrevious;

}
