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
@Schema(description = "Request object for resending verification code")
public class ResendVerificationRequestDto implements Serializable {

    @NotBlank(message = "Email is required and cannot be blank")
    @Email(message = "Email must be a valid email address")
    @Schema(description = "User's email address to resend verification code", example = "user@example.com", requiredMode = Schema.RequiredMode.REQUIRED)
    private String email;

}
