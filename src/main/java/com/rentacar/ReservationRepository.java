package com.rentacar;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    Optional<Reservation> findByReservationNumber(String reservationNumber);
    List<Reservation> findByStatus(ReservationStatus status);

    // Добавляем недостающие методы
    long countByCarId(Long carId);

    @Query("SELECT r FROM Reservation r WHERE r.status = 'ACTIVE' AND " +
            "CURRENT_TIMESTAMP BETWEEN r.pickupDate AND r.dropoffDate")
    List<Reservation> findCurrentlyActiveReservations();

    @Query("SELECT COUNT(r) > 0 FROM Reservation r WHERE r.car.id = :carId AND " +
            "r.status = 'ACTIVE' AND " +
            "((r.pickupDate <= :dropoffDate AND r.dropoffDate >= :pickupDate))")
    boolean existsActiveReservationForCar(@Param("carId") Long carId,
                                          @Param("pickupDate") LocalDateTime pickupDate,
                                          @Param("dropoffDate") LocalDateTime dropoffDate);

    // Дополнительные методы
    List<Reservation> findByCarId(Long carId);
    List<Reservation> findByMemberId(Long memberId);
    List<Reservation> findByPickupLocationCode(String locationCode);
    List<Reservation> findByDropoffLocationCode(String locationCode);
}