package com.example.New_Project.Service;

import com.example.New_Project.DTO.MovieDTO;
import com.example.New_Project.Entity.MovieEntity;
import com.example.New_Project.Repository.MovieRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MovieService {

    @Autowired
    private MovieRepository moviesRepository;

    public MovieDTO addmovies(MovieDTO dto) {

        MovieEntity entity = MovieEntity.builder()
                .title(dto.getTitle())
                .description(dto.getDescription())
                .duration_minutes(dto.getDuration_minutes())
                .language(dto.getLanguage())
                .genre(dto.getGenre())
                .poster_Url(dto.getPoster_Url())
//                .created_at(dto.getCreated_at())
//                .created_by(dto.getCreated_by())
                .status(dto.getStatus())
                .build();

        MovieEntity savedMovies = moviesRepository.save(entity);

        MovieDTO response = MovieDTO.builder()
                .title(dto.getTitle())
                .description(dto.getDescription())
                .duration_minutes(dto.getDuration_minutes())
                .language(dto.getLanguage())
                .genre(dto.getGenre())
                .poster_Url(dto.getPoster_Url())
                .build();

        return response;

    }

    public MovieEntity getMoviesById(Long id) {
        return moviesRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Movie Not Found" + id ));
    }

    public List<MovieEntity> getAllMovies() {
        return moviesRepository.findAll();
    }

    public String deleteMovie(Long id){
        if (!moviesRepository.existsById(id)) {
            throw new RuntimeException("Movie not found with id: " + id);
        }
        moviesRepository.deleteById(id);

        return "Movie is delete : " + id ;
    }

}