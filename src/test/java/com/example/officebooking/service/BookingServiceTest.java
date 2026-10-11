package com.example.officebooking.service;

import com.example.officebooking.entity.Booking;
import com.example.officebooking.entity.BookingStatus;
import com.example.officebooking.entity.Room;
import com.example.officebooking.exception.BookingConflictException;
import com.example.officebooking.repository.BookingRepository;
import com.example.officebooking.repository.RoomRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import com.example.officebooking.BaseIntegrationTest;
import org.testng.annotations.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.expectThrows;

@SpringBootTest
@ActiveProfiles("test")
public class BookingServiceTest extends BaseIntegrationTest {

    private static final LocalDate DATE = LocalDate.of(2026, 10, 12);

    @Autowired
    private BookingService bookingService;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private RoomRepository roomRepository;

    private Room saveRoom(String name) {
        return roomRepository.saveAndFlush(new Room(name, 4));
    }

    @Test
    public void createsBookingForValidInterval() {
        Room room = saveRoom("Переговорка 1");
        Booking booking = bookingService.createBooking(room.getId(), "Антон",
                DATE.atTime(10, 0), DATE.atTime(11, 0));

        assertNotNull(booking.getId(), "ID должен быть присвоен");
        assertEquals(booking.getStatus(), BookingStatus.ACTIVE);
        assertEquals(booking.getUserName(), "Антон");
    }

    @Test
    public void rejectsIntervalWhereEndNotAfterStart() {
        Room room = saveRoom("Переговорка 1");
        expectThrows(IllegalArgumentException.class, () ->
                bookingService.createBooking(room.getId(), "Антон", DATE.atTime(11, 0), DATE.atTime(10, 0)));
    }

    @Test
    public void rejectsIntervalOutsideWorkingHours() {
        Room room = saveRoom("Переговорка 1");
        expectThrows(IllegalArgumentException.class, () ->
                bookingService.createBooking(room.getId(), "Антон", DATE.atTime(7, 0), DATE.atTime(8, 0)));
        expectThrows(IllegalArgumentException.class, () ->
                bookingService.createBooking(room.getId(), "Антон", DATE.atTime(18, 0), DATE.atTime(20, 0)));
    }

    @Test
    public void rejectsIntervalNotAlignedTo15Minutes() {
        Room room = saveRoom("Переговорка 1");
        expectThrows(IllegalArgumentException.class, () ->
                bookingService.createBooking(room.getId(), "Антон", DATE.atTime(10, 7), DATE.atTime(11, 0)));
    }

    @Test
    public void rejectsIntervalSpanningTwoDays() {
        Room room = saveRoom("Переговорка 1");
        expectThrows(IllegalArgumentException.class, () ->
                bookingService.createBooking(room.getId(), "Антон",
                        DATE.atTime(18, 0), DATE.plusDays(1).atTime(9, 0)));
    }

    @Test
    public void throwsConflictWhenOverlapping() {
        Room room = saveRoom("Переговорка 1");
        bookingRepository.saveAndFlush(new Booking(room, "Петя",
                DATE.atTime(10, 0), DATE.atTime(11, 0), BookingStatus.ACTIVE));

        BookingConflictException ex = expectThrows(BookingConflictException.class, () ->
                bookingService.createBooking(room.getId(), "Антон", DATE.atTime(10, 30), DATE.atTime(11, 30)));

        assertEquals(ex.getConflictingBooking().getUserName(), "Петя");
        assertEquals(ex.getConflictingBooking().getStartAt(), DATE.atTime(10, 0));
        assertEquals(ex.getConflictingBooking().getEndAt(), DATE.atTime(11, 0));
    }

    @Test
    public void cancelsBookingByMarkingCancelled() {
        Room room = saveRoom("Переговорка 1");
        Booking booking = bookingRepository.saveAndFlush(new Booking(room, "Антон",
                DATE.atTime(10, 0), DATE.atTime(11, 0), BookingStatus.ACTIVE));

        Booking cancelled = bookingService.cancelBooking(booking.getId());
        assertEquals(cancelled.getStatus(), BookingStatus.CANCELLED);
    }

    @Test
    public void returnsAllSlotsWhenDayIsEmpty() {
        Room room = saveRoom("Переговорка 1");
        List<LocalTime> slots = bookingService.getFreeSlots(room.getId(), DATE);

        assertEquals(slots.size(), 44);
        assertEquals(slots.get(0), LocalTime.of(8, 0));
        assertEquals(slots.get(slots.size() - 1), LocalTime.of(18, 45));
    }

    @Test
    public void returnsNoSlotsWhenDayIsFullyBooked() {
        Room room = saveRoom("Переговорка 1");
        bookingRepository.saveAndFlush(new Booking(room, "Антон",
                DATE.atTime(8, 0), DATE.atTime(19, 0), BookingStatus.ACTIVE));

        assertTrue(bookingService.getFreeSlots(room.getId(), DATE).isEmpty());
    }

    @Test
    public void returnsOnlyFreeSlotsWhenPartiallyBooked() {
        Room room = saveRoom("Переговорка 1");
        bookingRepository.saveAndFlush(new Booking(room, "Антон",
                DATE.atTime(10, 0), DATE.atTime(11, 0), BookingStatus.ACTIVE));

        List<LocalTime> slots = bookingService.getFreeSlots(room.getId(), DATE);

        assertEquals(slots.size(), 40);
        assertFalse(slots.contains(LocalTime.of(10, 0)), "Занятый слот не должен быть свободным");
        assertFalse(slots.contains(LocalTime.of(10, 45)), "Занятый слот не должен быть свободным");
        assertTrue(slots.contains(LocalTime.of(9, 45)));
        assertTrue(slots.contains(LocalTime.of(11, 0)));
    }

    @Test
    public void returnsOnlyFreeRooms() {
        Room room1 = saveRoom("Переговорка 1");
        Room room2 = saveRoom("Переговорка 2");
        bookingRepository.saveAndFlush(new Booking(room1, "Антон",
                DATE.atTime(10, 0), DATE.atTime(11, 0), BookingStatus.ACTIVE));

        List<Room> freeRooms = bookingService.getFreeRooms(DATE.atTime(10, 30), DATE.atTime(10, 45));

        assertEquals(freeRooms.size(), 1);
        assertEquals(freeRooms.get(0).getId(), room2.getId());
    }
}
