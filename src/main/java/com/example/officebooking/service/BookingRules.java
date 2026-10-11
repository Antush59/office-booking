package com.example.officebooking.service;

import java.time.LocalTime;

/**
 * Константы бизнес-правил бронирования: рабочий день и шаг слота.
 */
public final class BookingRules {

    public static final LocalTime WORKDAY_START = LocalTime.of(8, 0);
    public static final LocalTime WORKDAY_END = LocalTime.of(19, 0);
    public static final int SLOT_MINUTES = 15;

    private BookingRules() {
    }
}
