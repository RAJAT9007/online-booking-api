package com.example.New_Project.DTO;

import lombok.Builder;
import lombok.Data;

/**
 * API response DTO for a Seat.
 * Avoids exposing JPA lazy-load proxies directly.
 */
@Data
@Builder
public class SeatResponseDTO {
    private Long   id;
    private String rowName;      // "A"
    private String seatNumber;   // "A1"
    private String seatType;     // "PREMIUM" | "GOLD" | "SILVER"
    private Double price;
    private Boolean isActive;
    private Long   screenId;     // convenience field for frontend
}
