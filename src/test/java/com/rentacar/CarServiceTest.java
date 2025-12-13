package com.rentacar;

import com.rentacar.model.Car;
import com.rentacar.model.Location;
import com.rentacar.repository.CarRepository;
import com.rentacar.repository.LocationRepository;
import com.rentacar.service.CarService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@Import(CarService.class)
class CarServiceTest {

    @Autowired
    private CarService carService;

    @Autowired
    private CarRepository carRepository;

    @Autowired
    private LocationRepository locationRepository;

    private Location testLocation;

    @BeforeEach
    void setUp() {
        testLocation = new Location("IST", "Istanbul Airport");
        locationRepository.save(testLocation);
    }

    @Test
    void saveCar_PersistsCorrectly() {
        Car car = new Car();
        car.setBarcode("34TEST99");
        car.setLicensePlate("34TEST99");
        car.setBrand("Renault");
        car.setModel("Clio");
        car.setNumberOfSeats(5);
        car.setTransmissionType("Manual");
        car.setDailyPrice(800.0);
        car.setCategory("Compact");
        car.setLocation(testLocation);

        Car saved = carService.saveCar(car);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getBarcode()).isEqualTo("34TEST99");
        assertThat(carRepository.findByBarcode("34TEST99")).isPresent();
    }

    @Test
    void getCarByBarcode_ReturnsCorrectCar() {
        Car car = new Car();
        car.setBarcode("TEST123");
        car.setLicensePlate("34ABC123");
        car.setBrand("Fiat");
        car.setModel("Egea");
        car.setNumberOfSeats(5);
        car.setTransmissionType("Manual");
        car.setDailyPrice(950.0);
        car.setCategory("Economy");
        car.setLocation(testLocation);
        carRepository.save(car);

        Car found = carService.getCarByBarcode("TEST123");

        assertThat(found).isNotNull();
        assertThat(found.getModel()).isEqualTo("Egea");
        assertThat(found.getBrand()).isEqualTo("Fiat");
    }

    @Test
    void getCarByBarcode_ThrowsException_WhenNotFound() {
        assertThatThrownBy(() -> carService.getCarByBarcode("NONEXISTENT"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Car not found");
    }

    @Test
    void getAllCars_ReturnsAllCars() {
        Car car1 = new Car();
        car1.setBarcode("CAR001");
        car1.setLicensePlate("34CAR001");
        car1.setBrand("Toyota");
        car1.setModel("Corolla");
        car1.setNumberOfSeats(5);
        car1.setTransmissionType("Automatic");
        car1.setDailyPrice(1000.0);
        car1.setLocation(testLocation);

        Car car2 = new Car();
        car2.setBarcode("CAR002");
        car2.setLicensePlate("34CAR002");
        car2.setBrand("Honda");
        car2.setModel("Civic");
        car2.setNumberOfSeats(5);
        car2.setTransmissionType("Manual");
        car2.setDailyPrice(900.0);
        car2.setLocation(testLocation);

        carRepository.saveAll(List.of(car1, car2));

        List<Car> cars = carService.getAllCars();

        assertThat(cars).hasSize(2);
        assertThat(cars).extracting(Car::getBarcode).contains("CAR001", "CAR002");
    }

    @Test
    void getAvailableCars_ReturnsOnlyAvailableCars() {
        Car availableCar = new Car();
        availableCar.setBarcode("AVAIL001");
        availableCar.setLicensePlate("34AVAIL");
        availableCar.setBrand("Toyota");
        availableCar.setModel("Corolla");
        availableCar.setNumberOfSeats(5);
        availableCar.setTransmissionType("Automatic");
        availableCar.setDailyPrice(1000.0);
        availableCar.setStatus("AVAILABLE");
        availableCar.setLocation(testLocation);

        Car reservedCar = new Car();
        reservedCar.setBarcode("RESV001");
        reservedCar.setLicensePlate("34RESV");
        reservedCar.setBrand("Honda");
        reservedCar.setModel("Civic");
        reservedCar.setNumberOfSeats(5);
        reservedCar.setTransmissionType("Manual");
        reservedCar.setDailyPrice(900.0);
        reservedCar.setStatus("RESERVED");
        reservedCar.setLocation(testLocation);

        carRepository.saveAll(List.of(availableCar, reservedCar));

        List<Car> availableCars = carService.getAvailableCars();

        assertThat(availableCars).hasSize(1);
        assertThat(availableCars.get(0).getBarcode()).isEqualTo("AVAIL001");
        assertThat(availableCars.get(0).getStatus()).isEqualTo("AVAILABLE");
    }

    @Test
    void deleteCar_ReturnsTrue_WhenNoReservations() {
        Car car = new Car();
        car.setBarcode("DEL001");
        car.setLicensePlate("34DEL001");
        car.setBrand("Toyota");
        car.setModel("Corolla");
        car.setNumberOfSeats(5);
        car.setTransmissionType("Automatic");
        car.setDailyPrice(1000.0);
        car.setLocation(testLocation);
        carRepository.save(car);

        boolean result = carService.deleteCar(car.getId());

        assertThat(result).isTrue();
        assertThat(carRepository.findByBarcode("DEL001")).isEmpty();
    }

    @Test
    void updateCarStatus_UpdatesStatusCorrectly() {
        Car car = new Car();
        car.setBarcode("UPDATE001");
        car.setLicensePlate("34UPDATE");
        car.setBrand("Toyota");
        car.setModel("Corolla");
        car.setNumberOfSeats(5);
        car.setTransmissionType("Automatic");
        car.setDailyPrice(1000.0);
        car.setStatus("AVAILABLE");
        car.setLocation(testLocation);
        carRepository.save(car);

        Car updated = carService.updateCarStatus(car.getId(), "RESERVED");

        assertThat(updated.getStatus()).isEqualTo("RESERVED");
        assertThat(carRepository.findById(car.getId()).get().getStatus()).isEqualTo("RESERVED");
    }
}