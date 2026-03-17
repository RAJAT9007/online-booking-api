package com.example.New_Project.DTO;


import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Data
public class PaymentDTO {
    private Long bookingId;
    private String transactionId;
    private Double amount;
    private String paymentMethod;
    private String status;
}