# Flight Booking API

A Spring Boot REST API for booking airline flights with thread-safe, in-memory storage.

## Requirements

- Java 17+
- Maven (or use the included `./mvnw` wrapper)

## Run the Service

```bash
./mvnw spring-boot:run
```

The server starts on `http://localhost:8080`.

## Run Tests

```bash
./mvnw test
```

## API

### `POST /api/bookings`

**Request body**

| Field | Type | Required |
|-------|------|----------|
| `flightNumber` | string | yes |
| `passengerName` | string | yes |

---

**Success — 201 Created**

```bash
curl -X POST http://localhost:8080/api/bookings \
  -H "Content-Type: application/json" \
  -d '{"flightNumber":"AA123","passengerName":"Xin"}'
```

```json
{
  "bookingId": "e3d1b6a2-7f4c-4e8a-9c2d-1a3b5f8e0d7c",
  "flightNumber": "AA123",
  "passengerName": "Xin",
  "bookingTime": "2026-04-07T20:00:00Z"
}
```

---

**Flight not found — 404 Not Found**

```bash
curl -X POST http://localhost:8080/api/bookings \
  -H "Content-Type: application/json" \
  -d '{"flightNumber":"ZZ999","passengerName":"Xin"}'
```

```json
{ "error": "Flight not found: ZZ999" }
```

---

**Flight full — 409 Conflict**

```json
{ "error": "Flight AA123 is fully booked" }
```

---

**Invalid request — 400 Bad Request**

```bash
curl -X POST http://localhost:8080/api/bookings \
  -H "Content-Type: application/json" \
  -d '{"flightNumber":"AA123"}'
```

```json
{ "error": "Invalid request: passengerName is required" }
```

## Pre-loaded Flights

| Flight | Capacity |
|--------|----------|
| AA123  | 150      |
| UA456  | 180      |


## Quick Manually Fix
Claude add test code (addFlight) in production service, I need to eliminate test-only method from production service

- Removed addFlight() from BookingService as it was only used for tests
- Prevented leakage of test concerns into production code
- Recommended using @TestConfiguration or test-specific setup for initializing data

Improves code integrity and enforces proper separation between production and test logic.


## Improvements with more time
1.	Global exception handling
Move @ExceptionHandler from controller to a centralized @ControllerAdvice to avoid duplication and handle 400/404/409/500 consistently.

2.	Standardized error response
Replace ad-hoc Map.of(...) with a unified error schema (timestamp, status, path), or use Spring’s built-in ProblemDetail.

3.	Scalability of thread safety
synchronized only works in a single JVM. For multi-instance deployment, use database optimistic locking (@Version) or distributed locks (e.g., Redis).