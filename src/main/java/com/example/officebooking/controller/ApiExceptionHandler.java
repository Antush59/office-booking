package com.example.officebooking.controller;

import com.example.officebooking.dto.ConflictResponse;
import com.example.officebooking.dto.ErrorResponse;
import com.example.officebooking.entity.Booking;
import com.example.officebooking.exception.BookingConflictException;
import com.example.officebooking.exception.NotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(BookingConflictException.class)
    public ResponseEntity<ConflictResponse> handleConflict(BookingConflictException ex) {
        Booking booking = ex.getConflictingBooking();
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ConflictResponse(booking.getUserName(), booking.getStartAt(), booking.getEndAt()));
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(NotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse(ex.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleBadRequest(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponse(ex.getMessage()));
    }
}
