package com.example.officebooking.service;

import com.example.officebooking.entity.Booking;
import com.example.officebooking.entity.BookingStatus;
import com.example.officebooking.entity.Room;
import com.example.officebooking.exception.BookingConflictException;
import com.example.officebooking.exception.NotFoundException;
import com.example.officebooking.repository.BookingRepository;
import com.example.officebooking.repository.RoomRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final RoomRepository roomRepository;

    public BookingService(BookingRepository bookingRepository, RoomRepository roomRepository) {
        this.bookingRepository = bookingRepository;
        this.roomRepository = roomRepository;
    }

    @Transactional
    public Booking createBooking(Long roomId, String userName, LocalDateTime startAt, LocalDateTime endAt) {
        validateInterval(startAt, endAt);
        if (userName == null || userName.isBlank()) {
            throw new IllegalArgumentException("Имя пользователя обязательно");
        }

        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new NotFoundException("Комната не найдена: " + roomId));

        List<Booking> overlapping = bookingRepository.findOverlapping(roomId, BookingStatus.ACTIVE, startAt, endAt);
        if (!overlapping.isEmpty()) {
            throw new BookingConflictException(overlapping.get(0));
        }

        Booking booking = new Booking(room, userName, startAt, endAt, BookingStatus.ACTIVE);
        return bookingRepository.save(booking);
    }

    @Transactional
    public Booking cancelBooking(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Бронь не найдена: " + bookingId));
        booking.setStatus(BookingStatus.CANCELLED);
        return bookingRepository.save(booking);
    }

    @Transactional(readOnly = true)
    public List<LocalTime> getFreeSlots(Long roomId, LocalDate date) {
        roomRepository.findById(roomId)
                .orElseThrow(() -> new NotFoundException("Комната не найдена: " + roomId));

        LocalDateTime dayStart = date.atTime(BookingRules.WORKDAY_START);
        LocalDateTime dayEnd = date.atTime(BookingRules.WORKDAY_END);

        List<Booking> bookings = bookingRepository.findOverlapping(roomId, BookingStatus.ACTIVE, dayStart, dayEnd);

        List<LocalTime> freeSlots = new ArrayList<>();
        LocalDateTime slotStart = dayStart;
        while (slotStart.isBefore(dayEnd)) {
            LocalDateTime slotEnd = slotStart.plusMinutes(BookingRules.SLOT_MINUTES);
            if (!isSlotOccupied(bookings, slotStart, slotEnd)) {
                freeSlots.add(slotStart.toLocalTime());
            }
            slotStart = slotEnd;
        }
        return freeSlots;
    }

    @Transactional(readOnly = true)
    public List<Room> getFreeRooms(LocalDateTime startAt, LocalDateTime endAt) {
        return roomRepository.findFreeRooms(BookingStatus.ACTIVE, startAt, endAt);
    }

    private void validateInterval(LocalDateTime startAt, LocalDateTime endAt) {
        if (startAt == null || endAt == null) {
            throw new IllegalArgumentException("Время начала и конца брони обязательны");
        }
        if (!endAt.isAfter(startAt)) {
            throw new IllegalArgumentException("Конец брони должен быть позже начала");
        }
        if (!startAt.toLocalDate().equals(endAt.toLocalDate())) {
            throw new IllegalArgumentException("Бронь должна быть в пределах одного дня");
        }
        LocalTime startTime = startAt.toLocalTime();
        LocalTime endTime = endAt.toLocalTime();
        if (startTime.isBefore(BookingRules.WORKDAY_START) || endTime.isAfter(BookingRules.WORKDAY_END)) {
            throw new IllegalArgumentException("Бронь должна быть в пределах рабочего времени "
                    + BookingRules.WORKDAY_START + "–" + BookingRules.WORKDAY_END);
        }
        if (!isAlignedToSlot(startAt) || !isAlignedToSlot(endAt)) {
            throw new IllegalArgumentException("Бронь должна начинаться и заканчиваться в границах 15-минутных слотов");
        }
    }

    private boolean isAlignedToSlot(LocalDateTime dateTime) {
        return dateTime.getMinute() % BookingRules.SLOT_MINUTES == 0
                && dateTime.getSecond() == 0
                && dateTime.getNano() == 0;
    }

    private boolean isOverlapping(LocalDateTime start1, LocalDateTime end1,
                                  LocalDateTime start2, LocalDateTime end2) {
        return start1.isBefore(end2) && end1.isAfter(start2);
    }

    private boolean isSlotOccupied(List<Booking> bookings, LocalDateTime slotStart, LocalDateTime slotEnd) {
        return bookings.stream()
                .anyMatch(b -> isOverlapping(b.getStartAt(), b.getEndAt(), slotStart, slotEnd));
    }
}
