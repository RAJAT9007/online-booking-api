package com.example.New_Project.DTO;


import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Data
public class BookingSeatDTO {
        private Long id;
        private Long bookingId;
        private Long seatId;

    }