package com.yellowrose.busbookingapp;

import java.util.List;

public class Booking {
    private String Id;
    private String busId;
    private String userId;
    private List<String> selectedSeats;
    private double totalPrice;
    private String bookingDate;
    private String status;

    // Constructor (optional)
    public Booking() {
    }

    public String getId() {
        return Id;
    }

    public void setId(String id) {
        Id = id;
    }

    // Getters and Setters
    public String getBusId() {
        return busId;
    }

    public void setBusId(String busId) {
        this.busId = busId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public List<String> getSelectedSeats() {
        return selectedSeats;
    }

    public void setSelectedSeats(List<String> selectedSeats) {
        this.selectedSeats = selectedSeats;
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(double totalPrice) {
        this.totalPrice = totalPrice;
    }

    public String getBookingDate() {
        return bookingDate;
    }

    public void setBookingDate(String bookingDate) {
        this.bookingDate = bookingDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}

