package com.example.New_Project.DTO;

import lombok.Data;

/**
 * General seat DTO — used for creation and price-update requests.
 */
@Data
public class SeatDTO {
    private Long   screenId;    // which screen
    private String seatNumber;  // "A1"
    private String seatType;    // "PREMIUM" | "GOLD" | "SILVER"
    private String rowName;     // "A"
    private Double price;       // owner-configured price
    private String status;      // "ACTIVE" | "BLOCKED"
}