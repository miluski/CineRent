package pl.kielce.tu.backend.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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
import pl.kielce.tu.backend.model.dto.ReservationDto;
import pl.kielce.tu.backend.service.reservation.ReservationService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/reservations")
@Tag(name = "Reservations Management", description = "Operations for managing Reservations in the rental system")
public class ReservationController {

    private final ReservationService reservationService;

    @GetMapping
    @Operation(summary = "Get current user's reservations", description = "Retrieve all reservations for the authenticated user with optional status filtering", security = {
            @SecurityRequirement(name = "accessToken") })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Reservations retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid JWT token"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error occurred")
    })
    public ResponseEntity<List<ReservationDto>> getUserReservations(
            HttpServletRequest request,
            @Parameter(description = "Filter by status: CANCELLED, PENDING, ACCEPTED, REJECTED") @RequestParam(required = false) String filter) {
        return reservationService.handleGetUserReservations(request, filter);
    }

    @GetMapping("all")
    @Operation(summary = "Get all reservations (Admin only)", description = "Retrieve all reservations for admin review. Shows all reservations across all users with optional status filtering.", security = {
            @SecurityRequirement(name = "accessToken") })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "All reservations retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid JWT token"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Admin access required"),
            @ApiResponse(responseCode = "500", description = "Internal server error occurred")
    })
    public ResponseEntity<List<ReservationDto>> getAllReservations(
            @Parameter(description = "Filter by status: CANCELLED, PENDING, ACCEPTED, REJECTED") @RequestParam(required = false) String filter) {
        return reservationService.handleGetAllReservations(filter);
    }

    @PostMapping("new")
    @Operation(summary = "Create new reservation", description = "Create a new DVD reservation for the authenticated user", security = {
            @SecurityRequirement(name = "accessToken") })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Reservation created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid reservation data or conflicts detected"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid JWT token"),
            @ApiResponse(responseCode = "404", description = "DVD or user not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error occurred")
    })
    public ResponseEntity<Void> createReservation(
            HttpServletRequest request,
            @Parameter(description = "Reservation details", required = true) @RequestBody(required = true) ReservationDto reservationDto) {
        return reservationService.handleCreateReservation(request, reservationDto);
    }

    @PostMapping("{id}/accept")
    @Operation(summary = "Accept reservation (Admin only)", description = "Accept a reservation request and change status to ACCEPTED. This action creates a rental and decreases available DVD copies.", security = {
            @SecurityRequirement(name = "accessToken") })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "202", description = "Reservation accepted successfully and rental created"),
            @ApiResponse(responseCode = "400", description = "Invalid reservation ID or not pending"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid JWT token"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Admin access required"),
            @ApiResponse(responseCode = "404", description = "Reservation not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error occurred")
    })
    public ResponseEntity<Void> acceptReservation(
            @Parameter(description = "ID of the reservation", required = true) @PathVariable(required = true) String id) {
        return reservationService.handleAcceptReservation(id);
    }

    @PostMapping("{id}/decline")
    @Operation(summary = "Decline reservation (Admin only)", description = "Decline a reservation request and change status to REJECTED. This action rejects the reservation without creating a rental.", security = {
            @SecurityRequirement(name = "accessToken") })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "202", description = "Reservation declined successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid reservation ID or not pending"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid JWT token"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Admin access required"),
            @ApiResponse(responseCode = "404", description = "Reservation not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error occurred")
    })
    public ResponseEntity<Void> declineReservation(
            @Parameter(description = "ID of the reservation", required = true) @PathVariable(required = true) String id) {
        return reservationService.handleDeclineReservation(id);
    }

    @PostMapping("{id}/cancel")
    @Operation(summary = "Cancel reservation", description = "Cancel a pending reservation and return available DVD copies. Only the owner of the reservation can cancel it, and only pending reservations can be cancelled.", security = {
            @SecurityRequirement(name = "accessToken") })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "202", description = "Reservation cancelled successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid reservation ID or not pending"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid JWT token"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Can only cancel own reservations"),
            @ApiResponse(responseCode = "404", description = "Reservation not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error occurred")
    })
    public ResponseEntity<Void> cancelReservation(
            HttpServletRequest request,
            @Parameter(description = "ID of the reservation", required = true) @PathVariable(required = true) String id) {
        return reservationService.handleCancelReservation(request, id);
    }

}
