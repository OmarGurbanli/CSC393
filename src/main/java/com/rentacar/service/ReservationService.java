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

    // -------------------------
    // BASIC RESERVATION METHODS
    // -------------------------

    public List<Reservation> getAllReservations() {
        return reservationRepository.findAll();
    }

    public Reservation getReservationById(Long id) {
        return reservationRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Reservation not found with id: " + id));
    }

    public Reservation getReservationByNumber(String reservationNumber) {
        return reservationRepository.findByReservationNumber(reservationNumber)
                .orElseThrow(() ->
                        new RuntimeException("Reservation not found with number: " + reservationNumber));
    }

    public List<Reservation> getReservationsByStatus(ReservationStatus status) {
        return reservationRepository.findByStatus(status);
    }

    // -------------------------
    // RESERVATION CREATION (ENTITY BASED)
    // -------------------------

    public Reservation makeReservation(Long carId, Long memberId,
                                       String pickupLocationCode,
                                       String dropoffLocationCode,
                                       LocalDateTime pickupDate,
                                       LocalDateTime dropoffDate,
                                       List<Long> extraIds) {

        Car car = carRepository.findById(carId)
                .orElseThrow(() -> new RuntimeException("Car not found"));

        if (!"AVAILABLE".equals(car.getStatus())) {
            throw new RuntimeException("Car is not available for reservation");
        }

        boolean hasConflict =
                reservationRepository.existsActiveReservationForCar(carId, pickupDate, dropoffDate);

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
        reservation.setCreationDate(LocalDateTime.now());

        if (extraIds != null && !extraIds.isEmpty()) {
            List<Extra> extras = extraRepository.findAllById(extraIds);
            reservation.setExtras(extras);
        }

        car.setStatus("RESERVED");
        carRepository.save(car);

        return reservationRepository.save(reservation);
    }

    // -------------------------
    // EXTRA OPERATIONS
    // -------------------------

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

    // Used by REST controller (extraCode based)
    public boolean addExtra(String reservationNumber, String extraCode) {

        Reservation reservation = getReservationByNumber(reservationNumber);
        Extra extra = extraRepository.findByName(extraCode)
                .orElse(null);

        if (extra == null || reservation.getExtras().contains(extra)) {
            return false;
        }

        reservation.getExtras().add(extra);
        reservationRepository.save(reservation);
        return true;
    }

    // -------------------------
    // RESERVATION STATE CHANGES
    // -------------------------

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

    // -------------------------
    // QUERY / HELPER METHODS
    // -------------------------

    public List<Reservation> getCurrentlyActiveReservations() {
        return reservationRepository.findCurrentlyActiveReservations();
    }

    public List<Car> getAllRentedCars() {
        return carRepository.findCurrentlyRentedCars();
    }

    public Double calculateTotalPrice(String reservationNumber) {
        Reservation reservation = getReservationByNumber(reservationNumber);
        return reservation.calculateTotalPrice();
    }

    // -------------------------
    // DTO BASED METHOD (USED BY CONTROLLER)
    // -------------------------

    public ReservationResponseDTO makeReservation(ReservationRequestDTO request) {

        Car car = carRepository.findByBarcode(request.getCarBarcode())
                .orElseThrow(() ->
                        new RuntimeException("Car not found with barcode: " + request.getCarBarcode()));

        if (!"AVAILABLE".equals(car.getStatus())) {
            return null;
        }

        boolean hasConflict =
                reservationRepository.existsActiveReservationForCar(
                        car.getId(),
                        request.getPickupDateTime(),
                        request.getDropoffDateTime()
                );

        if (hasConflict) {
            return null;
        }

        Member member = memberRepository.findById(request.getMemberId())
                .orElseThrow(() -> new RuntimeException("Member not found"));

        Location pickupLocation = locationRepository.findByCode(request.getPickupLocationCode())
                .orElseThrow(() -> new RuntimeException("Pickup location not found"));

        Location dropoffLocation = locationRepository.findByCode(request.getDropoffLocationCode())
                .orElseThrow(() -> new RuntimeException("Dropoff location not found"));

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

        if (request.getExtraCodes() != null && !request.getExtraCodes().isEmpty()) {
            List<Extra> extras = new ArrayList<>();
            for (String extraCode : request.getExtraCodes()) {
                Extra extra = extraRepository.findByName(extraCode)
                        .orElseThrow(() ->
                                new RuntimeException("Extra not found: " + extraCode));
                extras.add(extra);
            }
            reservation.setExtras(extras);
        }

        reservationRepository.save(reservation);

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

    // -------------------------
    // INTERNAL UTILITY
    // -------------------------

    private String generateReservationNumber() {
        Random random = new Random();
        String number;
        do {
            number = String.format("%08d", random.nextInt(100000000));
        } while (reservationRepository.findByReservationNumber(number).isPresent());
        return number;
    }
}
