package com.example.New_Project.Exception;

import java.util.List;

public class SeatsAlreadyBookedException extends RuntimeException {

    public SeatsAlreadyBookedException(List<Long> conflictingSeatIds) {
        super("The following seats are already booked: " + conflictingSeatIds);
    }
}
