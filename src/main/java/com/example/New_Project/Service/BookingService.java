package com.example.New_Project.Service;

import com.example.New_Project.DTO.BookingRequestDTO;
import com.example.New_Project.DTO.BookingResponseDTO;
import com.example.New_Project.Entity.Booking;
import com.example.New_Project.Entity.BookingSeat;
import com.example.New_Project.Entity.Show;
import com.example.New_Project.Exception.BookingAlreadyCancelledException;
import com.example.New_Project.Exception.BookingNotFoundException;
import com.example.New_Project.Exception.ResourceNotFoundException;
import com.example.New_Project.Exception.SeatsAlreadyBookedException;
import com.example.New_Project.Repository.BookingRepository;
import com.example.New_Project.Repository.BookingSeatRepository;
import com.example.New_Project.Repository.SeatRepository;
import com.example.New_Project.Repository.ShowRepository;
import com.example.New_Project.enums.BookingStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository     bookingRepository;
    private final BookingSeatRepository bookingSeatRepository;
    private final ShowRepository        showRepository;
    private final SeatRepository        seatRepository;
    private final Clock                 clock;

    // -------------------------------------------------------------------------
    // COMMANDS (Write Operations)
    // -------------------------------------------------------------------------

    /**
     * Creates a confirmed booking securely.
     * Validates show, seats, availability, and computes the price server-side 
     * to prevent client-side payment tampering.
     */
    @Transactional(isolation = Isolation.SERIALIZABLE, rollbackFor = Exception.class)
    public BookingResponseDTO createBooking(BookingRequestDTO request) {
        Objects.requireNonNull(request, "BookingRequestDTO must not be null");
        
        if (request.getSeatIds() == null || request.getSeatIds().isEmpty()) {
            throw new IllegalArgumentException("At least one seat must be selected");
        }

        log.info("Processing booking creation [IdempotencyKey={}]: userId={}, showId={}, requestedSeats={}",
                request.getIdempotencyKey(), request.getUserId(), request.getShowId(), request.getSeatIds());

        // 1. Validate Show and fetch pricing
        Show show = showRepository.findById(request.getShowId())
                .orElseThrow(() -> new ResourceNotFoundException("Show", request.getShowId()));

        // 2. Deduplicate requested seat IDs (preserving order) to prevent logic errors
        List<Long> seatIds = new ArrayList<>(new LinkedHashSet<>(request.getSeatIds()));

        // 3. Validate every requested seat exists in the DB
        long existingSeatsCount = seatRepository.countByIdIn(seatIds);
        if (existingSeatsCount != seatIds.size()) {
            log.warn("Seat existence check failed. Found {} out of {}", existingSeatsCount, seatIds.size());
            throw new ResourceNotFoundException("One or more Seats", -1L);
        }

        // 4. Server-Side Price Calculation (CRITICAL SECURITY FIX)
        BigDecimal seatPrice = BigDecimal.valueOf(show.getPrice() != null ? show.getPrice() : 0.0);
        BigDecimal serverCalculatedTotal = seatPrice.multiply(BigDecimal.valueOf(seatIds.size()));

        if (request.getTotalAmount().compareTo(serverCalculatedTotal) != 0) {
            log.warn("🚨 PRICE MISMATCH DETECTED! User {} sent {}, Server calculated {}", 
                    request.getUserId(), request.getTotalAmount(), serverCalculatedTotal);
            throw new IllegalArgumentException("Invalid payment amount. Expected: " + serverCalculatedTotal);
        }

        // 5. Concurrency Guard: Ensure seats aren't already booked for this show
        List<Long> conflictingSeats = bookingSeatRepository
                .findBookedSeatIds(request.getShowId(), seatIds, BookingStatus.CONFIRMED);

        if (!conflictingSeats.isEmpty()) {
            log.warn("Seat conflict for showId={}, conflictingSeats={}", request.getShowId(), conflictingSeats);
            throw new SeatsAlreadyBookedException(conflictingSeats);
        }

        // 6. Build booking WITHOUT cascade save (faster)
        Booking booking = Booking.builder()
                .userId(request.getUserId())
                .showId(request.getShowId())
                .totalAmount(serverCalculatedTotal)
                .bookingDateTime(LocalDateTime.now(clock))
                .status(BookingStatus.CONFIRMED)
                .build();

        Booking saved = bookingRepository.save(booking);

        // 7. Bulk insert all seats in one batch (NOT in loop with cascade)
        List<BookingSeat> bookingSeats = seatIds.stream()
                .map(seatId -> BookingSeat.builder()
                        .booking(saved)
                        .seatId(seatId)
                        .build())
                .toList();
        bookingSeatRepository.saveAll(bookingSeats);

        // 8. Batch update available seats (single query, no fetch)
        if (show.getAvailableSeats() != null) {
            showRepository.decrementAvailableSeats(request.getShowId(), seatIds.size());
        }

        log.info("✅ Booking confirmed: bookingId={}, userId={}, showId={}", 
                saved.getId(), saved.getUserId(), saved.getShowId());

        return toResponseDTO(saved);
    }

    /**
     * Cancels an existing booking.
     * Throws {@link BookingNotFoundException} if the booking does not exist.
     * Throws {@link BookingAlreadyCancelledException} if already cancelled.
     */
    @Transactional(rollbackFor = Exception.class)
    public BookingResponseDTO cancelBooking(Long bookingId) {
        Booking booking = fetchBookingOrThrow(bookingId);

        if (booking.getStatus() == BookingStatus.CANCELLED) {
            log.warn("Cancel attempted on already-cancelled booking: {}", bookingId);
            throw new BookingAlreadyCancelledException(bookingId);
        }

        booking.setStatus(BookingStatus.CANCELLED);
        
        // Get seat count without loading all seats (single query)
        long seatCount = bookingSeatRepository.countByBookingId(bookingId);
        
        // Batch update available seats
        if (seatCount > 0) {
            showRepository.incrementAvailableSeats(booking.getShowId(), (int) seatCount);
        }

        log.info("Booking cancelled: bookingId={}, userId={}", bookingId, booking.getUserId());
        return toResponseDTO(booking);
    }

    /**
     * Permanently deletes a booking and associated seats (Admin use or cleanup).
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteBooking(Long bookingId) {
        if (!bookingRepository.existsById(bookingId)) {
            log.warn("Delete attempted on non-existent booking: {}", bookingId);
            throw new BookingNotFoundException(bookingId);
        }

        // Bulk delete to avoid N+1 issues
        bookingSeatRepository.deleteByBookingId(bookingId);
        bookingRepository.deleteById(bookingId);

        log.info("Booking permanently deleted: bookingId={}", bookingId);
    }

    // -------------------------------------------------------------------------
    // QUERIES (Read Operations)
    // -------------------------------------------------------------------------

    /** Returns a paginated list of all bookings (Admin use). */
    @Transactional(readOnly = true)
    public Page<BookingResponseDTO> getAllBookings(Pageable pageable) {
        return bookingRepository.findAll(pageable).map(this::toResponseDTO);
    }

    /** Returns a single booking by ID. */
    @Transactional(readOnly = true)
    public BookingResponseDTO getBookingById(Long bookingId) {
        return toResponseDTO(fetchBookingOrThrow(bookingId));
    }

    /** Returns paginated booking history for a specific user. */
    @Transactional(readOnly = true)
    public Page<BookingResponseDTO> getUserBookingHistory(Long userId, Pageable pageable) {
        return bookingRepository.findByUserId(userId, pageable).map(this::toResponseDTO);
    }

    // -------------------------------------------------------------------------
    // PRIVATE UTILS
    // -------------------------------------------------------------------------

    private Booking fetchBookingOrThrow(Long bookingId) {
        return bookingRepository.findById(bookingId)
                .orElseThrow(() -> {
                    log.warn("Booking not found: bookingId={}", bookingId);
                    return new BookingNotFoundException(bookingId);
                });
    }

    private BookingResponseDTO toResponseDTO(Booking booking) {
        // Fetch only seatIds in a single query (avoid N+1 lazy-loading)
        List<Long> seatIds = bookingSeatRepository.findSeatIdsByBookingId(booking.getId());

        return BookingResponseDTO.builder()
                .bookingId(booking.getId())
                .userId(booking.getUserId())
                .showId(booking.getShowId())
                .totalAmount(booking.getTotalAmount())
                .bookingTime(booking.getBookingDateTime())
                .status(booking.getStatus())
                .seatCount(seatIds.size())
                .seatIds(seatIds)
                .build();
    }
}