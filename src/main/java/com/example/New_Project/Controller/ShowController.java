package com.example.New_Project.Controller;

import com.example.New_Project.DTO.ShowDTO;
import com.example.New_Project.Entity.Show;
import com.example.New_Project.Service.ShowService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/shows")
@CrossOrigin(origins = "http://localhost:4200")
public class ShowController {

    @Autowired
    private ShowService showService;

    @PostMapping("/create")
    public ResponseEntity<Show> createShow(@RequestBody ShowDTO showDTO) {
        Show sh = showService.addShow(showDTO);
        return ResponseEntity.ok(sh);
    }

    @GetMapping("/movie/{movieId}")
    public ResponseEntity<List<Show>> getByMovie(@PathVariable Integer movieId) {
        return ResponseEntity.ok(showService.getShowsForMovie(movieId));
    }

    // NEW: Get shows by screenId — used by manage-theatre
    @GetMapping("/screen/{screenId}")
    public ResponseEntity<List<Show>> getByScreen(@PathVariable Integer screenId) {
        return ResponseEntity.ok(showService.getShowsForScreen(screenId));
    }

    // FIXED: delete endpoint was /{id} — frontend was calling /delete/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<String> removeShow(@PathVariable Long id) {
        showService.deleteShow(id);
        return ResponseEntity.ok("Show timing removed");
    }
}