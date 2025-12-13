package com.rentacar.controller;

import com.rentacar.dto.ReservationRequestDTO;
import com.rentacar.dto.ReservationResponseDTO;
import com.rentacar.service.ReservationService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reservations")
public class ReservationController {

    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    // 1️⃣ MAKE RESERVATION
    @PostMapping
    public ReservationResponseDTO makeReservation(
            @RequestBody ReservationRequestDTO request) {

        return reservationService.makeReservation(request);
    }

    // 2️⃣ RETURN CAR
    @PostMapping("/{reservationNumber}/return")
    public void returnCar(@PathVariable String reservationNumber) {
        reservationService.returnCar(reservationNumber);
    }

    // 3️⃣ CANCEL RESERVATION
    @PostMapping("/{reservationNumber}/cancel")
    public void cancelReservation(@PathVariable String reservationNumber) {
        reservationService.cancelReservation(reservationNumber);
    }

    // 4️⃣ ADD EXTRA TO RESERVATION
    @PostMapping("/{reservationNumber}/extras/{extraCode}")
    public void addExtraToReservation(
            @PathVariable String reservationNumber,
            @PathVariable String extraCode) {

        reservationService.addExtra(reservationNumber, extraCode);
    }

    // 5️⃣ DELETE RESERVATION
    @DeleteMapping("/{reservationNumber}")
    public void deleteReservation(@PathVariable String reservationNumber) {
        reservationService.deleteReservation(reservationNumber);
    }
}
