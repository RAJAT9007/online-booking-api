package com.example.New_Project.Repository;

import com.example.New_Project.Entity.BookingSeat;
import com.example.New_Project.enums.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookingSeatRepository extends JpaRepository<BookingSeat, Long> {

    /**
     * Concurrency guard: returns any requested seat IDs already CONFIRMED for this show.
     * Uses BookingStatus enum parameter — not a raw string — to stay type-safe.
     */
    @Query("SELECT bs.seatId FROM BookingSeat bs " +
           "WHERE bs.booking.showId = :showId " +
           "AND bs.seatId IN :seatIds " +
           "AND bs.booking.status = :status")
    List<Long> findBookedSeatIds(
            @Param("showId") Long showId,
            @Param("seatIds") List<Long> seatIds,
            @Param("status") BookingStatus status
    );

    /**
     * Bulk delete: single DELETE WHERE booking_id = X.
     * Used by deleteBooking — replaces the old fetch-then-deleteAll N+1 pattern.
     */
    @Modifying
    @Query("DELETE FROM BookingSeat bs WHERE bs.booking.id = :bookingId")
    void deleteByBookingId(@Param("bookingId") Long bookingId);

    List<BookingSeat> findByBookingId(Long bookingId);

    /**
     * Fetch only seat IDs for a booking without loading full BookingSeat entities.
     * Improves performance by avoiding N+1 lazy-load issues.
     */
    @Query("SELECT bs.seatId FROM BookingSeat bs WHERE bs.booking.id = :bookingId")
    List<Long> findSeatIdsByBookingId(@Param("bookingId") Long bookingId);

    /**
     * Count seats for a booking efficiently without fetching entities.
     */
    @Query("SELECT COUNT(bs) FROM BookingSeat bs WHERE bs.booking.id = :bookingId")
    long countByBookingId(@Param("bookingId") Long bookingId);

    /**
     * Get all seat IDs ever associated with a show (any booking status).
     * Used as the candidate list when checking for booking conflicts.
     */
    @Query("SELECT bs.seatId FROM BookingSeat bs WHERE bs.booking.showId = :showId")
    List<Long> findAllSeatIdsForShow(@Param("showId") Long showId);
}