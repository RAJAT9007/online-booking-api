package com.example.New_Project.Controller;

import com.example.New_Project.DTO.TheatreDTO;
import com.example.New_Project.Entity.Theatre;
import com.example.New_Project.Service.TheatreService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
// api/theatre/add
@RestController
@RequestMapping("/api/theatre")
@CrossOrigin(origins = "http://localhost:4200") // Connect with Angular
public class TheatreController {

    @Autowired
    private TheatreService theatreService;

   //  API to add a theatre [cite: 45]
    @PostMapping("/add")
    public ResponseEntity<Theatre> addTheatre(@Valid @RequestBody  TheatreDTO theatre) {
        Theatre save = theatreService.saveTheatre(theatre);
        return ResponseEntity.ok(save);
    }

    // API to view all theatres [cite: 12]
    @GetMapping("/all")
    public ResponseEntity<List<Theatre>> getAll() {
        return ResponseEntity.ok(theatreService.getAllTheatres());
    }

    // API to get a single theatre detail [cite: 12]
    @GetMapping("/{id}")
    public ResponseEntity<Theatre> getById(@PathVariable Long id) {
        return theatreService.getTheatreById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // API to update theatre info [cite: 38]
    @PutMapping("/update/{id}")
    public ResponseEntity<Theatre> update(@PathVariable Long id, @RequestBody TheatreDTO dto) {
        return ResponseEntity.ok(theatreService.updateTheatre(id, dto));
    }

    // API to delete a theatre [cite: 38]
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> delete(@PathVariable Long id) {
        theatreService.deleteTheatre(id);
        return ResponseEntity.ok("Theatre deleted successfully");
    }
}