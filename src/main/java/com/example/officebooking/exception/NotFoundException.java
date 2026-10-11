package com.example.officebooking.exception;

/**
 * Бросается, когда запрошенный ресурс (комната или бронь) не найден.
 */
public class NotFoundException extends RuntimeException {

    public NotFoundException(String message) {
        super(message);
    }
}
