package com.example.New_Project.Repository;

import com.example.New_Project.Entity.Booking;
import com.example.New_Project.enums.BookingStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    /** Paginated: used by the admin list endpoint */
    Page<Booking> findAll(Pageable pageable);

    /** Paginated: user booking history */
    Page<Booking> findByUserId(Long userId, Pageable pageable);

    /** Used by cancel guard to check if booking is already CANCELLED */
    boolean existsByIdAndStatus(Long id, BookingStatus status);
}