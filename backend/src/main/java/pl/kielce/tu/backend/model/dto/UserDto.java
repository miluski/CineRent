package pl.kielce.tu.backend.model.dto;

import java.io.Serializable;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
@Schema(description = "User data transfer object")
public class UserDto implements Serializable {

    @Schema(description = "User's unique nickname", example = "john_doe", requiredMode = Schema.RequiredMode.REQUIRED)
    private String nickname;

    @Schema(description = "User's password", example = "SecurePass123!", requiredMode = Schema.RequiredMode.REQUIRED)
    private String password;

    @Schema(description = "User's rank/role in the system", example = "USER", allowableValues = { "USER", "ADMIN" })
    private String rank;

    @Schema(description = "Flag to indicate if user wants to stay logged in (extended session)", example = "false")
    private boolean isRemembered;

}
