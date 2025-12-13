package com.rentacar;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@Import({ReservationService.class, CarService.class, MemberService.class, LocationService.class, ExtraService.class})
class ReservationServiceTest {

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private CarService carService;

    @Autowired
    private MemberService memberService;

    @Autowired
    private LocationService locationService;

    @Autowired
    private ExtraService extraService;

    @Autowired
    private CarRepository carRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private LocationRepository locationRepository;

    @Autowired
    private ExtraRepository extraRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    private Location testLocation;
    private Car testCar;
    private Member testMember;
    private Extra testExtra;

    @BeforeEach
    void setUp() {
        // Create test location
        testLocation = new Location("TEST", "Test Location");
        locationRepository.save(testLocation);

        // Create test car
        testCar = new Car();
        testCar.setBarcode("TESTCAR");
        testCar.setLicensePlate("34TEST");
        testCar.setBrand("Toyota");
        testCar.setModel("Corolla");
        testCar.setNumberOfSeats(5);
        testCar.setTransmissionType("Automatic");
        testCar.setDailyPrice(1200.0);
        testCar.setCategory("Mid-size");
        testCar.setStatus("AVAILABLE");
        testCar.setLocation(testLocation);
        carRepository.save(testCar);

        // Create test member
        testMember = new Member();
        testMember.setName("Test Member");
        testMember.setEmail("test@example.com");
        testMember.setPhone("+905551111111");
        testMember.setDrivingLicenseNumber("DL999999");
        memberRepository.save(testMember);

        // Create test extra
        testExtra = new Extra("GPS Navigation", 200.0);
        extraRepository.save(testExtra);
    }

    @Test
    void makeReservation_CreatesReservationSuccessfully() {
        LocalDateTime pickup = LocalDateTime.now().plusDays(1);
        LocalDateTime dropoff = LocalDateTime.now().plusDays(5);

        Reservation reservation = reservationService.makeReservation(
                testCar.getId(), testMember.getId(), "TEST", "TEST",
                pickup, dropoff, List.of(testExtra.getId())
        );

        assertThat(reservation).isNotNull();
        assertThat(reservation.getReservationNumber()).hasSize(8);
        assertThat(reservation.getReservationNumber()).containsOnlyDigits();
        assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.ACTIVE);
        assertThat(reservation.getCreationDate()).isBeforeOrEqualTo(LocalDateTime.now());
        assertThat(reservation.getCar().getId()).isEqualTo(testCar.getId());
        assertThat(reservation.getMember().getId()).isEqualTo(testMember.getId());
        assertThat(reservation.getExtras()).hasSize(1);
        assertThat(reservation.getExtras().get(0).getName()).isEqualTo("GPS Navigation");

        // Check that car status was updated
        assertThat(carRepository.findById(testCar.getId()).get().getStatus()).isEqualTo("RESERVED");
    }

    @Test
    void makeReservation_ThrowsException_WhenCarNotAvailable() {
        // Make car unavailable
        testCar.setStatus("RESERVED");
        carRepository.save(testCar);

        LocalDateTime pickup = LocalDateTime.now().plusDays(1);
        LocalDateTime dropoff = LocalDateTime.now().plusDays(5);

        assertThatThrownBy(() -> reservationService.makeReservation(
                testCar.getId(), testMember.getId(), "TEST", "TEST",
                pickup, dropoff, List.of()
        )).isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Car is not available");
    }

    @Test
    void getReservationByNumber_ReturnsCorrectReservation() {
        LocalDateTime pickup = LocalDateTime.now().plusDays(1);
        LocalDateTime dropoff = LocalDateTime.now().plusDays(5);

        Reservation created = reservationService.makeReservation(
                testCar.getId(), testMember.getId(), "TEST", "TEST",
                pickup, dropoff, List.of()
        );

        Reservation found = reservationService.getReservationByNumber(created.getReservationNumber());

        assertThat(found).isNotNull();
        assertThat(found.getReservationNumber()).isEqualTo(created.getReservationNumber());
        assertThat(found.getCar().getId()).isEqualTo(testCar.getId());
        assertThat(found.getMember().getId()).isEqualTo(testMember.getId());
    }

    @Test
    void addExtraToReservation_AddsExtraSuccessfully() {
        LocalDateTime pickup = LocalDateTime.now().plusDays(1);
        LocalDateTime dropoff = LocalDateTime.now().plusDays(5);

        Reservation reservation = reservationService.makeReservation(
                testCar.getId(), testMember.getId(), "TEST", "TEST",
                pickup, dropoff, List.of()
        );

        boolean result = reservationService.addExtraToReservation(
                reservation.getReservationNumber(), testExtra.getId()
        );

        assertThat(result).isTrue();

        Reservation updated = reservationService.getReservationByNumber(reservation.getReservationNumber());
        assertThat(updated.getExtras()).hasSize(1);
        assertThat(updated.getExtras().get(0).getName()).isEqualTo("GPS Navigation");
    }

    @Test
    void addExtraToReservation_ReturnsFalse_WhenExtraAlreadyAdded() {
        LocalDateTime pickup = LocalDateTime.now().plusDays(1);
        LocalDateTime dropoff = LocalDateTime.now().plusDays(5);

        Reservation reservation = reservationService.makeReservation(
                testCar.getId(), testMember.getId(), "TEST", "TEST",
                pickup, dropoff, List.of(testExtra.getId())
        );

        boolean result = reservationService.addExtraToReservation(
                reservation.getReservationNumber(), testExtra.getId()
        );

        assertThat(result).isFalse(); // Already added
    }

    @Test
    void cancelReservation_CancelsSuccessfully() {
        LocalDateTime pickup = LocalDateTime.now().plusDays(1);
        LocalDateTime dropoff = LocalDateTime.now().plusDays(5);

        Reservation reservation = reservationService.makeReservation(
                testCar.getId(), testMember.getId(), "TEST", "TEST",
                pickup, dropoff, List.of()
        );

        boolean result = reservationService.cancelReservation(reservation.getReservationNumber());

        assertThat(result).isTrue();

        Reservation cancelled = reservationService.getReservationByNumber(reservation.getReservationNumber());
        assertThat(cancelled.getStatus()).isEqualTo(ReservationStatus.CANCELLED);

        // Check that car status was reset to AVAILABLE
        assertThat(carRepository.findById(testCar.getId()).get().getStatus()).isEqualTo("AVAILABLE");
    }

    @Test
    void returnCar_CompletesReservationSuccessfully() {
        LocalDateTime pickup = LocalDateTime.now().minusDays(1); // Started yesterday
        LocalDateTime dropoff = LocalDateTime.now().plusDays(4); // Ends in 4 days

        Reservation reservation = reservationService.makeReservation(
                testCar.getId(), testMember.getId(), "TEST", "TEST",
                pickup, dropoff, List.of()
        );

        boolean result = reservationService.returnCar(reservation.getReservationNumber());

        assertThat(result).isTrue();

        Reservation completed = reservationService.getReservationByNumber(reservation.getReservationNumber());
        assertThat(completed.getStatus()).isEqualTo(ReservationStatus.COMPLETED);
        assertThat(completed.getReturnDate()).isNotNull();

        // Check that car status and location were updated
        Car car = carRepository.findById(testCar.getId()).get();
        assertThat(car.getStatus()).isEqualTo("AVAILABLE");
        assertThat(car.getLocation().getCode()).isEqualTo("TEST");
    }

    @Test
    void calculateTotalPrice_CalculatesCorrectly() {
        LocalDateTime pickup = LocalDateTime.now().plusDays(1);
        LocalDateTime dropoff = LocalDateTime.now().plusDays(6); // 5 days rental

        Reservation reservation = reservationService.makeReservation(
                testCar.getId(), testMember.getId(), "TEST", "TEST",
                pickup, dropoff, List.of(testExtra.getId())
        );

        Double totalPrice = reservationService.calculateTotalPrice(reservation.getReservationNumber());

        // 5 days * 1200.0 (car daily price) + 200.0 (extra) = 6200.0
        assertThat(totalPrice).isEqualTo(6200.0);
    }

    @Test
    void getAllReservations_ReturnsAllReservations() {
        LocalDateTime pickup = LocalDateTime.now().plusDays(1);
        LocalDateTime dropoff = LocalDateTime.now().plusDays(3);

        reservationService.makeReservation(
                testCar.getId(), testMember.getId(), "TEST", "TEST",
                pickup, dropoff, List.of()
        );

        List<Reservation> reservations = reservationService.getAllReservations();

        assertThat(reservations).hasSize(1);
        assertThat(reservations.get(0).getStatus()).isEqualTo(ReservationStatus.ACTIVE);
    }

    @Test
    void getReservationsByStatus_ReturnsFilteredReservations() {
        LocalDateTime pickup = LocalDateTime.now().plusDays(1);
        LocalDateTime dropoff = LocalDateTime.now().plusDays(3);

        Reservation activeReservation = reservationService.makeReservation(
                testCar.getId(), testMember.getId(), "TEST", "TEST",
                pickup, dropoff, List.of()
        );

        reservationService.cancelReservation(activeReservation.getReservationNumber());

        List<Reservation> cancelledReservations = reservationService.getReservationsByStatus(ReservationStatus.CANCELLED);

        assertThat(cancelledReservations).hasSize(1);
        assertThat(cancelledReservations.get(0).getReservationNumber())
                .isEqualTo(activeReservation.getReservationNumber());
    }
}