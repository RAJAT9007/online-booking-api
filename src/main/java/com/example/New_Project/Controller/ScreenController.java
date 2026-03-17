package com.example.New_Project.Controller;

import com.example.New_Project.DTO.ScreenDTO;
import com.example.New_Project.Entity.Screen;
import com.example.New_Project.Service.ScreenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/screens")
@CrossOrigin(origins = "http://localhost:4200")
public class ScreenController {

    @Autowired
    private ScreenService screenService;

    // API to add a new screen
    @PostMapping("/add")
    public ResponseEntity<Screen> createScreen(@RequestBody ScreenDTO screenDTO) {
        Screen scr = screenService.addScreen(screenDTO);
        return ResponseEntity.ok(scr);
    }

    // API to get all screens belonging to a theatre
    @GetMapping("/theatre/{theatreId}")
    public ResponseEntity<List<Screen>> getByTheatre(@PathVariable Long theatreId) {
        return ResponseEntity.ok(screenService.getScreensByTheatre(theatreId));
    }

    // API to update screen info
    @PutMapping("/update/{id}")
    public ResponseEntity<Screen> update(@PathVariable Long id, @RequestBody ScreenDTO dto) {
        return ResponseEntity.ok(screenService.updateScreen(id, dto));
    }

    // API to delete a screen
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> delete(@PathVariable Long id) {
        screenService.deleteScreen(id);
        return ResponseEntity.ok("Screen removed successfully");
    }
}