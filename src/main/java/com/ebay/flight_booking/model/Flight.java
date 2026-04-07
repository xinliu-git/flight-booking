package com.ebay.flight_booking.model;

import java.util.HashMap;
import java.util.Map;

public class Flight {

    private final String flightNumber;
    private final int capacity;
    private int bookedSeats;
    private final Map<String, Booking> bookings;

    public Flight(String flightNumber, int capacity) {
        this.flightNumber = flightNumber;
        this.capacity = capacity;
        this.bookedSeats = 0;
        this.bookings = new HashMap<>();
    }

    public String getFlightNumber() {
        return flightNumber;
    }

    public int getCapacity() {
        return capacity;
    }

    public int getBookedSeats() {
        return bookedSeats;
    }

    public Map<String, Booking> getBookings() {
        return bookings;
    }

    /**
     * Attempts to add a booking atomically. Returns the new Booking on success,
     * or null if the flight is already full. Callers must synchronize on this
     * Flight instance before calling.
     */
    public Booking addBooking(Booking booking) {
        if (bookedSeats >= capacity) {
            return null;
        }
        bookedSeats++;
        bookings.put(booking.getBookingId(), booking);
        return booking;
    }
}
