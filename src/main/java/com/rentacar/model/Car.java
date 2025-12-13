package com.rentacar.model;

import jakarta.persistence.*;
import lombok.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "cars")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Car {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String barcode;

    @Column(name = "license_plate", unique = true, nullable = false)
    private String licensePlate;

    @Column(nullable = false)
    private String brand;

    @Column(nullable = false)
    private String model;

    @Column(name = "number_of_seats", nullable = false)
    private Integer numberOfSeats;

    private Double mileage;

    @Column(name = "transmission_type", nullable = false)
    private String transmissionType;

    @Column(name = "daily_price", nullable = false)
    private Double dailyPrice;

    private String category;

    @Column(nullable = false)
    private String status = "AVAILABLE";

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id")
    private Location location;

    @OneToMany(mappedBy = "car", fetch = FetchType.LAZY)
    private List<Reservation> reservations = new ArrayList<>();
}