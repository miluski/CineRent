package pl.kielce.tu.backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import pl.kielce.tu.backend.model.dto.ResendVerificationRequestDto;
import pl.kielce.tu.backend.model.dto.VerificationRequestDto;
import pl.kielce.tu.backend.service.verification.VerificationService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/verification")
@Tag(name = "Email Verification", description = "Endpoints for email verification management")
public class VerificationController {

    private final VerificationService verificationService;

    @PostMapping("/verify")
    @Operation(summary = "Verify email with code", description = """
            Verifies a user's email address using the provided verification code. \
            The code must be valid and not expired (15 minutes expiration). \
            Upon successful verification, the user's isVerified flag is set to true \
            and they can log in to the system.""")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Email successfully verified"),
            @ApiResponse(responseCode = "403", description = "Verification failed - invalid or expired code, user not found, or user already verified", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error occurred during verification", content = @Content)
    })
    public ResponseEntity<Void> verifyEmail(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Verification request with email and code", required = true, content = @Content(schema = @Schema(example = """
                    {
                      "email": "filmlover99@example.com",
                      "code": "123456"
                    }"""))) @Valid @RequestBody VerificationRequestDto request) {
        return verificationService.verifyCode(request);
    }

    @PostMapping("/resend")
    @Operation(summary = "Resend verification code", description = """
            Generates a new verification code and sends it to the user's email address. \
            This can be used if the previous code expired or was not received. \
            The old code will be replaced with a new one.""")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Verification code successfully resent"),
            @ApiResponse(responseCode = "500", description = "Internal server error occurred while resending code", content = @Content)
    })
    public ResponseEntity<Void> resendVerificationCode(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Resend request with email address", required = true, content = @Content(schema = @Schema(example = """
                    {
                      "email": "filmlover99@example.com"
                    }"""))) @Valid @RequestBody ResendVerificationRequestDto request) {
        return verificationService.resendVerificationCode(request);
    }

}
