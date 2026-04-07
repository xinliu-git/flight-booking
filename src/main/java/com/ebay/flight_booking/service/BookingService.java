package com.ebay.flight_booking.service;

import com.ebay.flight_booking.model.Booking;
import com.ebay.flight_booking.model.Flight;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class BookingService {

    private final ConcurrentHashMap<String, Flight> flights = new ConcurrentHashMap<>();

    public BookingService() {
        flights.put("AA123", new Flight("AA123", 150));
        flights.put("UA456", new Flight("UA456", 180));
    }

    /**
     * @return the created Booking
     * @throws FlightNotFoundException if no flight with the given number exists
     * @throws FlightFullException     if the flight has no remaining seats
     */
    public Booking book(String flightNumber, String passengerName) {
        Flight flight = flights.get(flightNumber);
        if (flight == null) {
            throw new FlightNotFoundException(flightNumber);
        }

        Booking booking = new Booking(
                UUID.randomUUID().toString(),
                flightNumber,
                passengerName,
                Instant.now()
        );

        synchronized (flight) {
            Booking result = flight.addBooking(booking);
            if (result == null) {
                throw new FlightFullException(flightNumber);
            }
            return result;
        }
    }

    public static class FlightNotFoundException extends RuntimeException {
        public FlightNotFoundException(String flightNumber) {
            super("Flight not found: " + flightNumber);
        }
    }

    public static class FlightFullException extends RuntimeException {
        public FlightFullException(String flightNumber) {
            super("Flight " + flightNumber + " is fully booked");
        }
    }
}
