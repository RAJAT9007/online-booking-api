package com.example.New_Project.Entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "show_seats",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"show_id","seat_id"})
        },
        indexes = {
                @Index(columnList = "show_id"),
                @Index(columnList = "status")
        })
@Data
public class ShowSeat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private Long version;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="show_id", nullable=false)
    private Show show;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="seat_id", nullable=false)
    private Seat seat;

    private Double price;

    private String status; // AVAILABLE LOCKED BOOKED

    private Long lockedByUserId;

    private LocalDateTime lockExpiryTime;
}
