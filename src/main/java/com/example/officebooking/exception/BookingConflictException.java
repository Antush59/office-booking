package com.example.officebooking.exception;

import com.example.officebooking.entity.Booking;

/**
 * Бросается при попытке создать бронь, пересекающуюся с существующей.
 * Несёт конфликтующую бронь, чтобы REST-слой мог вернуть её имя и интервал.
 */
public class BookingConflictException extends RuntimeException {

    private final Booking conflictingBooking;

    public BookingConflictException(Booking conflictingBooking) {
        super("Бронь пересекается с существующей бронью");
        this.conflictingBooking = conflictingBooking;
    }

    public Booking getConflictingBooking() {
        return conflictingBooking;
    }
}
