package com.ebay.flight_booking.model;

import java.time.Instant;

public class Booking {

    private final String bookingId;
    private final String flightNumber;
    private final String passengerName;
    private final Instant bookingTime;

    public Booking(String bookingId, String flightNumber, String passengerName, Instant bookingTime) {
        this.bookingId = bookingId;
        this.flightNumber = flightNumber;
        this.passengerName = passengerName;
        this.bookingTime = bookingTime;
    }

    public String getBookingId() {
        return bookingId;
    }

    public String getFlightNumber() {
        return flightNumber;
    }

    public String getPassengerName() {
        return passengerName;
    }

    public Instant getBookingTime() {
        return bookingTime;
    }
}
