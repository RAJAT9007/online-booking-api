package com.example.New_Project.Controller;

import com.example.New_Project.DTO.BookingRequestDTO;
import com.example.New_Project.DTO.BookingResponseDTO;
import com.example.New_Project.Service.BookingService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
@Validated
public class BookingController {

    private final BookingService bookingService;

    /**
     * POST /api/bookings
     * Create a new booking. Returns 201 Created with the booking details.
     */
    @PostMapping
    public ResponseEntity<BookingResponseDTO> createBooking(
            @Valid @RequestBody BookingRequestDTO request) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(bookingService.createBooking(request));
    }

    /**
     * GET /api/bookings?page=0&size=20&sort=bookingTime,desc
     * Paginated list of all bookings (admin use).
     */
    @GetMapping
    public ResponseEntity<Page<BookingResponseDTO>> getAllBookings(
            @PageableDefault(size = 20, sort = "bookingTime", direction = Sort.Direction.DESC)
            Pageable pageable) {

        return ResponseEntity.ok(bookingService.getAllBookings(pageable));
    }

    /**
     * GET /api/bookings/{id}
     * Fetch a single booking by ID. Returns 404 if not found.
     */
    @GetMapping("/{id}")
    public ResponseEntity<BookingResponseDTO> getBookingById(
            @PathVariable @Positive(message = "Booking ID must be a positive number") Long id) {

        return ResponseEntity.ok(bookingService.getBookingById(id));
    }

    /**
     * GET /api/bookings/users/{userId}?page=0&size=10
     * Paginated booking history for a specific user.
     */
    @GetMapping("/users/{userId}")
    public ResponseEntity<Page<BookingResponseDTO>> getUserBookingHistory(
            @PathVariable @Positive(message = "User ID must be a positive number") Long userId,
            @PageableDefault(size = 10, sort = "bookingTime", direction = Sort.Direction.DESC)
            Pageable pageable) {

        return ResponseEntity.ok(bookingService.getUserBookingHistory(userId, pageable));
    }

    /**
     * PATCH /api/bookings/{id}/cancel
     * Cancel an active booking. Returns 409 if already cancelled.
     */
    @PatchMapping("/{id}/cancel")
    public ResponseEntity<BookingResponseDTO> cancelBooking(
            @PathVariable @Positive(message = "Booking ID must be a positive number") Long id) {

        return ResponseEntity.ok(bookingService.cancelBooking(id));
    }

    /**
     * DELETE /api/bookings/{id}
     * Permanently remove a booking and its associated seats. Returns 204 No Content.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBooking(
            @PathVariable @Positive(message = "Booking ID must be a positive number") Long id) {

        bookingService.deleteBooking(id);
        return ResponseEntity.noContent().build();
    }
}