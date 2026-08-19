package com.example.New_Project.Repository;

import com.example.New_Project.Entity.Booking;
import com.example.New_Project.Entity.Seat;
import com.example.New_Project.Entity.ShowSeat;
import com.example.New_Project.enums.BookingStatus;
import io.lettuce.core.dynamic.annotation.Param;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ShowSeatRepository extends JpaRepository<ShowSeat, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select ss from ShowSeat ss where ss.id in :ids")
    List<ShowSeat> findAllForUpdate(List<Long> ids);

    List<ShowSeat> findByShow_Id(Long showId);

    @Modifying
    @Query("update ShowSeat ss set ss.status='BOOKED', ss.lockedByUserId=null, ss.lockExpiryTime=null where ss.id in :ids")
    void markBooked(List<Long> ids);

    @Modifying
    @Query("update ShowSeat ss set ss.status='AVAILABLE', ss.lockedByUserId=null, ss.lockExpiryTime=null where ss.id in :ids")
    void releaseSeats(List<Long> ids);

    @Query("SELECT COUNT(ss) FROM ShowSeat ss WHERE ss.status = :status")
    Long countByStatus(@Param("status") String status);
}
