package com.example.New_Project.Controller;

import com.example.New_Project.DTO.MovieDTO;
import com.example.New_Project.Entity.MovieEntity;
import com.example.New_Project.Service.MovieService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/movies")
@CrossOrigin(origins = "http://localhost:4200") // Connect with Angular
public class MoviesController {

    @Autowired
    private MovieService movieService;

        @PostMapping("/add")
        public ResponseEntity<MovieDTO> addmovies(@RequestBody MovieDTO dto) {
            MovieDTO save = movieService.addmovies(dto);
            return ResponseEntity.ok(save);
        }

    @GetMapping("/all")
    public ResponseEntity<List<MovieEntity>> all(){
            List<MovieEntity> movie = movieService.getAllMovies();
            return ResponseEntity.ok(movie);
        }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> deleteMovie(@PathVariable Long id) {
        return ResponseEntity.ok(movieService.deleteMovie(id));
    }

    @GetMapping("/{id}")
    public ResponseEntity<MovieEntity> getMovieId(@PathVariable Long id){
//        System.out.println("hello");
        MovieEntity movie = movieService.getMoviesById(id);
        return ResponseEntity.ok(movie);
    }
}