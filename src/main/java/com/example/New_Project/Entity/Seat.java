package com.example.New_Project.Entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "seats", indexes = {
    @Index(name = "idx_seat_screen_id", columnList = "screen_id"),
    @Index(name = "idx_seat_type",      columnList = "seat_type")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Seat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ─── Relationship ───────────────────────────────────────────
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "screen_id", nullable = false)
    @JsonIgnore
    private Screen screen;

    // ─── Core Fields ────────────────────────────────────────────
    @Column(name = "row_name")
    private String rowName;           // "A", "B" … "J"

    @Column(name = "seat_number")
    private String seatNumber;        // "A1", "A2" … "J20"

    @Column(name = "seat_type")
    private String seatType;          // "PREMIUM" | "GOLD" | "SILVER"

    @Column(name = "price")
    @Builder.Default
    private Double price = 0.0;       // owner-configurable price

    // ─── Status ─────────────────────────────────────────────────
    @Column(name = "is_active")
    @Builder.Default
    @Transient
    private Boolean isActive = true;  // false = seat is disabled/blocked

    // ─── Audit ──────────────────────────────────────────────────
    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Transient
    private String status;
}