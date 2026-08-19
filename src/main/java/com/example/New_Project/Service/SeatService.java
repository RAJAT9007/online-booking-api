package com.example.New_Project.Service;

import com.example.New_Project.DTO.SeatResponseDTO;
import com.example.New_Project.Entity.Screen;
import com.example.New_Project.Entity.Seat;
import com.example.New_Project.Exception.ResourceNotFoundException;
import com.example.New_Project.Repository.ScreenRepository;
import com.example.New_Project.Repository.SeatRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class SeatService {

    private final SeatRepository   seatRepository;
    private final ScreenRepository screenRepository;

    // ── Layout Constants ────────────────────────────────────────────────────
    private static final int SEATS_PER_ROW = 20;
    private static final String[] ROW_NAMES = {"A","B","C","D","E","F","G","H","I","J"};

    // Type distribution
    private static final int PREMIUM_ROWS = 3;  // A, B, C  → indices 0,1,2
    private static final int GOLD_ROWS    = 4;  // D,E,F,G  → indices 3,4,5,6
    // SILVER_ROWS = remaining (H,I,J → indices 7,8,9)

    // Default prices
    private static final double DEFAULT_PREMIUM_PRICE = 500.0;
    private static final double DEFAULT_GOLD_PRICE    = 300.0;
    private static final double DEFAULT_SILVER_PRICE  = 150.0;

    // ── 1. AUTO-GENERATE SEATS FOR A SCREEN ────────────────────────────────

    /**
     * Automatically generates 200 seats for the given screen.
     * Called by ScreenService.addScreen() — no need to call manually.
     * Idempotent: skips generation if seats already exist.
     *
     * @param screenId the screen to generate seats for
     */
    public String createSeatsForScreen(Long screenId) {
        if (screenId == null) throw new IllegalArgumentException("screenId cannot be null");

        Screen screen = screenRepository.findById(screenId)
                .orElseThrow(() -> new ResourceNotFoundException("Screen", screenId));

        // Idempotency check — never generate twice
        List<Seat> existing = seatRepository.findByScreen_Id(screenId);
        if (!existing.isEmpty()) {
            log.warn("Seats already exist for screen {}: {} seats. Skipping generation.", screenId, existing.size());
            return "Seats already generated. Count: " + existing.size();
        }

        List<Seat> seats = new ArrayList<>(ROW_NAMES.length * SEATS_PER_ROW);

        for (int r = 0; r < ROW_NAMES.length; r++) {
            String rowName  = ROW_NAMES[r];
            String seatType = getSeatType(r);
            double price    = getDefaultPrice(seatType);

            for (int col = 1; col <= SEATS_PER_ROW; col++) {
                seats.add(Seat.builder()
                        .screen(screen)
                        .rowName(rowName)
                        .seatNumber(rowName + col)   // "A1", "A2" … "J20"
                        .seatType(seatType)
                        .price(price)
                        .isActive(true)
                        .build());
            }
        }

        seatRepository.saveAll(seats);
        log.info(" {} seats generated for screen {}", seats.size(), screenId);
        return seats.size() + " seats generated for Screen " + screenId;
    }

    // ── 2. FETCH SEAT LAYOUT FOR A SCREEN ──────────────────────────────────

    /**
     * Returns all seats for a screen as response DTOs.
     * Used by the seat-booking page and manage-theatre pricing panel.
     */
    @Transactional(readOnly = true)
    public List<SeatResponseDTO> getLayoutByScreen(Long screenId) {
        if (screenId == null) throw new IllegalArgumentException("screenId cannot be null");
        List<Seat> seats = seatRepository.findByScreen_Id(screenId);
        return seats.stream().map(this::toResponseDTO).collect(Collectors.toList());
    }

    // ── 3. BULK PRICE UPDATE BY SEAT TYPE ──────────────────────────────────

    /**
     * Updates the price for all seats of a given type on a screen.
     * Example: set all GOLD seats on screen 1 to ₹350.
     *
     * @param screenId the screen
     * @param seatType "PREMIUM" | "GOLD" | "SILVER"
     * @param newPrice the new price
     */
    public void bulkUpdatePrice(Long screenId, String seatType, Double newPrice) {
        if (screenId == null || seatType == null || newPrice == null)
            throw new IllegalArgumentException("screenId, seatType and newPrice are all required");
        if (newPrice < 0) throw new IllegalArgumentException("Price cannot be negative");

        List<Seat> seats = seatRepository.findByScreen_Id(screenId);
        seats.stream()
             .filter(s -> s.getSeatType().equalsIgnoreCase(seatType))
             .forEach(s -> s.setPrice(newPrice));
        seatRepository.saveAll(seats);
        log.info(" Bulk price updated: screen={}, type={}, price={}", screenId, seatType, newPrice);
    }

    // ── 4. UPDATE INDIVIDUAL SEAT ───────────────────────────────────────────

    /**
     * Updates a single seat's price and/or isActive flag.
     */
    public SeatResponseDTO updateSeat(Long seatId, Double price, Boolean isActive) {
        Seat seat = seatRepository.findById(seatId)
                .orElseThrow(() -> new ResourceNotFoundException("Seat", seatId));

        if (price    != null) seat.setPrice(price);
        if (isActive != null) seat.setIsActive(isActive);

        Seat saved = seatRepository.save(seat);
        log.info("Seat {} updated: price={}, isActive={}", seatId, price, isActive);
        return toResponseDTO(saved);
    }

    // ── 5. DISABLE / ENABLE SEAT ───────────────────────────────────────────

    /**
     * Disables a seat (marks isActive = false).
     * Disabled seats cannot be booked by users.
     */
    public SeatResponseDTO disableSeat(Long seatId) {
        return updateSeat(seatId, null, false);
    }

    /**
     * Re-enables a previously disabled seat.
     */
    public SeatResponseDTO enableSeat(Long seatId) {
        return updateSeat(seatId, null, true);
    }

    // ── 6. BOOKED SEAT IDS FOR A SHOW ──────────────────────────────────────
    // Note: This is handled by BookingController → /api/bookings/booked-seats/{showId}
    // The BookingSeatRepository.findBookedSeatIds() already supports this query.

    // ── Private Helpers ─────────────────────────────────────────────────────

    private String getSeatType(int rowIndex) {
        if (rowIndex < PREMIUM_ROWS)             return "PREMIUM";
        if (rowIndex < PREMIUM_ROWS + GOLD_ROWS) return "GOLD";
        return "SILVER";
    }

    private double getDefaultPrice(String seatType) {
        return switch (seatType) {
            case "PREMIUM" -> DEFAULT_PREMIUM_PRICE;
            case "GOLD"    -> DEFAULT_GOLD_PRICE;
            default        -> DEFAULT_SILVER_PRICE;
        };
    }

    public SeatResponseDTO toResponseDTO(Seat seat) {
        return SeatResponseDTO.builder()
                .id(seat.getId())
                .rowName(seat.getRowName())
                .seatNumber(seat.getSeatNumber())
                .seatType(seat.getSeatType())
                .price(seat.getPrice())
                .isActive(seat.getIsActive())
                .screenId(seat.getScreen() != null ? seat.getScreen().getId() : null)
                .build();
    }
}

/**
 * Thrown when attempting to generate seats for a screen that already has seats.
 */
class SeatAlreadyGeneratedException extends RuntimeException {
    public SeatAlreadyGeneratedException(String message) { super(message); }
}