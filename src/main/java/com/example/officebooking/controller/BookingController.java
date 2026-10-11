package com.example.officebooking.controller;

import com.example.officebooking.dto.BookingResponse;
import com.example.officebooking.dto.CreateBookingRequest;
import com.example.officebooking.entity.Booking;
import com.example.officebooking.service.BookingService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/bookings")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping
    public ResponseEntity<BookingResponse> createBooking(@RequestBody CreateBookingRequest request) {
        Booking booking = bookingService.createBooking(
                request.roomId(), request.userName(), request.startAt(), request.endAt());
        return ResponseEntity.status(HttpStatus.CREATED).body(BookingResponse.from(booking));
    }

    @DeleteMapping("/{id}")
    public BookingResponse cancelBooking(@PathVariable Long id) {
        return BookingResponse.from(bookingService.cancelBooking(id));
    }
}
