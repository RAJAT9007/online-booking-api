package com.example.New_Project.Repository;

import com.example.New_Project.Entity.Seat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SeatRepository extends JpaRepository<Seat, Long> {

    List<Seat> findByScreen_Id(Long screenId);

//    List<Seat> findByScreen_IdAndStatus(Long screenId, String status);

    /** Used by BookingService to validate all requested seat IDs exist in a single query */
    long countByIdIn(List<Long> ids);
}