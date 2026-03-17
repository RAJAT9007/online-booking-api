package com.example.New_Project.Entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Data
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; //

    @Column(name = "booking_id", nullable = false)
    private Long bookingId;

    @Column(name = "transaction_id", unique = true)
    private String transactionId;
    private Double amount;
    private String paymentMethod;
    private String status;
    private LocalDateTime paymentDate = LocalDateTime.now();
}