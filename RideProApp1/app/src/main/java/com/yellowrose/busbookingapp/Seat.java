package com.yellowrose.busbookingapp;

public class Seat {
    private String number;
    private String status; // available, booked, selected

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
