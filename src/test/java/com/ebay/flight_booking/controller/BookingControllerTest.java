package com.ebay.flight_booking.controller;

import com.ebay.flight_booking.model.Flight;
import com.ebay.flight_booking.service.BookingService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class BookingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private BookingService bookingService;

    private static final String URL = "/api/bookings";

    // -------------------------------------------------------------------------
    // Test Case 1: Valid Booking → 201 Created
    // -------------------------------------------------------------------------

    @Test
    void validBooking_returns201WithBookingDetails() throws Exception {
        mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"flightNumber":"AA123","passengerName":"Xin"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.bookingId").isNotEmpty())
                .andExpect(jsonPath("$.flightNumber").value("AA123"))
                .andExpect(jsonPath("$.passengerName").value("Xin"))
                .andExpect(jsonPath("$.bookingTime").isNotEmpty());
    }

    // -------------------------------------------------------------------------
    // Test Case 2: Invalid Request → 400 Bad Request
    // -------------------------------------------------------------------------

    @Test
    void missingFlightNumber_returns400() throws Exception {
        mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"passengerName":"Xin"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Invalid request: flightNumber is required"));
    }

    @Test
    void blankPassengerName_returns400() throws Exception {
        mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"flightNumber":"AA123","passengerName":"   "}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Invalid request: passengerName is required"));
    }

    @Test
    void malformedJson_returns400() throws Exception {
        mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("not-json"))
                .andExpect(status().isBadRequest());
    }

    // -------------------------------------------------------------------------
    // Test Case 3: Flight Not Found → 404 Not Found
    // -------------------------------------------------------------------------

    @Test
    void nonExistentFlight_returns404() throws Exception {
        mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"flightNumber":"ZZ999","passengerName":"Xin"}
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Flight not found: ZZ999"));
    }

    // -------------------------------------------------------------------------
    // Test Case 4: Concurrency – No Overbooking
    // -------------------------------------------------------------------------

    @Test
    void concurrentBookings_noOverbooking() throws Exception {
        int capacity = 5;
        int totalRequests = 10;
        String flightNumber = "TEST001";

        bookingService.addFlight(new Flight(flightNumber, capacity));

        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(totalRequests);
        AtomicInteger created = new AtomicInteger();
        AtomicInteger conflict = new AtomicInteger();

        ExecutorService executor = Executors.newFixedThreadPool(totalRequests);
        String body = """
                {"flightNumber":"%s","passengerName":"Passenger"}
                """.formatted(flightNumber);

        for (int i = 0; i < totalRequests; i++) {
            executor.submit(() -> {
                try {
                    startLatch.await(); // all threads start at the same moment
                    MvcResult result = mockMvc.perform(post(URL)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(body))
                            .andReturn();
                    int status = result.getResponse().getStatus();
                    if (status == 201) created.incrementAndGet();
                    else if (status == 409) conflict.incrementAndGet();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        startLatch.countDown(); // release all threads simultaneously
        doneLatch.await();      // wait for all to finish
        executor.shutdown();

        assertThat(created.get())
                .as("successful bookings must equal flight capacity")
                .isEqualTo(capacity);
        assertThat(conflict.get())
                .as("remaining requests must return 409 Conflict")
                .isEqualTo(totalRequests - capacity);
    }
}
