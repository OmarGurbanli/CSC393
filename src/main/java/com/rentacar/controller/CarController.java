package com.rentacar.controller;

import com.rentacar.dto.CarResponseDTO;
import com.rentacar.dto.CarSearchRequestDTO;
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

    // SEARCH AVAILABLE CARS
    @PostMapping("/search")
    public List<CarResponseDTO> searchAvailableCars(
            @RequestBody CarSearchRequestDTO request) {

        return carService.searchAvailableCars(request);
    }
}
