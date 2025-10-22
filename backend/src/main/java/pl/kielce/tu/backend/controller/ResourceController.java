package pl.kielce.tu.backend.controller;

import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import pl.kielce.tu.backend.service.resource.ResourceService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/resources")
@Tag(name = "Resource Management", description = "Operations for downloading media resources")
public class ResourceController {

    private final ResourceService resourceService;

    @GetMapping("/posters/{filename}")
    @Operation(summary = "Download DVD poster image", description = """
            Downloads a DVD poster image by filename. The poster images are cached \
            for optimal performance. Supports JPEG, PNG, and WebP formats. \
            Returns the image with appropriate Content-Type headers for browser display. \
            This endpoint is publicly accessible.""")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Poster image retrieved successfully", content = @Content(mediaType = "image/jpeg", schema = @Schema(type = "string", format = "binary"))),
            @ApiResponse(responseCode = "404", description = "Poster image not found", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error occurred", content = @Content)
    })
    public ResponseEntity<Resource> downloadPoster(@PathVariable String filename) {
        return resourceService.handleGetPosterRequest(filename);
    }

    @GetMapping("/avatars/{filename}")
    @Operation(summary = "Download user avatar image", description = """
            Downloads a user avatar image by filename. Avatar images are cached \
            for optimal performance. Supports PNG format. \
            Returns the image with appropriate Content-Type headers for browser display. \
            This endpoint is publicly accessible.""")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Avatar image retrieved successfully", content = @Content(mediaType = "image/png", schema = @Schema(type = "string", format = "binary"))),
            @ApiResponse(responseCode = "404", description = "Avatar image not found", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error occurred", content = @Content)
    })
    public ResponseEntity<Resource> downloadAvatar(@PathVariable String filename) {
        return resourceService.handleGetAvatarRequest(filename);
    }

}
