package com.example.New_Project.Service;

import com.example.New_Project.DTO.SeatDTO;
import com.example.New_Project.Entity.Screen;
import com.example.New_Project.Entity.Seat;
import com.example.New_Project.Exception.ResourceNotFoundException;
import com.example.New_Project.Repository.ScreenRepository;
import com.example.New_Project.Repository.SeatRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for managing seat operations in theatres.
 * Handles manual seat creation and bulk seat generation for screens.
 */
@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class SeatService {

    private final SeatRepository seatRepository;
    private final ScreenRepository screenRepository;

    private static final int TOTAL_ROWS = 10;
    private static final int SEATS_PER_ROW = 15;
    private static final int TOTAL_SEATS = TOTAL_ROWS * SEATS_PER_ROW;
    private static final String[] ROW_NAMES = {"A", "B", "C", "D", "E", "F", "G", "H", "I", "J"};

    /**
     * Manually add seats for a screen with validation.
     * Each seat must have valid screen, row, seat number, and type.
     *
     * @param dtos List of seat DTOs to add
     * @return List of persisted seat entities
     * @throws IllegalArgumentException if dtos is null or empty
     * @throws ResourceNotFoundException if screen not found
     */
    @Transactional(rollbackFor = Exception.class)
    public List<Seat> addSeats(@NotEmpty(message = "Seat list cannot be empty")
                               List<@Valid SeatDTO> dtos) {
        // 1. Validate input
        if (dtos == null || dtos.isEmpty()) {
            log.warn("Attempt to add empty seat list");
            throw new IllegalArgumentException("Seat list cannot be null or empty");
        }

        log.info("Adding {} seats manually", dtos.size());

        // 2. Map DTOs to entities with validation
        List<Seat> seats = dtos.stream().map(dto -> {
            // Verify screen exists
            Screen screen = screenRepository.findById(dto.getScreenId())
                    .orElseThrow(() -> {
                        log.warn("Screen not found during seat creation: {}", dto.getScreenId());
                        return new ResourceNotFoundException("Screen", dto.getScreenId());
                    });

            // Build seat entity
            Seat seat = Seat.builder()
                    .screen(screen)
                    .seatNumber(dto.getSeatNumber())
                    .seatType(dto.getSeatType())
                    .rowName(dto.getRowName())
                    .status("ACTIVE")
                    .build();

            log.debug("Prepared seat: screen={}, row={}, number={}, type={}", 
                    dto.getScreenId(), dto.getRowName(), dto.getSeatNumber(), dto.getSeatType());

            return seat;
        }).collect(Collectors.toList());

        // 3. Batch save all seats at once
        List<Seat> saved = seatRepository.saveAll(seats);
        log.info("✅ {} seats added successfully", saved.size());

        return saved;
    }

    /**
     * Retrieve all seats for a given screen layout.
     * Used to display seat availability for a theatre screen.
     *
     * @param screenId The screen ID
     * @return List of all seats for the screen
     * @throws IllegalArgumentException if screenId is null
     */
    @Transactional(readOnly = true)
    public List<Seat> getLayoutByScreen(Long screenId) {
        if (screenId == null) {
            log.warn("Null screenId provided to getLayoutByScreen");
            throw new IllegalArgumentException("Screen ID cannot be null");
        }

        List<Seat> seats = seatRepository.findByScreen_Id(screenId);
        log.debug("Retrieved {} seats for screen: {}", seats.size(), screenId);

        return seats;
    }

    /**
     * Generate standard seat layout (150 seats: 10 rows × 15 seats per row).
     * Premium seats in last 2 rows, normal seats in first 8 rows.
     * Fails if seats already exist for the screen.
     *
     * @param screenId The screen ID to generate seats for
     * @return Generation result message with count
     * @throws IllegalArgumentException if screenId is null
     * @throws ResourceNotFoundException if screen not found
     * @throws SeatAlreadyGeneratedException if seats already exist for screen
     */
    @Transactional(rollbackFor = Exception.class)
    public String generateSeats(Long screenId) {
        // 1. Validate input
        if (screenId == null) {
            log.warn("Null screenId provided to generateSeats");
            throw new IllegalArgumentException("Screen ID cannot be null");
        }

        // 2. Verify screen exists
        Screen screen = screenRepository.findById(screenId)
                .orElseThrow(() -> {
                    log.warn("Screen not found for seat generation: {}", screenId);
                    return new ResourceNotFoundException("Screen", screenId);
                });

        // 3. Check if seats already generated (idempotency check)
        List<Seat> existing = seatRepository.findByScreen_Id(screenId);
        if (!existing.isEmpty()) {
            log.warn("Seats already generated for screen: {}", screenId);
            throw new SeatAlreadyGeneratedException(
                    "Seats already generated for screen " + screenId + ". Count: " + existing.size()
            );
        }

        log.info("Generating {} seats for screen: {}", TOTAL_SEATS, screenId);

        // 4. Generate seats: Premium in last 2 rows, Normal in first 8 rows
        List<Seat> seats = generateSeatList(screen);

        // 5. Batch save all seats at once
        seatRepository.saveAll(seats);

        log.info("✅ {} seats generated successfully for screen: {}", TOTAL_SEATS, screenId);

        return String.format("%d seats generated for Screen %d successfully!", TOTAL_SEATS, screenId);
    }

    /**
     * Internal helper to generate seat list with proper row and type assignment.
     *
     * @param screen The screen entity
     * @return List of generated seat entities
     */
    private List<Seat> generateSeatList(Screen screen) {
        List<Seat> seats = new ArrayList<>(TOTAL_SEATS);

        for (int r = 0; r < TOTAL_ROWS; r++) {
            for (int s = 1; s <= SEATS_PER_ROW; s++) {
                // Last 2 rows are PREMIUM, first 8 are NORMAL
                String seatType = r >= TOTAL_ROWS - 2 ? "PREMIUM" : "NORMAL";

                Seat seat = Seat.builder()
                        .screen(screen)
                        .rowName(ROW_NAMES[r])
                        .seatNumber(String.valueOf(s))
                        .seatType(seatType)
                        .status("ACTIVE")
                        .build();

                seats.add(seat);
            }
        }

        return seats;
    }
}

/**
 * Thrown when attempting to generate seats for a screen that already has seats.
 */
class SeatAlreadyGeneratedException extends RuntimeException {
    public SeatAlreadyGeneratedException(String message) {
        super(message);
    }
}