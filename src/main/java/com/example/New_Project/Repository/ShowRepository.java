package com.example.New_Project.Repository;

import com.example.New_Project.Entity.Show;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface ShowRepository extends JpaRepository<Show, Long> {

    List<Show> findByMovieId(Integer movieId);
    List<Show> findByScreenId(Integer screenId);

    @Modifying
    @Transactional
    @Query("UPDATE Show s SET s.availableSeats = s.availableSeats + :seatCount WHERE s.id = :showId")
    void incrementAvailableSeats(@Param("showId") Long showId, @Param("seatCount") int seatCount);

    @Modifying
    @Transactional
    @Query("UPDATE Show s SET s.availableSeats = s.availableSeats - :size WHERE s.id = :showId AND s.availableSeats >= :size")
    void decrementAvailableSeats(
            @Param("showId") @NotNull @Positive Long showId,
            @Param("size") int size
    );
}