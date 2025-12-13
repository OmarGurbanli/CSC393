package com.rentacar;

import jakarta.persistence.*;
import lombok.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "locations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Location {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String code;

    @Column(nullable = false)
    private String name;

    @OneToMany(mappedBy = "location", fetch = FetchType.LAZY)
    private List<Car> cars = new ArrayList<>();

    @OneToMany(mappedBy = "pickupLocation", fetch = FetchType.LAZY)
    private List<Reservation> pickupReservations = new ArrayList<>();

    @OneToMany(mappedBy = "dropoffLocation", fetch = FetchType.LAZY)
    private List<Reservation> dropoffReservations = new ArrayList<>();

    public Location(String code, String name) {
        this.code = code;
        this.name = name;
    }
}