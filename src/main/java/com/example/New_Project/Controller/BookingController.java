package com.example.New_Project.Controller;

import com.example.New_Project.DTO.BookingRequestDTO;
import com.example.New_Project.DTO.BookingResponseDTO;
import com.example.New_Project.Entity.Booking;
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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import com.example.New_Project.DTO.BookingRequestDTO; // <-- Add this line
import com.example.New_Project.Repository.BookingSeatRepository;
import com.example.New_Project.enums.BookingStatus;

import java.util.List;

@RestController
@RequestMapping("/api/bookings")
@CrossOrigin(origins = "http://localhost:4200")
@RequiredArgsConstructor
@Validated
public class BookingController {

    private final BookingService bookingService;
    private final BookingSeatRepository bookingSeatRepository;

    /**
     * GET /api/bookings/booked-seats/{showId}
     * Returns IDs of all seats already booked (CONFIRMED) for a given show.
     * Used by the seat-layout UI to show which seats are unavailable.
     */
    @GetMapping("/booked-seats/{showId}")
    public ResponseEntity<List<Long>> getBookedSeatIds(@PathVariable Long showId) {
        List<Long> bookedIds = bookingSeatRepository.findBookedSeatIds(
                showId,
                bookingSeatRepository.findAllSeatIdsForShow(showId),
                BookingStatus.CONFIRMED);
        return ResponseEntity.ok(bookedIds);
    }

    /**
     * POST /api/bookings
     * Create a new booking. Returns 201 Created with the booking details.
     */
    @PostMapping("/create")
    public ResponseEntity<BookingResponseDTO> createBookings(
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
            @PageableDefault(size = 20, sort = "bookingDateTime", direction = Sort.Direction.DESC) Pageable pageable) {

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
    public ResponseEntity<List<Booking>> getUserBookings(@PathVariable Long userId) {
        List<Booking> userBookings = bookingService.getBookingsByUserId(userId);
        return ResponseEntity.ok(userBookings);
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
     * Permanently remove a booking and its associated seats. Returns 204 No
     * Content.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBooking(
            @PathVariable @Positive(message = "Booking ID must be a positive number") Long id) {

        bookingService.deleteBooking(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/confirm")
    public ResponseEntity<BookingResponseDTO> confirmBookingPayment(
            @PathVariable Long id) {
        return ResponseEntity.ok(bookingService.confirmBookingPayment(id));
    }
}