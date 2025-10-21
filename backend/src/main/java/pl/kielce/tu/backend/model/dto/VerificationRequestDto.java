package pl.kielce.tu.backend.model.dto;

import java.io.Serializable;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request object for email verification")
public class VerificationRequestDto implements Serializable {

    @NotBlank(message = "Email is required and cannot be blank")
    @Email(message = "Email must be a valid email address")
    @Schema(description = "User's email address to verify", example = "user@example.com", requiredMode = Schema.RequiredMode.REQUIRED)
    private String email;

    @NotBlank(message = "Verification code is required and cannot be blank")
    @Schema(description = "6-digit verification code sent to the user's email", example = "123456", requiredMode = Schema.RequiredMode.REQUIRED)
    private String code;

}
