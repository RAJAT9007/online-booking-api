package com.example.New_Project.Exception;

public class BookingAlreadyCancelledException extends RuntimeException {

    public BookingAlreadyCancelledException(Long bookingId) {
        super("Booking ID " + bookingId + " is already cancelled.");
    }
}
