package com.example.New_Project.DTO;

import lombok.Data;

@Data
public class SeatDTO {
    private Long screenId;      // which screen
    private String seatNumber;  // "1", "2", "3"
    private String seatType;    // "NORMAL" or "PREMIUM"
    private String rowName;     // "A", "B", "C"
    private String status;      // "ACTIVE"
}