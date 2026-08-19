package com.example.New_Project.Service;

import com.example.New_Project.DTO.MovieDTO;
import com.example.New_Project.Entity.MovieEntity;
import com.example.New_Project.Repository.MovieRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class MovieService {

    @Autowired
    private MovieRepository moviesRepository;

    public MovieDTO addmovie(MovieDTO dto) {

        MovieEntity entity = MovieEntity.builder()
                .title(dto.getTitle())
                .description(dto.getDescription())
                .duration_minutes(dto.getDuration_minutes())
                .language(dto.getLanguage())
                .genre(dto.getGenre())
                .poster_Url(dto.getPoster_Url())
                // .created_at(dto.getCreated_at())
                // .created_by(dto.getCreated_by())
                .status(dto.getStatus())
                .build();

        MovieEntity savedMovie = moviesRepository.save(entity);

        MovieDTO response = MovieDTO.builder()
                .title(savedMovie.getTitle())
                .description(savedMovie.getDescription())
                .duration_minutes(savedMovie.getDuration_minutes())
                .language(savedMovie.getLanguage())
                .genre(savedMovie.getGenre())
                .poster_Url(savedMovie.getPoster_Url())
                .build();

        return response;

    }

    public MovieDTO getMoviesById(Long id) {
        MovieEntity movieEntity = moviesRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Movie not found with id: " + id));

        MovieDTO response = MovieDTO.builder()
                .id(movieEntity.getId())
                .title(movieEntity.getTitle())
                .description(movieEntity.getDescription())
                .duration_minutes(movieEntity.getDuration_minutes())
                .language(movieEntity.getLanguage())
                .genre(movieEntity.getGenre())
                .poster_Url(movieEntity.getPoster_Url())
                .build();

        return response;
    }

    public List<MovieEntity> getAllMovies() {
        return moviesRepository.findAll();
    }

    public String deleteMovie(Long id) {
        if (!moviesRepository.existsById(id)) {
            throw new RuntimeException("Movie not found with id: " + id);
        }
        moviesRepository.deleteById(id);
        return "Movie is delete : " + id;
    }

    public MovieDTO updateMovie(Long id, MovieDTO dto) {
        MovieEntity movieEntity = moviesRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Movie not found with id: " + id));

        movieEntity.setTitle(dto.getTitle());
        movieEntity.setDescription(dto.getDescription());
        movieEntity.setDuration_minutes(dto.getDuration_minutes());
        movieEntity.setLanguage(dto.getLanguage());
        movieEntity.setGenre(dto.getGenre());
        movieEntity.setPoster_Url(dto.getPoster_Url());

        MovieEntity updatedMovie = moviesRepository.save(movieEntity);

        MovieDTO response = MovieDTO.builder()
                .title(updatedMovie.getTitle())
                .description(updatedMovie.getDescription())
                .duration_minutes(updatedMovie.getDuration_minutes())
                .language(updatedMovie.getLanguage())
                .genre(updatedMovie.getGenre())
                .poster_Url(updatedMovie.getPoster_Url())
                .build();

        return response;
    }

    private List<MovieDTO> applySlidingWindow(List<MovieEntity> movies, String keyword) {
        List<MovieDTO> result = new ArrayList<>();

        String cleanKeyword = keyword.toLowerCase().trim();
        int windowSize = cleanKeyword.length();

        for (MovieEntity movie : movies) {
            if (movie.getTitle() == null) continue;
            String title = movie.getTitle().toLowerCase();

            if (windowSize > title.length()) {
                continue;
            }

            // Check if title starts with the keyword, OR if there's a word starting with keyword (i.e. space + keyword)
            if (title.startsWith(cleanKeyword) || title.contains(" " + cleanKeyword)) {
                MovieDTO dto = MovieDTO.builder()
                        .id(movie.getId())
                        .title(movie.getTitle())
                        .description(movie.getDescription())
                        .duration_minutes(movie.getDuration_minutes())
                        .language(movie.getLanguage())
                        .genre(movie.getGenre())
                        .poster_Url(movie.getPoster_Url())
                        .build();

                result.add(dto);
            }
        }
        return result;
    }

    public List<MovieDTO> searchMoviesByTitle(String keyword) {
        List<MovieEntity> movies = moviesRepository.findAll();
        return applySlidingWindow(movies, keyword);
    }
}