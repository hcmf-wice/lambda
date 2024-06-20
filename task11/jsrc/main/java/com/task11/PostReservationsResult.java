package com.task11;

public class PostReservationsResult {
    private String reservationId;

    public PostReservationsResult(String reservationId) {
        this.reservationId = reservationId;
    }

    public String getReservationId() {
        return reservationId;
    }
}
