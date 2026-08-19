package com.example.New_Project.Repository;

import com.example.New_Project.Entity.MovieEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import javax.swing.text.html.Option;
import java.util.Optional;

public interface MovieRepository extends JpaRepository<MovieEntity, Long> {

    Optional<MovieEntity> findByTitle(String title);

    Optional<MovieEntity> findById(Long id);

}