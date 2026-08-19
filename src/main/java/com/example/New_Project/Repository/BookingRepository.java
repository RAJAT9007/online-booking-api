package com.example.New_Project.Repository;

import com.example.New_Project.Entity.Booking;
import com.example.New_Project.enums.BookingStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    /** Paginated: used by the admin list endpoint */
    Page<Booking> findAll(Pageable pageable);

    /** Paginated: user booking history */
    Page<Booking> findByUserId(Long userId, Pageable pageable);

    List<Booking> findByUserId(Long userId);

    /** Used by cancel guard to check if booking is already CANCELLED */
    boolean existsByIdAndStatus(Long id, BookingStatus status);

    /** Finds ghost transactions that were abandoned mid-checkout */
    List<Booking> findByStatusAndBookingDateTimeBefore(BookingStatus status, java.time.LocalDateTime dateTime);

    @Query("SELECT COALESCE(SUM(b.totalAmount), 0) FROM Booking b WHERE b.status = 'CONFIRMED'")
    Double getTotalRevenue();

    @Query("SELECT COALESCE(SUM(b.totalAmount), 0) FROM Booking b, Show s, Screen sc WHERE b.showId = s.id AND s.screenId = sc.id AND sc.theatreId = :theatreId AND b.status = 'CONFIRMED'")
    Double getTotalRevenueByTheatreId(@Param("theatreId") Long theatreId);

    @Query("SELECT COUNT(b) FROM Booking b, Show s, Screen sc WHERE b.showId = s.id AND s.screenId = sc.id AND sc.theatreId = :theatreId AND b.status = 'CONFIRMED'")
    Long countBookingsByTheatreId(@Param("theatreId") Long theatreId);

}