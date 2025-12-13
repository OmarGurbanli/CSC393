package com.rentacar.service;

import com.rentacar.model.Location;
import com.rentacar.repository.LocationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class LocationService {
    private final LocationRepository locationRepository;

    public List<Location> getAllLocations() {
        return locationRepository.findAll();
    }

    public Location getLocationById(Long id) {
        return locationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Location not found with id: " + id));
    }

    public Location getLocationByCode(String code) {
        return locationRepository.findByCode(code)
                .orElseThrow(() -> new RuntimeException("Location not found with code: " + code));
    }

    public Location createLocation(Location location) {
        if (locationRepository.findByCode(location.getCode()).isPresent()) {
            throw new RuntimeException("Location with code " + location.getCode() + " already exists");
        }
        return locationRepository.save(location);
    }

    public Location updateLocation(Long id, Location locationDetails) {
        Location location = getLocationById(id);
        location.setName(locationDetails.getName());

        return locationRepository.save(location);
    }

    public boolean deleteLocation(Long id) {
        Location location = getLocationById(id);

        if (!location.getCars().isEmpty() ||
                !location.getPickupReservations().isEmpty() ||
                !location.getDropoffReservations().isEmpty()) {
            return false;
        }

        locationRepository.delete(location);
        return true;
    }
}