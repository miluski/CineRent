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
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import pl.kielce.tu.backend.model.dto.DvdReminderRequestDto;
import pl.kielce.tu.backend.service.reminder.ReminderService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/reminders")
@Tag(name = "DVD Reminders", description = "Endpoints for managing DVD availability reminders")
public class ReminderController {

    private final ReminderService reminderService;

    @PostMapping
    @Operation(summary = "Create DVD availability reminder", description = """
            Creates a reminder to notify the user when a currently unavailable DVD becomes available. \
            The user must have a verified email address to create reminders. \
            Only one reminder per user-DVD combination is allowed. \
            When the DVD becomes available, the user will receive an email notification.""", security = {
            @SecurityRequirement(name = "accessToken") })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Reminder successfully created"),
            @ApiResponse(responseCode = "403", description = "Forbidden - user email not verified or reminder already exists", content = @Content),
            @ApiResponse(responseCode = "404", description = "DVD not found", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error occurred while creating reminder", content = @Content)
    })
    public ResponseEntity<Void> createReminder(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "DVD reminder request", required = true, content = @Content(schema = @Schema(example = """
                    {
                      "dvdId": 42
                    }"""))) @Valid @RequestBody DvdReminderRequestDto reminderRequest,
            HttpServletRequest httpServletRequest) {
        return reminderService.handleCreateReminder(httpServletRequest, reminderRequest);
    }

}
