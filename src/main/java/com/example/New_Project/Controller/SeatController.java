package com.example.New_Project.Controller;

import com.example.New_Project.Entity.Seat;
import com.example.New_Project.Service.SeatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/seats")
@CrossOrigin(origins = "http://localhost:4200")
public class SeatController {

    @Autowired
    private SeatService seatService;

    @PostMapping("/generate/{screenId}")
    public ResponseEntity<String> generateSeats(
            @PathVariable Long screenId) {
        String result = seatService.generateSeats(screenId);
        return ResponseEntity.ok(result);
    }
    @GetMapping("/screen/{screenId}")
    public ResponseEntity<List<Seat>> getScreenLayout(@PathVariable Long screenId) {
        return ResponseEntity.ok(seatService.getLayoutByScreen(screenId));
    }
}
