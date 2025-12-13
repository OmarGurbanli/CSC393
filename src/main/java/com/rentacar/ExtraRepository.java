package com.rentacar;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ExtraRepository extends JpaRepository<Extra, Long> {
    Optional<Extra> findByName(String name);
}