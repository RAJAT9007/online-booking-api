package com.example.New_Project.Entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "show_times")
@Data
public class Show {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;              // ← this IS the showId!

    @Column(name = "screen_id", nullable = false)
    private Long screenId;

    @Column(name = "movie_id", nullable = false)
    private Long movieId;

    @Column(name = "start_time")
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    private String language;
    private Double price;

    // ✅ Add these two fields
    private Integer availableSeats;  // ← how many seats left
    private String status;           // ← ACTIVE or CANCELLED
}