package com.example.New_Project.DTO;

import com.example.New_Project.enums.Role;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginResponse {

    private String token;
    private Long id;       // ✅ userId / ownerId — needed by frontend for filtering
    private String email;  // ✅ shown on profile page
    private Role role;     // ✅ used for routing (ADMIN / OWNER / USER)

    // Constructor with only token (used during registration — no user context needed)
    public LoginResponse(String token) {
        this.token = token;
    }

    // ✅ Full constructor — used during login
    public LoginResponse(String token, Long id, String email, Role role) {
        this.token = token;
        this.id = id;
        this.email = email;
        this.role = role;
    }
}