package com.example.officebooking.dto;

import java.time.LocalDateTime;

/**
 * Тело ответа 409: имя пользователя и интервал конфликтующей брони.
 */
public record ConflictResponse(String userName, LocalDateTime startAt, LocalDateTime endAt) {
}
