package com.example.New_Project.Entity;

import com.example.New_Project.enums.BookingStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
    name = "bookings",
    indexes = {
        @Index(name = "idx_booking_user_id",  columnList = "user_id"),
        @Index(name = "idx_booking_show_id",  columnList = "show_id"),
        @Index(name = "idx_booking_status",   columnList = "status"),
        @Index(name = "idx_booking_time",     columnList = "booking_time")
    }
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Optimistic lock — prevents concurrent modification; requires retry logic in services */
    @Version
    private Long version;

    @NotNull
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @NotNull
    @Column(name = "show_id", nullable = false)
    private Long showId;

    @NotNull
    @PastOrPresent  // Ensures booking time is not in the future
    @Column(name = "booking_time", nullable = false)
    private LocalDateTime bookingDateTime;  // Renamed for clarity

    @NotNull
    @DecimalMin(value = "0.00")  // Ensures non-negative amount
    @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private BookingStatus status;

    /** Cascade: saving/deleting a Booking also saves/deletes its seats */
    @OneToMany(mappedBy = "booking", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<BookingSeat> seats = new ArrayList<>();
}
