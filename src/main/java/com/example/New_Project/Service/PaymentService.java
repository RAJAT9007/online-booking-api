package com.example.New_Project.Service;

import com.example.New_Project.DTO.PaymentDTO;
import com.example.New_Project.Entity.Booking;
import com.example.New_Project.Entity.Payment;
import com.example.New_Project.Repository.BookingRepository;
import com.example.New_Project.Repository.PaymentRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.example.New_Project.enums.BookingStatus.CONFIRMED;

@Service
public class PaymentService {

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Transactional
    public Payment processPayment(PaymentDTO dto) {
        // 1. Create Payment Record
        Payment payment = new Payment();
        payment.setBookingId(dto.getBookingId());
        payment.setTransactionId(dto.getTransactionId());
        payment.setAmount(dto.getAmount());
        payment.setPaymentMethod(dto.getPaymentMethod());
        payment.setStatus(dto.getStatus());

        Payment savedPayment = paymentRepository.save(payment);

        // 2. Update Booking Status if payment is SUCCESS
        if ("SUCCESS".equalsIgnoreCase(dto.getStatus())) {
            Booking booking = bookingRepository.findById(dto.getBookingId())
                    .orElseThrow(() -> new RuntimeException("Booking not found"));
            booking.setStatus(CONFIRMED);
            bookingRepository.save(booking);
        }

        return savedPayment;
    }
}