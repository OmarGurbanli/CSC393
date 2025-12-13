package com.rentacar.dto;

import java.time.LocalDateTime;
import java.util.List;

public class ReservationRequestDTO {

    private String carBarcode;
    private LocalDateTime pickupDateTime;
    private LocalDateTime dropoffDateTime;
    private Long memberId;
    private String pickupLocationCode;
    private String dropoffLocationCode;
    private List<String> extraCodes;

    public String getCarBarcode() {
        return carBarcode;
    }

    public void setCarBarcode(String carBarcode) {
        this.carBarcode = carBarcode;
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

    public Long getMemberId() {
        return memberId;
    }

    public void setMemberId(Long memberId) {
        this.memberId = memberId;
    }

    public String getPickupLocationCode() {
        return pickupLocationCode;
    }

    public void setPickupLocationCode(String pickupLocationCode) {
        this.pickupLocationCode = pickupLocationCode;
    }

    public String getDropoffLocationCode() {
        return dropoffLocationCode;
    }

    public void setDropoffLocationCode(String dropoffLocationCode) {
        this.dropoffLocationCode = dropoffLocationCode;
    }

    public List<String> getExtraCodes() {
        return extraCodes;
    }

    public void setExtraCodes(List<String> extraCodes) {
        this.extraCodes = extraCodes;
    }
}
