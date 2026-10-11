package com.example.officebooking.dto;

import java.time.LocalDateTime;

/**
 * Тело запроса POST /bookings.
 */
public record CreateBookingRequest(Long roomId, String userName, LocalDateTime startAt, LocalDateTime endAt) {
}
