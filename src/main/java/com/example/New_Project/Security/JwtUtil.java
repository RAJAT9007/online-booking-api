package com.example.New_Project.Security;

import com.example.New_Project.enums.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


@Component
public class JwtUtil {

    @Value("${jwt.secret:${JWT_SECRET:bXlTZWNyZXRLZXlGb3JUaWNrZXRCb29raW5nQXBpV2l0aEF0TGVhc3QyNTZCaXRzT2ZMZW5ndGhGb3JTZWN1cml0eVNpZ25hdHVyZSE=}}")
    private String SECRET;// minimum 32 chars (must be at least 256 bits / 32 characters for HS256)

    private Key getSignKey() {
        byte[] secretBytes = SECRET.getBytes();
        // Ensure secret is at least 256 bits (32 bytes) for HS256
        if (secretBytes.length < 32) {
            throw new IllegalArgumentException("JWT secret key must be at least 256 bits (32 characters). Current length: " + secretBytes.length);
        }
        return Keys.hmacShaKeyFor(secretBytes);
    }

    public String generateToken(UserDetails userDetails, Role role) {

        Map<String, Object> claims = new HashMap<>();
        claims.put("role", role.name());
        if (userDetails instanceof CustomUserDetails) {
            claims.put("userId", ((CustomUserDetails) userDetails).getUser().getId());
        }

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60)) // 1 hour
                .signWith(getSignKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public String extractEmail(String token) {
        return extractAllClaims(token).getSubject();
    }

    public Date extractExpiration(String token) {
        return extractAllClaims(token).getExpiration();
    }
    public boolean isTokenExpired(String token) {
        Date expiration = extractExpiration(token);
        return expiration.before(new Date());
    }

    public boolean validateToken(String token, UserDetails userDetails) {
        String email = extractEmail(token);
        return email.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }
    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSignKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}