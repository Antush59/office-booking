package com.example.officebooking.dto;

import com.example.officebooking.entity.Booking;
import com.example.officebooking.entity.BookingStatus;

import java.time.LocalDateTime;

public record BookingResponse(Long id, Long roomId, String userName,
                              LocalDateTime startAt, LocalDateTime endAt,
                              BookingStatus status) {

    public static BookingResponse from(Booking booking) {
        return new BookingResponse(
                booking.getId(),
                booking.getRoom().getId(),
                booking.getUserName(),
                booking.getStartAt(),
                booking.getEndAt(),
                booking.getStatus());
    }
}
