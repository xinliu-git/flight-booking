package com.ebay.flight_booking.controller;

import com.ebay.flight_booking.model.Booking;
import com.ebay.flight_booking.service.BookingService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping
    public ResponseEntity<?> createBooking(@Valid @RequestBody BookingRequest request) {
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

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationErrors(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> "Invalid request: " + fe.getDefaultMessage())
                .collect(Collectors.joining(", "));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", message));
    }

    public record BookingRequest(
            @NotBlank(message = "flightNumber is required") String flightNumber,
            @NotBlank(message = "passengerName is required") String passengerName
    ) {}

    public record BookingResponse(
            String bookingId,
            String flightNumber,
            String passengerName,
            Instant bookingTime
    ) {}
}
