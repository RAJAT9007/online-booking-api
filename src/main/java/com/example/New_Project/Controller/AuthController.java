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

/**
 * REST Controller for authentication operations.
 * Handles user registration and login with JWT token generation.
 * 
 * Base URL: /api/auth
 * CORS: Allows requests from http://localhost:4200 (Angular frontend)
 */
@RestController
@RequestMapping("/api/auth")
@CrossOrigin("http://localhost:4200")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;

    /**
     * Register a new user account.
     * 
     * Accepts user details (name, email, password, role) and creates new account.
     * Returns JWT token for immediate session start after successful registration.
     * 
     * @param request Registration details (must have valid email and strong password)
     * @return JWT token wrapped in LoginResponse
     * @throws UserAlreadyExistsException if email already registered (409 Conflict)
     * @throws IllegalArgumentException if input is invalid (400 Bad Request)
     * 
     * @response 201 Created - User registered, JWT token returned
     * @response 400 Bad Request - Validation failed (weak password, invalid email, etc.)
     * @response 409 Conflict - Email already exists in system
     * 
     * Example Request:
     * POST /api/auth/register
     * {
     *   "name": "John Doe",
     *   "email": "john@example.com",
     *   "password": "SecurePass123",
     *   "role": "USER"
     * }
     * 
     * Example Response:
     * {
     *   "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
     * }
     */
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<LoginResponse> register(
            @Valid @RequestBody RegisterRequest request) {
        
        log.info("📝 Registration request received: email={}", request.getEmail());
        
        try {
            LoginResponse response = authService.register(request);
            log.info("✅ Registration successful: email={}", request.getEmail());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
            
        } catch (Exception e) {
            log.error("❌ Registration failed for email={}: {}", 
                    request.getEmail(), e.getMessage());
            throw e;  // Re-throw for @ControllerAdvice to handle
        }
    }

    /**
     * Authenticate user with email and password.
     * 
     * Validates credentials against stored encrypted password.
     * Returns JWT token containing user ID and role for authenticated requests.
     * 
     * @param request Login credentials (email and password)
     * @return JWT token wrapped in LoginResponse for authenticated session
     * @throws InvalidCredentialsException if credentials don't match (401 Unauthorized)
     * @throws IllegalArgumentException if input is invalid (400 Bad Request)
     * 
     * @response 200 OK - Login successful, JWT token returned
     * @response 400 Bad Request - Validation failed (missing email/password)
     * @response 401 Unauthorized - Invalid email or password
     * 
     * Example Request:
     * POST /api/auth/login
     * {
     *   "email": "john@example.com",
     *   "password": "SecurePass123"
     * }
     * 
     * Example Response:
     * {
     *   "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
     * }
     */
    @PostMapping("/login")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest request) {
        
        log.info("🔐 Login request received: email={}", request.getEmail());
        
        try {
            LoginResponse response = authService.login(request);
            log.info("✅ Login successful: email={}", request.getEmail());
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.warn("❌ Login failed for email={}: {}", 
                    request.getEmail(), e.getMessage());
            throw e;  // Re-throw for @ControllerAdvice to handle
        }
    }
}
