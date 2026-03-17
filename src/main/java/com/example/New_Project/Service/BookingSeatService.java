package com.example.New_Project.Service;

import com.example.New_Project.DTO.BookingSeatDTO;
import com.example.New_Project.Entity.Booking;
import com.example.New_Project.Entity.BookingSeat;
import com.example.New_Project.Exception.ResourceNotFoundException;
import com.example.New_Project.Repository.BookingRepository;
import com.example.New_Project.Repository.BookingSeatRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class BookingSeatService {

    private final BookingSeatRepository bookingSeatRepository;
    private final BookingRepository bookingRepository;

    /**
     * Assign a seat to a booking with proper validation and transaction safety.
     * 
     * @param dto Contains bookingId and seatId
     * @return Persisted BookingSeat entity
     * @throws ResourceNotFoundException if booking not found
     * @throws IllegalArgumentException if inputs are invalid
     */
    public BookingSeat assignSeatToBooking(BookingSeatDTO dto) {
        // 1. Validate inputs
        if (dto == null || dto.getBookingId() == null || dto.getSeatId() == null) {
            log.warn("Invalid booking seat assignment: dto={}", dto);
            throw new IllegalArgumentException("BookingId and SeatId cannot be null");
        }

        // 2. Verify booking exists
        Booking booking = bookingRepository.findById(dto.getBookingId())
                .orElseThrow(() -> {
                    log.warn("Booking not found for assignment: bookingId={}", dto.getBookingId());
                    return new ResourceNotFoundException("Booking", dto.getBookingId());
                });

        // 3. Create booking seat with proper entity relationship
        BookingSeat bookingSeat = BookingSeat.builder()
                .booking(booking)
                .seatId(dto.getSeatId())
                .build();

        BookingSeat saved = bookingSeatRepository.save(bookingSeat);
        log.info("✅ Seat assigned to booking: bookingId={}, seatId={}, bookingSeatId={}", 
                booking.getId(), dto.getSeatId(), saved.getId());

        return saved;
    }

    /**
     * Retrieve all seats for a given booking using optimized projection query.
     * 
     * @param bookingId The booking ID
     * @return List of BookingSeat entities
     * @throws IllegalArgumentException if bookingId is null
     */
    @Transactional(readOnly = true)
    public List<BookingSeat> getSeatsByBooking(Long bookingId) {
        if (bookingId == null) {
            log.warn("Null bookingId provided to getSeatsByBooking");
            throw new IllegalArgumentException("BookingId cannot be null");
        }

        List<BookingSeat> seats = bookingSeatRepository.findByBookingId(bookingId);
        log.debug("Retrieved {} seats for booking: bookingId={}", seats.size(), bookingId);
        return seats;
    }

    /**
     * Get only seat IDs for a booking (optimized query to avoid N+1 problem).
     * Use this when you only need IDs, not full BookingSeat entities.
     * 
     * @param bookingId The booking ID
     * @return List of seat IDs
     */
    @Transactional(readOnly = true)
    public List<Long> getSeatIdsByBooking(Long bookingId) {
        if (bookingId == null) {
            log.warn("Null bookingId provided to getSeatIdsByBooking");
            throw new IllegalArgumentException("BookingId cannot be null");
        }

        return bookingSeatRepository.findSeatIdsByBookingId(bookingId);
    }

    /**
     * Update seat assignment with validation and transaction safety.
     * Only seat ID can be updated; booking relationship is immutable.
     * 
     * @param id The BookingSeat ID to update
     * @param dto Contains new seatId
     * @return Updated BookingSeat entity
     * @throws ResourceNotFoundException if BookingSeat not found
     * @throws IllegalArgumentException if inputs are invalid
     */
    public BookingSeat updateBookingSeat(Long id, BookingSeatDTO dto) {
        // 1. Validate inputs
        if (id == null || dto == null || dto.getSeatId() == null) {
            log.warn("Invalid update request: id={}, dto={}", id, dto);
            throw new IllegalArgumentException("BookingSeat ID and SeatId cannot be null");
        }

        // 2. Fetch existing record
        BookingSeat existing = bookingSeatRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("BookingSeat not found for update: id={}", id);
                    return new ResourceNotFoundException("BookingSeat", id);
                });

        // 3. Update only seat ID (booking reference is immutable)
        existing.setSeatId(dto.getSeatId());
        
        BookingSeat updated = bookingSeatRepository.save(existing);
        log.info("✅ BookingSeat updated: id={}, newSeatId={}", id, dto.getSeatId());

        return updated;
    }

    /**
     * Remove seat assignment from a booking.
     * Validates that record exists before deletion.
     * 
     * @param id The BookingSeat ID to remove
     * @throws ResourceNotFoundException if BookingSeat not found
     * @throws IllegalArgumentException if id is null
     */
    public void removeSeatAssignment(Long id) {
        // 1. Validate input
        if (id == null) {
            log.warn("Null ID provided to removeSeatAssignment");
            throw new IllegalArgumentException("BookingSeat ID cannot be null");
        }

        // 2. Verify record exists before deletion (fail-fast)
        BookingSeat existing = bookingSeatRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("BookingSeat not found for deletion: id={}", id);
                    return new ResourceNotFoundException("BookingSeat", id);
                });

        // 3. Delete the record
        bookingSeatRepository.deleteById(id);
        log.info("✅ BookingSeat removed: id={}, bookingId={}, seatId={}", 
                id, existing.getBooking().getId(), existing.getSeatId());
    }

    /**
     * Bulk remove all seats for a booking (cascade handled by Booking entity).
     * This uses optimized batch delete query.
     * 
     * @param bookingId The booking ID
     * @throws IllegalArgumentException if bookingId is null
     */
    public void removeSeatsByBooking(Long bookingId) {
        if (bookingId == null) {
            log.warn("Null bookingId provided to removeSeatsByBooking");
            throw new IllegalArgumentException("BookingId cannot be null");
        }

        bookingSeatRepository.deleteByBookingId(bookingId);
        log.info("✅ All seats removed for booking: bookingId={}", bookingId);
    }
}