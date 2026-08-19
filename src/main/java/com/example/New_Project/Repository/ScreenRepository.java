package com.example.New_Project.Repository;

import com.example.New_Project.Entity.Screen;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ScreenRepository extends JpaRepository<Screen, Long> {

    List<Screen> findByTheatreId(Long theatreId);

    List<Screen> findByTheatreIdAndStatus(Integer theatreId, String status);

}