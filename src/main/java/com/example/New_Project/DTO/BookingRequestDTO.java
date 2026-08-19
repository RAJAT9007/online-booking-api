package com.example.New_Project.DTO;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Iterator;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingRequestDTO {

    @NotNull(message = "User ID is required")
    @Positive(message = "User ID must be a positive number")
    private Long userId;

    @Positive(message = "Show ID must be a positive number")
    private Long showId;

    // private String seat_id;

    @Size(max = 10, message = "Cannot book more than 10 seats in a single booking")
    private List<@NotNull(message = "Seat ID cannot be null") @Positive(message = "Seat ID must be positive") Long> seatIds;

    @Positive(message = "Total amount must be greater than zero")
    private BigDecimal totalAmount;

    // (message = "Idempotency key is required to prevent duplicate bookings")
    private String idempotencyKey;

    // Transient payload passthrough for Theatre tracking
    private Long theatreId;
}