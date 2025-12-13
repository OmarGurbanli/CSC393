package com.rentacar.controller;

import com.rentacar.dto.CarResponseDTO;
import com.rentacar.dto.CarSearchRequestDTO;
import com.rentacar.dto.RentedCarDTO;
import com.rentacar.service.CarService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cars")
public class CarController {

    private final CarService carService;

    public CarController(CarService carService) {
        this.carService = carService;
    }

    // 1️⃣ SEARCH AVAILABLE CARS
    @PostMapping("/search")
    public List<CarResponseDTO> searchAvailableCars(
            @RequestBody CarSearchRequestDTO request) {

        return carService.searchAvailableCars(request);
    }

    // 2️⃣ GET ALL RENTED CARS
    @GetMapping("/rented")
    public List<RentedCarDTO> getAllRentedCars() {
        return carService.getAllRentedCars();
    }

    // 3️⃣ DELETE CAR
    @DeleteMapping("/{barcode}")
    public void deleteCar(@PathVariable String barcode) {
        carService.deleteCar(barcode);
    }
}
