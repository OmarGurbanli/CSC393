package com.rentacar;

import com.rentacar.model.Extra;
import com.rentacar.repository.ExtraRepository;
import com.rentacar.service.ExtraService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@Import(ExtraService.class)
class ExtraServiceTest {

    @Autowired
    private ExtraService extraService;

    @Autowired
    private ExtraRepository extraRepository;

    @Test
    void getExtraById_ReturnsCorrectExtra() {
        Extra gps = new Extra("GPS Navigation", 200.0);
        Extra saved = extraRepository.save(gps);

        Extra found = extraService.getExtraById(saved.getId());

        assertThat(found).isNotNull();
        assertThat(found.getName()).isEqualTo("GPS Navigation");
        assertThat(found.getPrice()).isEqualTo(200.0);
    }

    @Test
    void getExtraByName_ReturnsCorrectExtra() {
        Extra babySeat = new Extra("Baby Seat", 150.0);
        extraRepository.save(babySeat);

        Extra found = extraService.getExtraByName("Baby Seat");

        assertThat(found).isNotNull();
        assertThat(found.getName()).isEqualTo("Baby Seat");
        assertThat(found.getPrice()).isEqualTo(150.0);
    }

    @Test
    void getAllExtras_ReturnsAllExtras() {
        Extra extra1 = new Extra("GPS", 200.0);
        Extra extra2 = new Extra("Baby Seat", 150.0);
        Extra extra3 = new Extra("Additional Driver", 300.0);

        extraRepository.saveAll(List.of(extra1, extra2, extra3));

        List<Extra> extras = extraService.getAllExtras();

        assertThat(extras).hasSize(3);
        assertThat(extras).extracting(Extra::getName)
                .contains("GPS", "Baby Seat", "Additional Driver");
    }

    @Test
    void createExtra_SavesCorrectly() {
        Extra newExtra = new Extra("WiFi Hotspot", 100.0);

        Extra saved = extraService.createExtra(newExtra);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getName()).isEqualTo("WiFi Hotspot");
        assertThat(saved.getPrice()).isEqualTo(100.0);
        assertThat(extraRepository.findByName("WiFi Hotspot")).isPresent();
    }

    @Test
    void createExtra_ThrowsException_WhenNameExists() {
        Extra existing = new Extra("GPS", 200.0);
        extraRepository.save(existing);

        Extra duplicate = new Extra("GPS", 250.0);

        assertThatThrownBy(() -> extraService.createExtra(duplicate))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("already exists");
    }

    @Test
    void updateExtra_UpdatesCorrectly() {
        Extra extra = new Extra("Old Name", 100.0);
        Extra saved = extraRepository.save(extra);

        Extra updatedDetails = new Extra("New Name", 150.0);
        Extra updated = extraService.updateExtra(saved.getId(), updatedDetails);

        assertThat(updated.getName()).isEqualTo("New Name");
        assertThat(updated.getPrice()).isEqualTo(150.0);
    }

    @Test
    void deleteExtra_ReturnsTrue_WhenNoReservations() {
        Extra extra = new Extra("Test Extra", 100.0);
        Extra saved = extraRepository.save(extra);

        boolean result = extraService.deleteExtra(saved.getId());

        assertThat(result).isTrue();
        assertThat(extraRepository.findById(saved.getId())).isEmpty();
    }
}