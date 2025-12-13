package com.rentacar;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final LocationRepository locationRepo;
    private final CarRepository carRepo;
    private final MemberRepository memberRepo;
    private final ExtraRepository extraRepo;
    private final ReservationRepository reservationRepo; // Добавляем

    @Override
    public void run(String... args) {
        if (locationRepo.count() == 0) {
            Location ist = new Location("IST", "Istanbul Airport");
            Location saw = new Location("SAW", "Sabiha Gökçen Airport");
            Location kad = new Location("KAD", "Kadıköy Office");
            locationRepo.saveAll(List.of(ist, saw, kad));

            Car car1 = new Car();
            car1.setBarcode("34ABC123");
            car1.setLicensePlate("34XYZ999");
            car1.setBrand("Toyota");
            car1.setModel("Corolla");
            car1.setNumberOfSeats(5);
            car1.setMileage(15000.0);
            car1.setTransmissionType("Automatic");
            car1.setDailyPrice(1200.0);
            car1.setCategory("Mid-size");
            car1.setLocation(ist);
            carRepo.save(car1);

            Member member = new Member();
            member.setName("Alihan Kerimov");
            member.setAddress("Istanbul, Turkey");
            member.setEmail("ali@example.com");
            member.setPhone("+905551234567");
            member.setDrivingLicenseNumber("A1234567");
            memberRepo.save(member);

            Extra gps = new Extra("GPS Navigation", 200.0);
            Extra babySeat = new Extra("Baby Seat", 150.0);
            Extra additionalDriver = new Extra("Additional Driver", 300.0);
            extraRepo.saveAll(List.of(gps, babySeat, additionalDriver));

            Reservation reservation = new Reservation();
            reservation.setReservationNumber("TEST0001");
            reservation.setCreationDate(LocalDateTime.now());
            reservation.setPickupDate(LocalDateTime.now().plusDays(1));
            reservation.setDropoffDate(LocalDateTime.now().plusDays(3));
            reservation.setPickupLocation(ist);
            reservation.setDropoffLocation(ist);
            reservation.setStatus(ReservationStatus.ACTIVE);
            reservation.setMember(member);
            reservation.setCar(car1);
            reservation.setExtras(List.of(gps));

            reservationRepo.save(reservation);

            System.out.println("Initial data inserted successfully!");
        }
    }
}