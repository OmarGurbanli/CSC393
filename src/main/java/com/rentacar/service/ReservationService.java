package com.rentacar.service;

import com.rentacar.dto.ReservationRequestDTO;
import com.rentacar.dto.ReservationResponseDTO;
import com.rentacar.model.*;
import com.rentacar.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service
@RequiredArgsConstructor
@Transactional
public class ReservationService {
    private final ReservationRepository reservationRepository;
    private final CarRepository carRepository;
    private final MemberRepository memberRepository;
    private final LocationRepository locationRepository;
    private final ExtraRepository extraRepository;

    public List<Reservation> getAllReservations() {
        return reservationRepository.findAll();
    }

    public Reservation getReservationById(Long id) {
        return reservationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Reservation not found with id: " + id));
    }

    public Reservation getReservationByNumber(String reservationNumber) {
        return reservationRepository.findByReservationNumber(reservationNumber)
                .orElseThrow(() -> new RuntimeException("Reservation not found with number: " + reservationNumber));
    }

    public List<Reservation> getReservationsByStatus(ReservationStatus status) {
        return reservationRepository.findByStatus(status);
    }

    public Reservation makeReservation(Long carId, Long memberId, String pickupLocationCode,
                                       String dropoffLocationCode, LocalDateTime pickupDate,
                                       LocalDateTime dropoffDate, List<Long> extraIds) {
        Car car = carRepository.findById(carId)
                .orElseThrow(() -> new RuntimeException("Car not found"));

        if (!"AVAILABLE".equals(car.getStatus())) {
            throw new RuntimeException("Car is not available for reservation");
        }

        boolean hasConflict = reservationRepository.existsActiveReservationForCar(carId, pickupDate, dropoffDate);
        if (hasConflict) {
            throw new RuntimeException("Car is already reserved for the selected dates");
        }

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("Member not found"));
        Location pickupLocation = locationRepository.findByCode(pickupLocationCode)
                .orElseThrow(() -> new RuntimeException("Pickup location not found"));
        Location dropoffLocation = locationRepository.findByCode(dropoffLocationCode)
                .orElseThrow(() -> new RuntimeException("Dropoff location not found"));

        Reservation reservation = new Reservation();
        reservation.setReservationNumber(generateReservationNumber());
        reservation.setCar(car);
        reservation.setMember(member);
        reservation.setPickupLocation(pickupLocation);
        reservation.setDropoffLocation(dropoffLocation);
        reservation.setPickupDate(pickupDate);
        reservation.setDropoffDate(dropoffDate);
        reservation.setStatus(ReservationStatus.ACTIVE);

        // Добавление дополнительных услуг
        if (extraIds != null && !extraIds.isEmpty()) {
            List<Extra> extras = extraRepository.findAllById(extraIds);
            reservation.setExtras(extras);
        }

        car.setStatus("RESERVED");
        carRepository.save(car);

        return reservationRepository.save(reservation);
    }

    public boolean addExtraToReservation(String reservationNumber, Long extraId) {
        Reservation reservation = getReservationByNumber(reservationNumber);
        Extra extra = extraRepository.findById(extraId)
                .orElseThrow(() -> new RuntimeException("Extra not found"));

        if (reservation.getExtras().contains(extra)) {
            return false;
        }

        reservation.getExtras().add(extra);
        reservationRepository.save(reservation);
        return true;
    }

    public boolean cancelReservation(String reservationNumber) {
        Reservation reservation = getReservationByNumber(reservationNumber);

        if (reservation.getStatus() != ReservationStatus.ACTIVE) {
            return false;
        }

        reservation.setStatus(ReservationStatus.CANCELLED);

        Car car = reservation.getCar();
        car.setStatus("AVAILABLE");
        carRepository.save(car);

        reservationRepository.save(reservation);
        return true;
    }

    public boolean returnCar(String reservationNumber) {
        Reservation reservation = getReservationByNumber(reservationNumber);

        if (reservation.getStatus() != ReservationStatus.ACTIVE) {
            return false;
        }

        reservation.setStatus(ReservationStatus.COMPLETED);
        reservation.setReturnDate(LocalDateTime.now());

        Car car = reservation.getCar();
        car.setStatus("AVAILABLE");
        car.setLocation(reservation.getDropoffLocation());
        carRepository.save(car);

        reservationRepository.save(reservation);
        return true;
    }

    public boolean deleteReservation(String reservationNumber) {
        Reservation reservation = getReservationByNumber(reservationNumber);

        if (reservation.getStatus() == ReservationStatus.ACTIVE) {
            return false;
        }

        reservation.setCar(null);
        reservation.setMember(null);
        reservation.setPickupLocation(null);
        reservation.setDropoffLocation(null);
        reservation.getExtras().clear();

        reservationRepository.delete(reservation);
        return true;
    }

    public List<Reservation> getCurrentlyActiveReservations() {
        return reservationRepository.findCurrentlyActiveReservations();
    }

    public List<Car> getAllRentedCars() {
        return carRepository.findCurrentlyRentedCars();
    }

    private String generateReservationNumber() {
        Random random = new Random();
        String number;
        do {
            number = String.format("%08d", random.nextInt(100000000));
        } while (reservationRepository.findByReservationNumber(number).isPresent());
        return number;
    }

    public Double calculateTotalPrice(String reservationNumber) {
        Reservation reservation = getReservationByNumber(reservationNumber);
        return reservation.calculateTotalPrice();
    }

    // DTO-based methods for REST API
    public ReservationResponseDTO makeReservation(ReservationRequestDTO request) {
        // Find car by barcode
        Car car = carRepository.findByBarcode(request.getCarBarcode())
                .orElseThrow(() -> new RuntimeException("Car not found with barcode: " + request.getCarBarcode()));

        // Check car status
        if (!"AVAILABLE".equals(car.getStatus())) {
            return null; // Return null to indicate car is not available (406 status)
        }

        // Check for date conflicts
        boolean hasConflict = reservationRepository.existsActiveReservationForCar(
                car.getId(), request.getPickupDateTime(), request.getDropoffDateTime());
        if (hasConflict) {
            return null; // Car is already reserved for these dates
        }

        // Find member
        Member member = memberRepository.findById(request.getMemberId())
                .orElseThrow(() -> new RuntimeException("Member not found"));

        // Find locations
        Location pickupLocation = locationRepository.findByCode(request.getPickupLocationCode())
                .orElseThrow(() -> new RuntimeException("Pickup location not found"));
        Location dropoffLocation = locationRepository.findByCode(request.getDropoffLocationCode())
                .orElseThrow(() -> new RuntimeException("Dropoff location not found"));

        // Create reservation
        Reservation reservation = new Reservation();
        reservation.setReservationNumber(generateReservationNumber());
        reservation.setCar(car);
        reservation.setMember(member);
        reservation.setPickupLocation(pickupLocation);
        reservation.setDropoffLocation(dropoffLocation);
        reservation.setPickupDate(request.getPickupDateTime());
        reservation.setDropoffDate(request.getDropoffDateTime());
        reservation.setStatus(ReservationStatus.ACTIVE);
        reservation.setCreationDate(LocalDateTime.now());

        // Add extras if provided
        if (request.getExtraCodes() != null && !request.getExtraCodes().isEmpty()) {
            List<Extra> extras = new ArrayList<>();
            for (String extraCode : request.getExtraCodes()) {
                Extra extra = extraRepository.findByName(extraCode)
                        .orElseThrow(() -> new RuntimeException("Extra not found: " + extraCode));
                extras.add(extra);
            }
            reservation.setExtras(extras);
        }

        // Update car status - but wait, the requirement says to check status, not change it
        // Actually, we should not change car status to RESERVED, we just check it's AVAILABLE
        // The car remains AVAILABLE until pickup date

        reservation = reservationRepository.save(reservation);

        // Convert to DTO
        ReservationResponseDTO response = new ReservationResponseDTO();
        response.setReservationNumber(reservation.getReservationNumber());
        response.setPickupDateTime(reservation.getPickupDate());
        response.setDropoffDateTime(reservation.getDropoffDate());
        response.setPickupLocationCode(reservation.getPickupLocation().getCode());
        response.setPickupLocationName(reservation.getPickupLocation().getName());
        response.setDropoffLocationCode(reservation.getDropoffLocation().getCode());
        response.setDropoffLocationName(reservation.getDropoffLocation().getName());
        response.setTotalAmount(reservation.calculateTotalPrice());
        response.setMemberId(reservation.getMember().getId());
        response.setMemberName(reservation.getMember().getName());

        return response;
    }

    public boolean addExtra(String reservationNumber, String extraCode) {
        try {
            Reservation reservation = getReservationByNumber(reservationNumber);
            Extra extra = extraRepository.findByName(extraCode)
                    .orElse(null);

            if (extra == null) {
                return false; // Extra not found
            }

            if (reservation.getExtras().contains(extra)) {
                return false; // Extra already added
            }

            reservation.getExtras().add(extra);
            reservationRepository.save(reservation);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}