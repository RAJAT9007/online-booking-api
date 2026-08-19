package com.example.New_Project.Service;

import com.example.New_Project.DTO.PaymentDTO;
import com.example.New_Project.Entity.Booking;
import com.example.New_Project.Entity.Payment;
import com.example.New_Project.Repository.BookingRepository;
import com.example.New_Project.Repository.PaymentRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Optional;

import static com.example.New_Project.enums.BookingStatus.CONFIRMED;

@Service
public class PaymentService {

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Value("${stripe.secret.key}")
    private String stripeSecretKey;

    @Value("${frontend.url}")
    private String frontendUrl;

    @Transactional
    public Payment processPayment(PaymentDTO dto) {

        Optional<Payment> paymentOptional = paymentRepository.findByTransactionId(dto.getTransactionId());
        Payment payment;
        if(paymentOptional.isPresent()) {
            payment = paymentOptional.get();
        } else {
            payment = new Payment();
        }
        // 1. Create Payment Record
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

    public String createCheckoutSession(Long bookingId, Double amount, Long showId, Long theatreId) throws Exception {
        com.stripe.Stripe.apiKey = stripeSecretKey;

        long finalAmount = (long) (amount * 100);

        com.stripe.param.checkout.SessionCreateParams params = com.stripe.param.checkout.SessionCreateParams.builder()
                .setMode(com.stripe.param.checkout.SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl(this.frontendUrl + "/receipt/" + bookingId + "?session_id={CHECKOUT_SESSION_ID}&theatreId=" + theatreId)
                .setCancelUrl(this.frontendUrl +"/payment?showId=" + showId + "&theatreId=" + theatreId)
                .addLineItem(com.stripe.param.checkout.SessionCreateParams.LineItem.builder()
                        .setQuantity(1L)
                        .setPriceData(com.stripe.param.checkout.SessionCreateParams.LineItem.PriceData.builder()
                                .setCurrency("inr")
                                .setUnitAmount(finalAmount)
                                .setProductData(
                                        com.stripe.param.checkout.SessionCreateParams.LineItem.PriceData.ProductData
                                                .builder()
                                                .setName("Movie Tickets - Booking #" + bookingId)
                                                .build())
                                .build())
                        .build())
                .build();

        com.stripe.model.checkout.Session session = com.stripe.model.checkout.Session.create(params);
        return session.getUrl();
    }

    @Transactional
    public boolean verifySession(String sessionId, Long bookingId) throws Exception {
        com.stripe.Stripe.apiKey = stripeSecretKey;
        com.stripe.model.checkout.Session session = com.stripe.model.checkout.Session.retrieve(sessionId);

        if ("paid".equals(session.getPaymentStatus())) {
            PaymentDTO dto = new PaymentDTO();
            dto.setBookingId(bookingId);
            dto.setTransactionId(sessionId);
            dto.setAmount(session.getAmountTotal() != null ? session.getAmountTotal() / 100.0 : 0.0);
            dto.setPaymentMethod("STRIPE");
            dto.setStatus("SUCCESS");
            processPayment(dto);
            return true;
        } else if ("unpaid".equals(session.getPaymentStatus())) {
            PaymentDTO dto = new PaymentDTO();
            dto.setBookingId(bookingId);
            dto.setTransactionId(sessionId);
            dto.setAmount(session.getAmountTotal() != null ? session.getAmountTotal() / 100.0 : 0.0);
            dto.setPaymentMethod("STRIPE");
            dto.setStatus("FAILED");
            processPayment(dto);
        }
        return false;
    }
}