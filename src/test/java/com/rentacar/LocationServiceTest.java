package com.rentacar;

import com.rentacar.model.Location;
import com.rentacar.repository.LocationRepository;
import com.rentacar.service.LocationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@Import(LocationService.class)
class LocationServiceTest {

    @Autowired
    private LocationService locationService;

    @Autowired
    private LocationRepository locationRepository;

    @Test
    void getLocationByCode_ReturnsCorrectLocation() {
        Location location = new Location("ANK", "Ankara Esenboğa Airport");
        locationRepository.save(location);

        Location found = locationService.getLocationByCode("ANK");

        assertThat(found).isNotNull();
        assertThat(found.getName()).isEqualTo("Ankara Esenboğa Airport");
        assertThat(found.getCode()).isEqualTo("ANK");
    }

    @Test
    void getLocationByCode_ThrowsException_WhenNotFound() {
        assertThatThrownBy(() -> locationService.getLocationByCode("INVALID"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Location not found");
    }

    @Test
    void getAllLocations_ReturnsAllLocations() {
        Location loc1 = new Location("IST", "Istanbul Airport");
        Location loc2 = new Location("SAW", "Sabiha Gokcen Airport");
        Location loc3 = new Location("KAD", "Kadikoy Office");

        locationRepository.saveAll(List.of(loc1, loc2, loc3));

        List<Location> locations = locationService.getAllLocations();

        assertThat(locations).hasSize(3);
        assertThat(locations).extracting(Location::getCode)
                .contains("IST", "SAW", "KAD");
    }

    @Test
    void createLocation_SavesCorrectly() {
        Location newLocation = new Location("NEW", "New Location");

        Location saved = locationService.createLocation(newLocation);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getCode()).isEqualTo("NEW");
        assertThat(saved.getName()).isEqualTo("New Location");
        assertThat(locationRepository.findByCode("NEW")).isPresent();
    }

    @Test
    void createLocation_ThrowsException_WhenCodeExists() {
        Location existing = new Location("EXIST", "Existing Location");
        locationRepository.save(existing);

        Location duplicate = new Location("EXIST", "Duplicate Location");

        assertThatThrownBy(() -> locationService.createLocation(duplicate))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("already exists");
    }

    @Test
    void updateLocation_UpdatesNameCorrectly() {
        Location location = new Location("UPD", "Old Name");
        Location saved = locationRepository.save(location);

        Location updatedDetails = new Location("UPD", "New Name");
        Location updated = locationService.updateLocation(saved.getId(), updatedDetails);

        assertThat(updated.getName()).isEqualTo("New Name");
        assertThat(updated.getCode()).isEqualTo("UPD");
    }

    @Test
    void deleteLocation_ReturnsFalse_WhenCarsExist() {
        Location location = new Location("DEL", "Location with Cars");
        Location saved = locationRepository.save(location);
        boolean result = locationService.deleteLocation(saved.getId());
        assertThat(result).isTrue();
        assertThat(locationRepository.findByCode("DEL")).isEmpty();
    }
}