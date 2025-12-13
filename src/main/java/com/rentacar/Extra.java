package com.rentacar;

import jakarta.persistence.*;
import lombok.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "extras")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Extra {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Double price;

    @ManyToMany(mappedBy = "extras")
    private Set<Reservation> reservations = new HashSet<>();

    public Extra(String name, Double price) {
        this.name = name;
        this.price = price;
    }
}