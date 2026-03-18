package com.example.New_Project.DTO;

import com.example.New_Project.enums.BookingStatus;
import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * API Response DTO for booking details.
 * 
 * Returned to clients after successful booking creation or retrieval.
 * Contains complete booking information including assigned seat IDs, pricing, and status.
 * 
 * This DTO decouples the REST API contract from internal entity structures,
 * allowing independent evolution of both layers.
 * 
 * @see com.example.New_Project.Entity.Booking
 */
@Data
@Builder
public class BookingResponseDTO {

    /**
     * Unique booking ID generated on successful booking creation.
     * Immutable after creation, used for future references and cancellations.
     */
    @NotNull(message = "Booking ID cannot be null")
    private Long bookingId;

    /**
     * User ID who created this booking.
     * References UserEntity.id in the system.
     */
    @NotNull(message = "User ID cannot be null")
    private Long userId;

    /**
     * Show ID for which the booking was made.
     * References Show.id - the movie show for which seats were booked.
     */
    @NotNull(message = "Show ID cannot be null")
    private Long showId;

    /**
     * Total amount paid for this booking.
     * Calculated as: sum of all seat prices selected.
     * Server-calculated to prevent client manipulation.
     */
    @NotNull(message = "Total amount cannot be null")
    @DecimalMin(value = "0.00", message = "Total amount must be non-negative")
    @Digits(integer = 10, fraction = 2, message = "Total amount must have max 10 digits with 2 decimal places")
    private BigDecimal totalAmount;

    /**
     * Timestamp when booking was created.
     * Generated server-side, cannot be modified by client.
     * Used for audit trail and booking history.
     */
    @NotNull(message = "Booking time cannot be null")
    @PastOrPresent(message = "Booking time cannot be in the future")
    private LocalDateTime bookingTime;

    /**
     * Current status of this booking.
     * Possible values: CONFIRMED, CANCELLED, PENDING
     * Updated by cancellation endpoints or system processes.
     */
    @NotNull(message = "Status cannot be null")
    private BookingStatus status;

    /**
     * Number of seats included in this booking.
     * Derived from seatIds.size(), provided for convenience.
     */
    @NotNull(message = "Seat count cannot be null")
    @Min(value = 1, message = "At least 1 seat must be booked")
    private Integer seatCount;

    /**
     * List of seat IDs assigned to this booking.
     * Each ID corresponds to a Seat entity that was reserved.
     * Created as unmodifiable list by @Singular builder annotation.
     * 
     * Note: Collection is immutable to prevent client-side modifications.
     */
    @NotEmpty(message = "At least one seat must be booked")
    @Builder.Default
    private List<Long> seatIds = List.of();

}


