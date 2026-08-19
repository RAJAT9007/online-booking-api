package com.example.New_Project.Controller;

import com.example.New_Project.DTO.SeatResponseDTO;
import com.example.New_Project.Service.SeatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST Controller for Seat operations.
 *
 * Endpoints:
 *   GET  /api/seats/screen/{screenId}                       — get all seats for a screen
 *   POST /api/seats/generate/{screenId}                     — manually trigger seat generation
 *   PUT  /api/seats/screen/{screenId}/bulk-price            — bulk update price by seatType
 *   PUT  /api/seats/update/{seatId}                         — update individual seat
 *   PUT  /api/seats/disable/{seatId}                        — disable a seat
 *   PUT  /api/seats/enable/{seatId}                         — enable a seat
 */
@RestController
@RequestMapping("/api/seats")
@CrossOrigin(origins = "http://localhost:4200")
@RequiredArgsConstructor
public class SeatController {

    private final SeatService seatService;

    // ── 1. GET ALL SEATS FOR A SCREEN ──────────────────────────────────────
    @GetMapping("/screen/{screenId}")
    public ResponseEntity<List<SeatResponseDTO>> getScreenLayout(@PathVariable Long screenId) {
        return ResponseEntity.ok(seatService.getLayoutByScreen(screenId));
    }

    // ── 2. MANUAL SEAT GENERATION (fallback — normally auto-triggered) ─────
    @PostMapping("/generate/{screenId}")
    public ResponseEntity<String> generateSeats(@PathVariable Long screenId) {
        return ResponseEntity.ok(seatService.createSeatsForScreen(screenId));
    }

    // ── 3. BULK PRICE UPDATE BY SEAT TYPE ──────────────────────────────────
    /**
     * Request body: { "seatType": "GOLD", "price": 350.0 }
     */
    @PutMapping("/screen/{screenId}/bulk-price")
    public ResponseEntity<String> bulkUpdatePrice(
            @PathVariable Long screenId,
            @RequestBody Map<String, Object> request) {

        String seatType = (String) request.get("seatType");
        Double price    = Double.valueOf(request.get("price").toString());
        seatService.bulkUpdatePrice(screenId, seatType, price);
        return ResponseEntity.ok("Price updated for all " + seatType + " seats on screen " + screenId);
    }

    // ── 4. UPDATE INDIVIDUAL SEAT (price + isActive) ───────────────────────
    /**
     * Request body: { "price": 299.0, "isActive": true }
     */
    @PutMapping("/update/{seatId}")
    public ResponseEntity<SeatResponseDTO> updateSeat(
            @PathVariable Long seatId,
            @RequestBody Map<String, Object> request) {

        Double  price    = request.get("price")    != null ? Double.valueOf(request.get("price").toString())    : null;
        Boolean isActive = request.get("isActive") != null ? Boolean.valueOf(request.get("isActive").toString()) : null;
        return ResponseEntity.ok(seatService.updateSeat(seatId, price, isActive));
    }

    // ── 5. DISABLE SEAT ────────────────────────────────────────────────────
    @PutMapping("/disable/{seatId}")
    public ResponseEntity<SeatResponseDTO> disableSeat(@PathVariable Long seatId) {
        return ResponseEntity.ok(seatService.disableSeat(seatId));
    }

    // ── 6. ENABLE SEAT ─────────────────────────────────────────────────────
    @PutMapping("/enable/{seatId}")
    public ResponseEntity<SeatResponseDTO> enableSeat(@PathVariable Long seatId) {
        return ResponseEntity.ok(seatService.enableSeat(seatId));
    }
}
