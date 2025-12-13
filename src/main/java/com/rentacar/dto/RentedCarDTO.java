package com.rentacar.dto;

import java.time.LocalDateTime;

public class RentedCarDTO {

    private String brand;
    private String model;
    private String carType;
    private String transmissionType;
    private String barcode;
    private String reservationNumber;
    private String memberName;
    private LocalDateTime dropoffDateTime;
    private String dropoffLocation;
    private long reservationDayCount;

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getCarType() {
        return carType;
    }

    public void setCarType(String carType) {
        this.carType = carType;
    }

    public String getTransmissionType() {
        return transmissionType;
    }

    public void setTransmissionType(String transmissionType) {
        this.transmissionType = transmissionType;
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public String getReservationNumber() {
        return reservationNumber;
    }

    public void setReservationNumber(String reservationNumber) {
        this.reservationNumber = reservationNumber;
    }

    public String getMemberName() {
        return memberName;
    }

    public void setMemberName(String memberName) {
        this.memberName = memberName;
    }

    public LocalDateTime getDropoffDateTime() {
        return dropoffDateTime;
    }

    public void setDropoffDateTime(LocalDateTime dropoffDateTime) {
        this.dropoffDateTime = dropoffDateTime;
    }

    public String getDropoffLocation() {
        return dropoffLocation;
    }

    public void setDropoffLocation(String dropoffLocation) {
        this.dropoffLocation = dropoffLocation;
    }

    public long getReservationDayCount() {
        return reservationDayCount;
    }

    public void setReservationDayCount(long reservationDayCount) {
        this.reservationDayCount = reservationDayCount;
    }
}
