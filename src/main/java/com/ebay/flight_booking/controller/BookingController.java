package com.ebay.flight_booking.controller;

import com.ebay.flight_booking.model.Booking;
import com.ebay.flight_booking.service.BookingService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Map;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping
    public ResponseEntity<?> createBooking(@RequestBody BookingRequest request) {
        try {
            Booking booking = bookingService.book(request.flightNumber(), request.passengerName());
            return ResponseEntity.status(HttpStatus.CREATED).body(new BookingResponse(
                    booking.getBookingId(),
                    booking.getFlightNumber(),
                    booking.getPassengerName(),
                    booking.getBookingTime()
            ));
        } catch (BookingService.FlightNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        } catch (BookingService.FlightFullException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    public record BookingRequest(String flightNumber, String passengerName) {}

    public record BookingResponse(
            String bookingId,
            String flightNumber,
            String passengerName,
            Instant bookingTime
    ) {}
}
