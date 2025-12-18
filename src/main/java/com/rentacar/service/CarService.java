package com.rentacar.service;

import com.rentacar.dto.CarResponseDTO;
import com.rentacar.dto.CarSearchRequestDTO;
import com.rentacar.dto.RentedCarDTO;
import com.rentacar.model.Car;
import com.rentacar.model.Reservation;
import com.rentacar.model.ReservationStatus;
import com.rentacar.repository.CarRepository;
import com.rentacar.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class CarService {
    private final CarRepository carRepository;
    private final ReservationRepository reservationRepository;
    public List<Car> getAllCars() {
        return carRepository.findAll();
    }

    public Car getCarById(Long id) {
        return carRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Car not found with id: " + id));
    }

    public Car getCarByBarcode(String barcode) {
        return carRepository.findByBarcode(barcode)
                .orElseThrow(() -> new RuntimeException("Car not found with barcode: " + barcode));
    }

    public Car saveCar(Car car) {
        return carRepository.save(car);
    }

    public Car updateCar(Long id, Car carDetails) {
        Car car = getCarById(id);
        car.setBarcode(carDetails.getBarcode());
        car.setLicensePlate(carDetails.getLicensePlate());
        car.setBrand(carDetails.getBrand());
        car.setModel(carDetails.getModel());
        car.setNumberOfSeats(carDetails.getNumberOfSeats());
        car.setMileage(carDetails.getMileage());
        car.setTransmissionType(carDetails.getTransmissionType());
        car.setDailyPrice(carDetails.getDailyPrice());
        car.setCategory(carDetails.getCategory());
        car.setStatus(carDetails.getStatus());
        car.setLocation(carDetails.getLocation());
        return carRepository.save(car);
    }

    public List<Car> searchAvailableCars(LocalDateTime pickupDate, LocalDateTime dropoffDate,
                                         String category, String transmissionType,
                                         Double minPrice, Double maxPrice,
                                         Integer seats, String pickupLocationCode) {
        return carRepository.findAvailableCars(pickupDate, dropoffDate, pickupLocationCode,
                category, transmissionType, seats, minPrice, maxPrice);
    }

    public boolean deleteCar(Long id) {
        Car car = getCarById(id);
        long reservationCount = reservationRepository.countByCarId(id);

        if (reservationCount > 0) {
            return false;
        }

        carRepository.delete(car);
        return true;
    }

    public List<Car> getAvailableCars() {
        return carRepository.findByStatus("AVAILABLE");
    }

    public List<Car> getCarsByLocation(String locationCode) {
        return carRepository.findByLocationCode(locationCode);
    }

    public List<Car> getCarsByCategory(String category) {
        return carRepository.findByCategory(category);
    }

    public Car updateCarStatus(Long id, String status) {
        Car car = getCarById(id);
        car.setStatus(status);
        return carRepository.save(car);
    }

    public List<Car> getCurrentlyRentedCars() {
        return carRepository.findCurrentlyRentedCars();
    }

    public List<Car> getCarsByTransmissionType(String transmissionType) {
        return carRepository.findByTransmissionType(transmissionType);
    }

    public List<Car> getCarsByBrand(String brand) {
        return carRepository.findByBrand(brand);
    }

    public List<Car> getCarsBySeats(Integer minSeats) {
        return carRepository.findByNumberOfSeatsGreaterThanEqual(minSeats);
    }

    public List<Car> getCarsByPriceRange(Double minPrice, Double maxPrice) {
        return carRepository.findByDailyPriceBetween(minPrice, maxPrice);
    }

    public boolean isCarAvailableForDates(Long carId, LocalDateTime pickup, LocalDateTime dropoff) {
        Car car = getCarById(carId);
        if (!"AVAILABLE".equals(car.getStatus())) {
            return false;
        }

        boolean hasConflict = reservationRepository.existsActiveReservationForCar(carId, pickup, dropoff);
        return !hasConflict;
    }

    public List<CarResponseDTO> searchAvailableCars(CarSearchRequestDTO request) {
        List<Car> cars = carRepository.findAvailableCars(
                request.getPickupDate(),
                request.getDropoffDate(),
                request.getPickupLocationCode(),
                request.getCategory(),
                request.getTransmissionType(),
                request.getNumberOfSeats(),
                request.getMinDailyPrice(),
                request.getMaxDailyPrice()
        );

        return cars.stream().map(car -> {
            CarResponseDTO dto = new CarResponseDTO();
            dto.setBarcode(car.getBarcode());
            dto.setBrand(car.getBrand());
            dto.setModel(car.getModel());
            dto.setCategory(car.getCategory());
            dto.setTransmissionType(car.getTransmissionType());
            dto.setDailyPrice(car.getDailyPrice());
            dto.setNumberOfSeats(car.getNumberOfSeats());
            if (car.getLocation() != null) {
                dto.setLocationName(car.getLocation().getName());
            }
            return dto;
        }).collect(Collectors.toList());
    }

    public List<RentedCarDTO> getAllRentedCars() {
        List<Car> rentedCars = carRepository.findCurrentlyRentedCars();

        return rentedCars.stream().map(car -> {
            // Find the active reservation for this car
            Reservation activeReservation = reservationRepository.findByCarId(car.getId()).stream()
                    .filter(r -> r.getStatus() == ReservationStatus.ACTIVE &&
                            LocalDateTime.now().isAfter(r.getPickupDate()) &&
                            LocalDateTime.now().isBefore(r.getDropoffDate()))
                    .findFirst()
                    .orElse(null);

            if (activeReservation == null) {
                return null;
            }

            RentedCarDTO dto = new RentedCarDTO();
            dto.setBrand(car.getBrand());
            dto.setModel(car.getModel());
            dto.setCarType(car.getCategory());
            dto.setTransmissionType(car.getTransmissionType());
            dto.setBarcode(car.getBarcode());
            dto.setReservationNumber(activeReservation.getReservationNumber());
            dto.setMemberName(activeReservation.getMember().getName());
            dto.setDropoffDateTime(activeReservation.getDropoffDate());
            if (activeReservation.getDropoffLocation() != null) {
                dto.setDropoffLocation(activeReservation.getDropoffLocation().getName());
            }
            dto.setReservationDayCount(activeReservation.getDayCount());
            return dto;
        }).filter(dto -> dto != null).collect(Collectors.toList());
    }

    public boolean deleteCar(String barcode) {
        Car car = carRepository.findByBarcode(barcode)
                .orElse(null);

        if (car == null) {
            throw new RuntimeException("Car not found with barcode: " + barcode);
        }

        long reservationCount = reservationRepository.countByCarId(car.getId());

        if (reservationCount > 0) {
            return false;
        }

        carRepository.delete(car);
        return true;
    }
}