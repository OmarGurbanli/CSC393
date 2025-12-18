package com.rentacar.repository;

import com.rentacar.model.Extra;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ExtraRepository extends JpaRepository<Extra, Long> {
    Optional<Extra> findByName(String name);
    Optional<Extra> findById(Long id);
}