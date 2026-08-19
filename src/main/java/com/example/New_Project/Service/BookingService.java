package com.example.New_Project.Service;

import com.example.New_Project.DTO.BookingRequestDTO;
import com.example.New_Project.DTO.BookingResponseDTO;
import com.example.New_Project.Entity.Booking;
import com.example.New_Project.Entity.BookingSeat;
import com.example.New_Project.Entity.Seat;
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
import jakarta.validation.constraints.NotNull;
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

    private final BookingRepository bookingRepository;
    private final BookingSeatRepository bookingSeatRepository;
    private final ShowRepository showRepository;
    private final SeatRepository seatRepository;
    private final Clock clock;

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

        log.info(
                "Processing booking creation [IdempotencyKey={}]: userId={}, showId={}, requestedSeats={}, requestedTheaterId={}",
                request.getIdempotencyKey(), request.getUserId(), request.getShowId(), request.getSeatIds(),
                request.getTheatreId());

        // 1. Validate Show and fetch pricing
        Show show = showRepository.findById(request.getShowId())
                .orElseThrow(() -> new ResourceNotFoundException("Show", request.getShowId()));

        // 2. Deduplicate requested seat IDs (preserving order) to prevent logic errors
        List<Long> seatIds = new ArrayList<>(new LinkedHashSet<>(request.getSeatIds()));

        // 3. Fetch Seats, Validate Existence
        List<Seat> seats = seatRepository.findAllById(seatIds);
        if (seats.size() != seatIds.size()) {
            log.warn("Seat existence check failed. Found {} out of {}", seats.size(), seatIds.size());
            throw new ResourceNotFoundException("One or more Seats", -1L);
        }

        // 4. Server-Side Price Calculation (CRITICAL SECURITY FIX)
        BigDecimal serverCalculatedTotal = seats.stream()
                .map(seat -> BigDecimal.valueOf(seat.getPrice() != null ? seat.getPrice() : 0.0))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (request.getTotalAmount().compareTo(serverCalculatedTotal) != 0) {
            log.warn("🚨 PRICE MISMATCH DETECTED! User {} sent {}, Server calculated {}",
                    request.getUserId(), request.getTotalAmount(), serverCalculatedTotal);
            throw new IllegalArgumentException("Invalid payment amount. Expected: " + serverCalculatedTotal);
        }

        // 5. Concurrency Guard: Ensure seats aren't already booked or actively being
        // paid for
        List<Long> conflictingSeats = new ArrayList<>();
        conflictingSeats
                .addAll(bookingSeatRepository.findBookedSeatIds(request.getShowId(), seatIds, BookingStatus.CONFIRMED));
        conflictingSeats
                .addAll(bookingSeatRepository.findBookedSeatIds(request.getShowId(), seatIds, BookingStatus.PENDING));

        if (!conflictingSeats.isEmpty()) {
            log.warn("Seat conflict for showId={}, conflictingSeats={}", request.getShowId(), conflictingSeats);
            throw new SeatsAlreadyBookedException(conflictingSeats);
        }

        // 6. Build booking WITHOUT cascade save (faster)
        Booking booking = Booking.builder()
                .userId(request.getUserId())
                .showId(request.getShowId())
                .theatreId(request.getTheatreId())
                .totalAmount(serverCalculatedTotal)
                .bookingDateTime(LocalDateTime.now(clock))
                .status(BookingStatus.PENDING)
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

        log.info(" Booking confirmed: bookingId={}, userId={}, showId={}, theaterId={}",
                saved.getId(), saved.getUserId(), saved.getShowId(), saved.getTheatreId());

        return toResponseDTO(saved);
    }

    @Transactional
    public BookingResponseDTO confirmBookingPayment(Long bookingId) {

        Booking booking = fetchBookingOrThrow(bookingId);

        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new RuntimeException("Booking not in pending state");
        }

        // Re-check seat conflicts
        List<Long> seatIds = bookingSeatRepository.findSeatIdsByBookingId(bookingId);

        List<Long> conflicts = bookingSeatRepository.findBookedSeatIds(
                booking.getShowId(),
                seatIds,
                BookingStatus.CONFIRMED);

        if (!conflicts.isEmpty()) {
            throw new SeatsAlreadyBookedException(conflicts);
        }

        booking.setStatus(BookingStatus.CONFIRMED);

        return toResponseDTO(booking);
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

    /**
     * Returns a paginated list of all bookings (Admin use).
     */
    @Transactional(readOnly = true)
    public Page<BookingResponseDTO> getAllBookings(Pageable pageable) {
        return bookingRepository.findAll(pageable).map(this::toResponseDTO);
    }

    /**
     * Returns a single booking by ID.
     */
    @Transactional(readOnly = true)
    public BookingResponseDTO getBookingById(Long bookingId) {
        return toResponseDTO(fetchBookingOrThrow(bookingId));
    }

    /**
     * Returns paginated booking history for a specific user.
     */
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

    private @NotNull Long getUserId() {

        // In a real application, this would come from the security context or session
        // For this example, we'll just return a placeholder value
        return null; // TODO: Replace with actual user ID retrieval logic
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

    public List<Booking> getBookingsByUserId(Long userId) {
        // This avoids the 'bookingTime' property error entirely
        return bookingRepository.findByUserId(userId);
    }

    // -------------------------------------------------------------------------
    // CRON JOBS (Automated Server-Side Cleanup)
    // -------------------------------------------------------------------------

    /**
     * Cleans up completely orphaned bookings that were abandoned mid-checkout.
     * Evaluates every 60 seconds. Flushes seats older than 10 minutes.
     */
    @org.springframework.scheduling.annotation.Scheduled(fixedRate = 60000)
    @Transactional(rollbackFor = Exception.class)
    public void cleanupAbandonedBookings() {
        LocalDateTime cutoff = LocalDateTime.now(clock).minusMinutes(10);
        List<Booking> abandoned = bookingRepository.findByStatusAndBookingDateTimeBefore(BookingStatus.PENDING, cutoff);

        for (Booking booking : abandoned) {
            log.info("🧹 Sweeping ghost transaction: Canceling expired PENDING booking: {}", booking.getId());

            booking.setStatus(BookingStatus.CANCELLED);

            long seatCount = bookingSeatRepository.countByBookingId(booking.getId());
            if (seatCount > 0) {
                showRepository.incrementAvailableSeats(booking.getShowId(), (int) seatCount);
            }
        }
    }

}