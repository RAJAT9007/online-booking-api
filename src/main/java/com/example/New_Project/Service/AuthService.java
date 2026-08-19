package com.example.New_Project.Service;

import com.example.New_Project.DTO.LoginRequest;
import com.example.New_Project.DTO.LoginResponse;
import com.example.New_Project.DTO.RegisterRequest;
import com.example.New_Project.Entity.UserEntity;
import com.example.New_Project.Repository.UserRepository;
import com.example.New_Project.Security.CustomUserDetails;
import com.example.New_Project.Security.JwtUtil;
import com.example.New_Project.enums.Role;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Authentication service handling user registration and login.
 * Provides secure credential management with JWT token generation.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    @Transactional(rollbackFor = Exception.class)
    public LoginResponse register(@Valid RegisterRequest request) {
        // 1. Validate input
        if (request == null) {
            log.warn("Null registration request received");
            throw new IllegalArgumentException("Registration request cannot be null");
        }

        // 2. Check email uniqueness
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            log.warn("Registration failed - email already exists: {}", request.getEmail());
            throw new UserAlreadyExistsException("Email already registered: " + request.getEmail());
        }

        // 3. Encode password and build user
        UserEntity user = UserEntity.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .number(request.getNumber())
                .role(request.getRole() != null ? request.getRole() : Role.USER)
                .build();

        // 4. Save and generate token
        UserEntity saved = userRepository.save(user);

        // Create JWT token for immediate login after registration
        String token = jwtUtil.generateToken(
                new CustomUserDetails(saved),
                saved.getRole()
        );

        log.info(" User registered successfully: id={}, email={}, role={}, number={}",
                saved.getId(), saved.getEmail(), saved.getRole(), saved.getNumber());

        return new LoginResponse(token);
    }

    /**
     * Authenticate user with email and password.
     * Validates credentials against stored encrypted password and generates JWT token.
     *
     * @param request Contains email and password
     * @return JWT token for authenticated session
     * @throws IllegalArgumentException if request is null
     * @throws InvalidCredentialsException if credentials don't match
     * @throws AccountLockedException if account is locked (future enhancement)
     */
    @Transactional(readOnly = true)
    public LoginResponse login(@Valid LoginRequest request) {
        // 1. Validate input
        if (request == null) {
            log.warn("Null login request received");
            throw new IllegalArgumentException("Login request cannot be null");
        }

        try {
            // 2. Authenticate with provided credentials
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );

            // 3. Extract user details and generate token
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            UserEntity user = userDetails.getUser();
            String token = jwtUtil.generateToken(userDetails, user.getRole());

            log.info(" User logged in successfully: id={}, email={}, role={}",
                    user.getId(), user.getEmail(), user.getRole());

            //  Return token + user info so frontend can persist email, role, ownerId
            return new LoginResponse(token, user.getId(), user.getEmail(), user.getRole());

        } catch (BadCredentialsException e) {
            log.warn("Login failed - invalid credentials for email: {}", request.getEmail());
            throw new InvalidCredentialsException("Invalid email or password");
        } catch (Exception e) {
            log.error("Unexpected error during login for email: {}", request.getEmail(), e);
            throw new AuthenticationException("Authentication failed: " + e.getMessage());
        }
    }

    /**
     * Validates user exists and is active (helper method for other services).
     *
     * @param userId The user ID to validate
     * @return User entity if found and active
     * @throws ResourceNotFoundException if user not found
     */
    @Transactional(readOnly = true)
    public UserEntity validateUserExists(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }

        return userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("User validation failed - user not found: {}", userId);
                    return new ResourceNotFoundException("User", userId);
                });
    }
}

// Custom Exceptions for Auth Service

class UserAlreadyExistsException extends RuntimeException {
    public UserAlreadyExistsException(String message) {
        super(message);
    }
}

class InvalidCredentialsException extends RuntimeException {
    public InvalidCredentialsException(String message) {
        super(message);
    }
}

class AccountLockedException extends RuntimeException {
    public AccountLockedException(String email) {
        super("Account is locked for email: " + email);
    }
}

class AuthenticationException extends RuntimeException {
    public AuthenticationException(String message) {
        super(message);
    }
}

class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String resourceName, Long id) {
        super(resourceName + " not found with ID: " + id);
    }
}