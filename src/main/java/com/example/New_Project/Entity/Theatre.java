package com.example.New_Project.Entity;

import com.example.New_Project.enums.TheatreStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Theatre Entity represents a cinema theatre/venue in the system.
 * 
 * This entity manages core theatre information including identification,
 * location, ownership, and operational status. It maintains relationships
 * with cities, owners (users), and screens.
 * 
 * @author System
 * @version 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
    name = "theatres",
    indexes = {
        @Index(name = "idx_theatre_id", columnList = "theatre_id", unique = true),
        @Index(name = "idx_city_id", columnList = "city_id"),
        @Index(name = "idx_owner_id", columnList = "owner_id"),
        @Index(name = "idx_status", columnList = "status")
    }
)
public class Theatre {
    
    /**
     * Unique identifier for the theatre
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Unique registration ID for the theatre (immutable)
     * Format: THEATRE_[timestamp]_[random] recommended
     */
    @NotBlank(message = "Theatre registration ID cannot be blank")
    @Size(min = 5, max = 50, message = "Theatre registration ID must be between 5 and 50 characters")
    @Column(unique = true, updatable = false, nullable = false, length = 50, name = "register_id")
    private String theatreId;

    /**
     * Display name of the theatre
     */
    @NotBlank(message = "Theatre name cannot be blank")
    @Size(min = 3, max = 255, message = "Theatre name must be between 3 and 255 characters")
    @Column(nullable = false, length = 255)
    private String name;

    /**
     * Physical address of the theatre
     */
    @NotBlank(message = "Theatre address cannot be blank")
    @Size(min = 5, max = 500, message = "Theatre address must be between 5 and 500 characters")
    @Column(nullable = false, length = 500)
    private String address;

    /**
     * Foreign key reference to the city where the theatre is located
     */
    @NotNull(message = "City ID cannot be null")
    @Column(name = "city_id", nullable = false)
    private Long cityId;

    /**
     * Foreign key reference to the owner (user) of the theatre
     */
    @NotNull(message = "Owner ID cannot be null")
    @Column(name = "owner_id", nullable = false)
    private Long ownerId;

    /**
     * Operational status of the theatre (ACTIVE, INACTIVE, CLOSED)
     */
    @NotNull(message = "Status cannot be null")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TheatreStatus status;

    /**
     * Timestamp when the theatre was created (immutable)
     */
    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;

    /**
     * Timestamp when the theatre was last updated
     */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * Automatically set createdAt timestamp before persisting
     */
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null) {
            status = TheatreStatus.ACTIVE;
        }
    }

    /**
     * Automatically update updatedAt timestamp before updating
     */
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * Custom equals method for Theatre comparison
     * Uses id as primary identifier
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Theatre theatre = (Theatre) o;
        return Objects.equals(id, theatre.id) && 
               Objects.equals(theatreId, theatre.theatreId);
    }

    /**
     * Custom hashCode method for Theatre
     */
    @Override
    public int hashCode() {
        return Objects.hash(id, theatreId);
    }

    /**
     * Custom toString method for better logging and debugging
     */
    @Override
    public String toString() {
        return "Theatre{" +
                "id=" + id +
                ", theatreId='" + theatreId + '\'' +
                ", name='" + name + '\'' +
                ", cityId=" + cityId +
                ", ownerId=" + ownerId +
                ", status=" + status +
                ", createdAt=" + createdAt +
                '}';
    }
}