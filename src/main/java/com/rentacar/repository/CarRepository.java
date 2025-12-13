package com.rentacar.repository;

import com.rentacar.model.Car;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface CarRepository extends JpaRepository<Car, Long> {
    Optional<Car> findByBarcode(String barcode);
    List<Car> findByStatus(String status);
    List<Car> findByLocationCode(String locationCode);
    List<Car> findByCategory(String category);

    @Query("SELECT c FROM Car c WHERE c.status = 'AVAILABLE' AND " +
            "c.location.code = :locationCode AND " +
            "(:category IS NULL OR c.category = :category) AND " +
            "(:transmissionType IS NULL OR c.transmissionType = :transmissionType) AND " +
            "(:minSeats IS NULL OR c.numberOfSeats >= :minSeats) AND " +
            "(:maxPrice IS NULL OR c.dailyPrice <= :maxPrice) AND " +
            "(:minPrice IS NULL OR c.dailyPrice >= :minPrice) AND " +
            "NOT EXISTS (SELECT r FROM Reservation r WHERE r.car = c AND " +
            "r.status = 'ACTIVE' AND " +
            "((r.pickupDate <= :dropoffDate AND r.dropoffDate >= :pickupDate)))")
    List<Car> findAvailableCars(@Param("pickupDate") LocalDateTime pickupDate,
                                @Param("dropoffDate") LocalDateTime dropoffDate,
                                @Param("locationCode") String locationCode,
                                @Param("category") String category,
                                @Param("transmissionType") String transmissionType,
                                @Param("minSeats") Integer minSeats,
                                @Param("minPrice") Double minPrice,
                                @Param("maxPrice") Double maxPrice);

    @Query("SELECT c FROM Car c WHERE c.status = 'AVAILABLE' AND c.location.code = :locationCode")
    List<Car> findAvailableCarsAtLocation(@Param("locationCode") String locationCode);

    @Query("SELECT COUNT(r) FROM Reservation r WHERE r.car.id = :carId")
    long countReservationsByCar(@Param("carId") Long carId);

    @Query("SELECT c FROM Car c JOIN Reservation r ON c.id = r.car.id " +
            "WHERE r.status = 'ACTIVE' AND CURRENT_TIMESTAMP BETWEEN r.pickupDate AND r.dropoffDate")
    List<Car> findCurrentlyRentedCars();
    List<Car> findByTransmissionType(String transmissionType);
    List<Car> findByBrand(String brand);
    List<Car> findByNumberOfSeatsGreaterThanEqual(Integer minSeats);
    List<Car> findByDailyPriceBetween(Double minPrice, Double maxPrice);
}