package pl.kielce.tu.backend.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import pl.kielce.tu.backend.model.dto.RentalDto;
import pl.kielce.tu.backend.service.rental.RentalService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/rentals")
@Tag(name = "Rentals Management", description = "Operations for managing Rentals in the rental system")
public class RentalController {

    private final RentalService rentalService;

    @GetMapping
    @Operation(summary = "Get current user's rentals", description = "Retrieve all rentals for the authenticated user with optional filtering", security = {
            @SecurityRequirement(name = "accessToken") })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Rentals retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid JWT token"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error occurred")
    })
    public ResponseEntity<List<RentalDto>> getUserRentals(
            HttpServletRequest request,
            @Parameter(description = "Filter option: HISTORICAL for inactive rentals, null for active") @RequestParam(required = false) String filter) {
        return rentalService.handleGetUserRentals(request, filter);
    }

    @GetMapping("return-requests")
    @Operation(summary = "Get all return requests (Admin only)", description = "Retrieve all rental return requests for admin review. Shows all rentals with RETURN_REQUESTED status across all users.", security = {
            @SecurityRequirement(name = "accessToken") })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Return requests retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid JWT token"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Admin access required"),
            @ApiResponse(responseCode = "500", description = "Internal server error occurred")
    })
    public ResponseEntity<List<RentalDto>> getAllReturnRequests() {
        return rentalService.handleGetAllReturnRequests();
    }

    @PostMapping("{id}/return-request")
    @Operation(summary = "Request DVD return", description = "Submit a return request for a rented DVD", security = {
            @SecurityRequirement(name = "accessToken") })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "202", description = "Return request accepted and will be processed"),
            @ApiResponse(responseCode = "400", description = "Invalid rental ID or rental not eligible for return"),
            @ApiResponse(responseCode = "404", description = "Rental not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error occurred")
    })
    public ResponseEntity<Void> returnDvd(
            @Parameter(description = "ID of the rental", required = true) @PathVariable(required = true) String id) {
        return rentalService.handleReturnDvd(id);
    }

    @PostMapping("{id}/return-accept")
    @Operation(summary = "Accept DVD return (Admin only)", description = "Accept a DVD return request and automatically create transaction with potential late fees. Increases available DVD copies and finalizes the rental.", security = {
            @SecurityRequirement(name = "accessToken") })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "202", description = "Return accepted and transaction processing initiated"),
            @ApiResponse(responseCode = "400", description = "Invalid rental ID or return not pending"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid JWT token"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Admin access required"),
            @ApiResponse(responseCode = "404", description = "Rental not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error occurred")
    })
    public ResponseEntity<Void> acceptReturn(
            @Parameter(description = "ID of the rental", required = true) @PathVariable(required = true) String id) {
        return rentalService.handleAcceptReturn(id);
    }

    @PostMapping("{id}/return-decline")
    @Operation(summary = "Decline DVD return (Admin only)", description = "Decline a DVD return request and set rental status back to active. The rental continues and no transaction is created.", security = {
            @SecurityRequirement(name = "accessToken") })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "202", description = "Return declined successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid rental ID or return not pending"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid JWT token"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Admin access required"),
            @ApiResponse(responseCode = "404", description = "Rental not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error occurred")
    })
    public ResponseEntity<Void> declineReturn(
            @Parameter(description = "ID of the rental", required = true) @PathVariable(required = true) String id) {
        return rentalService.handleDeclineReturn(id);
    }

}
