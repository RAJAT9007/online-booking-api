package com.example.New_Project.Repository;

import com.example.New_Project.Entity.Theatre;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TheatreRepository extends JpaRepository<Theatre, Long> {
    List<Theatre> findByCityId(Integer cityId); // To support "Select City" feature [cite: 8, 23]
}