package com.example.New_Project.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(
    name = "booking_seats",
    uniqueConstraints = {
        // Database-level guarantee: a seat cannot be booked twice for the same booking
        @UniqueConstraint(name = "uq_booking_seat", columnNames = {"booking_id", "seat_id"})
    },
    indexes = {
        @Index(name = "idx_booking_seat_booking_id", columnList = "booking_id"),
        @Index(name = "idx_booking_seat_seat_id",    columnList = "seat_id")
    }
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingSeat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** JPA relationship — replaces the manual bookingId long field */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;

    @Column(name = "seat_id", nullable = false)
    private Long seatId;
}
