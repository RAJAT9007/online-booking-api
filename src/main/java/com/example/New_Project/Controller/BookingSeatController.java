package com.example.New_Project.Controller;

import com.example.New_Project.DTO.BookingSeatDTO;
import com.example.New_Project.Entity.BookingSeat;
import com.example.New_Project.Service.BookingSeatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/booking-seats")
@CrossOrigin(origins = "http://localhost:4200")
public class BookingSeatController {

    @Autowired
    private BookingSeatService bookingSeatService;

    // API: Map a seat to a booking
    @PostMapping("/assign")
    public ResponseEntity<BookingSeat> assign(@RequestBody BookingSeatDTO dto) {
        return ResponseEntity.ok(bookingSeatService.assignSeatToBooking(dto));
    }

    // API: View which seats belong to a specific booking ID
    @GetMapping("/booking/{bookingId}")
    public ResponseEntity<List<BookingSeat>> getByBooking(@PathVariable Long bookingId) {
        return ResponseEntity.ok(bookingSeatService.getSeatsByBooking(bookingId));
    }

    // API: Delete a seat assignment
    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable Long id) {
        bookingSeatService.removeSeatAssignment(id);
        return ResponseEntity.ok("Seat assignment removed");
    }
}