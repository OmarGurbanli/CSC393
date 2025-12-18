package com.rentacar.dto;

import java.time.LocalDateTime;

public class ReservationResponseDTO {

    private String reservationNumber;
    private LocalDateTime pickupDateTime;
    private LocalDateTime dropoffDateTime;
    private String pickupLocationCode;
    private String pickupLocationName;
    private String dropoffLocationCode;
    private String dropoffLocationName;
    private Double totalAmount;
    private Long memberId;
    private String memberName;

    public String getReservationNumber() {
        return reservationNumber;
    }

    public void setReservationNumber(String reservationNumber) {
        this.reservationNumber = reservationNumber;
    }

    public LocalDateTime getPickupDateTime() {
        return pickupDateTime;
    }

    public void setPickupDateTime(LocalDateTime pickupDateTime) {
        this.pickupDateTime = pickupDateTime;
    }

    public LocalDateTime getDropoffDateTime() {
        return dropoffDateTime;
    }

    public void setDropoffDateTime(LocalDateTime dropoffDateTime) {
        this.dropoffDateTime = dropoffDateTime;
    }

    public String getPickupLocationCode() {
        return pickupLocationCode;
    }

    public void setPickupLocationCode(String pickupLocationCode) {
        this.pickupLocationCode = pickupLocationCode;
    }

    public String getPickupLocationName() {
        return pickupLocationName;
    }

    public void setPickupLocationName(String pickupLocationName) {
        this.pickupLocationName = pickupLocationName;
    }

    public String getDropoffLocationCode() {
        return dropoffLocationCode;
    }

    public void setDropoffLocationCode(String dropoffLocationCode) {
        this.dropoffLocationCode = dropoffLocationCode;
    }

    public String getDropoffLocationName() {
        return dropoffLocationName;
    }

    public void setDropoffLocationName(String dropoffLocationName) {
        this.dropoffLocationName = dropoffLocationName;
    }

    public Double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(Double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public Long getMemberId() {
        return memberId;
    }

    public void setMemberId(Long memberId) {
        this.memberId = memberId;
    }

    public String getMemberName() {
        return memberName;
    }

    public void setMemberName(String memberName) {
        this.memberName = memberName;
    }
}
