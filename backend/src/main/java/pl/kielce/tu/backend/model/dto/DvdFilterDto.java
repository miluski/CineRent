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
@Schema(description = "DVD filtering parameters for searching and filtering DVD collections")
public class DvdFilterDto {

    @Schema(description = "Search phrase to match against DVD title and description", example = "matrix")
    private String searchPhrase;

    @Schema(description = "List of genre names to filter DVDs by", example = "[\"Action\", \"Sci-Fi\"]")
    private List<String> genreNames;

    @Schema(description = "List of genre identifiers to filter DVDs by", example = "[1, 2]")
    private List<Long> genreIds;

}
