package com.rentacar.dto;

import java.time.LocalDateTime;

public class CarSearchRequestDTO {

    private String category;
    private String transmissionType;
    private Double minDailyPrice;
    private Double maxDailyPrice;
    private Integer numberOfSeats;

    private LocalDateTime pickupDate;
    private LocalDateTime dropoffDate;

    private String pickupLocationCode;

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getTransmissionType() { return transmissionType; }
    public void setTransmissionType(String transmissionType) { this.transmissionType = transmissionType; }

    public Double getMinDailyPrice() { return minDailyPrice; }
    public void setMinDailyPrice(Double minDailyPrice) { this.minDailyPrice = minDailyPrice; }

    public Double getMaxDailyPrice() { return maxDailyPrice; }
    public void setMaxDailyPrice(Double maxDailyPrice) { this.maxDailyPrice = maxDailyPrice; }

    public Integer getNumberOfSeats() { return numberOfSeats; }
    public void setNumberOfSeats(Integer numberOfSeats) { this.numberOfSeats = numberOfSeats; }

    public LocalDateTime getPickupDate() { return pickupDate; }
    public void setPickupDate(LocalDateTime pickupDate) { this.pickupDate = pickupDate; }

    public LocalDateTime getDropoffDate() { return dropoffDate; }
    public void setDropoffDate(LocalDateTime dropoffDate) { this.dropoffDate = dropoffDate; }

    public String getPickupLocationCode() { return pickupLocationCode; }
    public void setPickupLocationCode(String pickupLocationCode) { this.pickupLocationCode = pickupLocationCode; }
}
