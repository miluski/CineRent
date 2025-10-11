package pl.kielce.tu.backend.model.dto;

import java.io.Serializable;
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
@Schema(description = "User data transfer object")
public class UserDto implements Serializable {

    @Schema(description = "User's unique nickname", example = "john_doe", requiredMode = Schema.RequiredMode.REQUIRED)
    private String nickname;

    @Schema(description = "User's password", example = "SecurePass123!", requiredMode = Schema.RequiredMode.REQUIRED)
    private String password;

    @Schema(description = "User's age", example = "24", requiredMode = Schema.RequiredMode.REQUIRED, minimum = "1", maximum = "149")
    private Integer age;

    @Schema(description = "List of preferred genre identifiers", example = "[1, 10, 21, 37]")
    private List<Long> preferredGenresIdentifiers;

    @Schema(description = "List of preferred genre names", example = "[\"Komedia\", \"Sci-Fi\", \"Akcja\"]")
    private List<String> preferredGenres;

}
