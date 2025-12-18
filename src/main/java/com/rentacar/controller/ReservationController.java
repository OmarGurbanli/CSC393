package com.rentacar.controller;

import com.rentacar.dto.ReservationRequestDTO;
import com.rentacar.dto.ReservationResponseDTO;
import com.rentacar.service.ReservationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reservations")
@Tag(name = "Reservation Controller", description = "Operations related to reservations: create, return, cancel, extras, delete")
public class ReservationController {

    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @PostMapping
    @Operation(
            summary = "Make a reservation",
            description = "Create a new reservation for a car. Checks car availability before proceeding."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Reservation created successfully"),
            @ApiResponse(responseCode = "406", description = "Car is not available for reservation")
    })
    public ResponseEntity<ReservationResponseDTO> makeReservation(
            @RequestBody ReservationRequestDTO request) {

        ReservationResponseDTO response = reservationService.makeReservation(request);

        if (response == null) {
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(null);
        }

        return ResponseEntity.ok(response);
    }

    @PostMapping("/{reservationNumber}/return")
    @Operation(
            summary = "Return a car",
            description = "Mark a reservation as COMPLETED and update car location if drop-off location differs"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Car returned successfully"),
            @ApiResponse(responseCode = "404", description = "Reservation not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<String> returnCar(
            @Parameter(description = "Reservation number", required = true, example = "12345678")
            @PathVariable String reservationNumber) {

        boolean isReturned = reservationService.returnCar(reservationNumber);

        if (!isReturned) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Reservation not found or cannot be returned");
        }

        return ResponseEntity.ok("Car returned successfully");
    }

    @PostMapping("/{reservationNumber}/cancel")
    @Operation(
            summary = "Cancel a reservation",
            description = "Cancel an active reservation by changing its status to CANCELLED"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Reservation cancelled successfully"),
            @ApiResponse(responseCode = "404", description = "Reservation not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<String> cancelReservation(
            @Parameter(description = "Reservation number", required = true, example = "12345678")
            @PathVariable String reservationNumber) {

        boolean isCancelled = reservationService.cancelReservation(reservationNumber);

        if (!isCancelled) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Reservation not found or cannot be cancelled");
        }

        return ResponseEntity.ok("Reservation cancelled successfully");
    }

    @PostMapping("/{reservationNumber}/extras/{extraCode}")
    @Operation(
            summary = "Add extra to reservation",
            description = "Add an extra service/equipment to an existing reservation"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Extra added successfully"),
            @ApiResponse(responseCode = "404", description = "Extra or reservation not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<String> addExtraToReservation(
            @Parameter(description = "Reservation number", required = true, example = "12345678")
            @PathVariable String reservationNumber,
            @Parameter(description = "Code of the extra service/equipment", required = true, example = "GPS")
            @PathVariable String extraCode) {

        boolean isAdded = reservationService.addExtra(reservationNumber, extraCode);

        if (!isAdded) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Extra or reservation not found, or extra already added");
        }

        return ResponseEntity.ok("Extra added successfully");
    }

    @DeleteMapping("/{reservationNumber}")
    @Operation(
            summary = "Delete a reservation",
            description = "Delete a reservation (disassociate related entities before deletion)"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Reservation deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Reservation not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<String> deleteReservation(
            @Parameter(description = "Reservation number", required = true, example = "12345678")
            @PathVariable String reservationNumber) {

        try {
            boolean isDeleted = reservationService.deleteReservation(reservationNumber);

            if (!isDeleted) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Reservation not found or cannot be deleted");
            }

            return ResponseEntity.ok("Reservation deleted successfully");
        } catch (RuntimeException e) {
            if (e.getMessage() != null && e.getMessage().contains("not found")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Reservation not found with number: " + reservationNumber);
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An error occurred: " + e.getMessage());
        }
    }
}