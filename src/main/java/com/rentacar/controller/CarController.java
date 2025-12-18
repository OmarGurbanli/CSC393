package com.rentacar.controller;

import com.rentacar.dto.CarResponseDTO;
import com.rentacar.dto.CarSearchRequestDTO;
import com.rentacar.dto.RentedCarDTO;
import com.rentacar.service.CarService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cars")
@Tag(name = "Car Controller", description = "Operations related to cars: search, rented cars, delete")
public class CarController {

    private final CarService carService;

    public CarController(CarService carService) {
        this.carService = carService;
    }

    // 1️⃣ SEARCH AVAILABLE CARS
    @PostMapping("/search")
    @Operation(
            summary = "Search available cars",
            description = "Search cars available for rent based on optional filters like category, transmission, price range, dates, etc."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Available cars found"),
            @ApiResponse(responseCode = "404", description = "No available cars found")
    })
    public ResponseEntity<List<CarResponseDTO>> searchAvailableCars(
            @RequestBody CarSearchRequestDTO request) {

        List<CarResponseDTO> cars = carService.searchAvailableCars(request);

        if (cars.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(cars);
        }

        return ResponseEntity.ok(cars);
    }

    // 2️⃣ GET ALL RENTED CARS
    @GetMapping("/rented")
    @Operation(
            summary = "Get all rented cars",
            description = "Retrieve all cars that are currently rented (with ACTIVE reservation status)"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Rented cars found"),
            @ApiResponse(responseCode = "404", description = "No rented cars found")
    })
    public ResponseEntity<List<RentedCarDTO>> getAllRentedCars() {

        List<RentedCarDTO> rentedCars = carService.getAllRentedCars();

        if (rentedCars.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(rentedCars);
        }

        return ResponseEntity.ok(rentedCars);
    }

    // 3️⃣ DELETE CAR
    @DeleteMapping("/{barcode}")
    @Operation(
            summary = "Delete a car",
            description = "Delete a car by barcode if it has never been used in any reservation"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Car deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Car not found"),
            @ApiResponse(responseCode = "406", description = "Car cannot be deleted because it's used in a reservation")
    })
    public ResponseEntity<String> deleteCar(
            @Parameter(description = "Barcode of the car to delete", required = true, example = "CAR001")
            @PathVariable String barcode) {

        try {
            boolean isDeleted = carService.deleteCar(barcode);

            if (!isDeleted) {
                return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE)
                        .body("Car cannot be deleted because it's used in a reservation");
            }

            return ResponseEntity.ok("Car deleted successfully");
        } catch (RuntimeException e) {
            if (e.getMessage() != null && e.getMessage().contains("not found")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Car not found with barcode: " + barcode);
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An error occurred: " + e.getMessage());
        }
    }
}