package com.ebay.flight_booking.config;

import com.ebay.flight_booking.model.Flight;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
public class FlightConfig {

    @Bean
    public Map<String, Flight> initialFlights() {
        return Map.of(
                "AA123", new Flight("AA123", 150),
                "UA456", new Flight("UA456", 180)
        );
    }
}
