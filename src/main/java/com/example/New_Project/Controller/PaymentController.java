package com.example.New_Project.Controller;

import com.example.New_Project.DTO.PaymentDTO;
import com.example.New_Project.Entity.Payment;
import com.example.New_Project.Service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/payments")
@CrossOrigin(origins = "http://localhost:4200")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    // API: Confirm payment and update booking
    @PostMapping("/confirm")
    public ResponseEntity<Payment> confirmPayment(@RequestBody PaymentDTO paymentDTO) {
        return ResponseEntity.ok(paymentService.processPayment(paymentDTO));
    }

    @PostMapping("/create-checkout-session")
    public ResponseEntity<Map<String, String>> createCheckoutSession(@RequestBody Map<String, Object> req) {
        try {
            Long bookingId = Long.valueOf(req.get("bookingId").toString());
            Double amount = Double.valueOf(req.get("amount").toString());
            Long showId = Long.valueOf(req.get("showId").toString());
            Long theatreId = req.get("theatreId") != null ? Long.valueOf(req.get("theatreId").toString()) : 0L;

            String checkoutUrl = paymentService.createCheckoutSession(bookingId, amount, showId, theatreId);
            Map<String, String> response = new HashMap<>();
            response.put("checkoutUrl", checkoutUrl);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/verify-session")
    public ResponseEntity<?> verifySession(@RequestParam String sessionId, @RequestParam Long bookingId) {
        try {
            boolean success = paymentService.verifySession(sessionId, bookingId);
            if (success) {
                return ResponseEntity.ok().body(Map.of("message", "Payment verified and booking confirmed"));
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "Payment not completed"));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}