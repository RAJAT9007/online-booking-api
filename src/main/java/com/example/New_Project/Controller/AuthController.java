package com.example.New_Project.Controller;

import com.example.New_Project.DTO.LoginRequest;
import com.example.New_Project.DTO.LoginResponse;
import com.example.New_Project.DTO.RegisterRequest;
import com.example.New_Project.Service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin("http://localhost:4200")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<LoginResponse> register(
            @Valid @RequestBody RegisterRequest request) {
        
        log.info(" Registration request received: email={}", request.getEmail());
        
        try {
            LoginResponse response = authService.register(request);
            log.info("Registration successful: email={}", request.getEmail());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
            
        } catch (Exception e) {
            log.error(" Registration failed for email={}: {}",
                    request.getEmail(), e.getMessage());
            throw e; 
        }
    }

    @PostMapping("/login")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest request) {
        
        log.info(" Login request received: email={}", request.getEmail());
        
        try {
            LoginResponse response = authService.login(request);
            log.info(" Login successful: email={}", request.getEmail());
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.warn(" Login failed for email={}: {}",
                    request.getEmail(), e.getMessage());
            throw e;  // Re-throw for @ControllerAdvice to handle
        }
    }
}
