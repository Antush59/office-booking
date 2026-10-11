package com.example.officebooking.repository;

import com.example.officebooking.entity.Booking;
import com.example.officebooking.entity.BookingStatus;
import com.example.officebooking.entity.Room;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import com.example.officebooking.BaseIntegrationTest;
import org.testng.annotations.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
public class BookingRepositoryTest extends BaseIntegrationTest {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private RoomRepository roomRepository;

    @Test
    public void persistsAndFindsBooking() {
        Room room = roomRepository.saveAndFlush(new Room("Переговорка 1", 6));
        Booking booking = new Booking(room, "Антон",
                LocalDateTime.of(2026, 10, 10, 10, 0),
                LocalDateTime.of(2026, 10, 10, 11, 0),
                BookingStatus.ACTIVE);
        Booking saved = bookingRepository.saveAndFlush(booking);

        assertNotNull(saved.getId(), "ID должен быть присвоен после сохранения");

        Optional<Booking> found = bookingRepository.findById(saved.getId());
        assertTrue(found.isPresent(), "Бронь должна находиться по ID");
        assertEquals(found.get().getStatus(), BookingStatus.ACTIVE);
    }

    @Test
    public void findsOnlyOverlappingActiveBookings() {
        Room room = roomRepository.saveAndFlush(new Room("Переговорка 2", 4));

        bookingRepository.saveAndFlush(new Booking(room, "Антон",
                LocalDateTime.of(2026, 10, 10, 10, 0),
                LocalDateTime.of(2026, 10, 10, 11, 0),
                BookingStatus.ACTIVE));

        bookingRepository.saveAndFlush(new Booking(room, "Петя",
                LocalDateTime.of(2026, 10, 10, 11, 0),
                LocalDateTime.of(2026, 10, 10, 12, 0),
                BookingStatus.ACTIVE));

        bookingRepository.saveAndFlush(new Booking(room, "Маша",
                LocalDateTime.of(2026, 10, 10, 10, 30),
                LocalDateTime.of(2026, 10, 10, 10, 45),
                BookingStatus.CANCELLED));

        // Пересечение 10:30–10:45 -> только "Антон" (отменённая "Маша" не учитывается)
        List<Booking> overlapping = bookingRepository.findOverlapping(
                room.getId(), BookingStatus.ACTIVE,
                LocalDateTime.of(2026, 10, 10, 10, 30),
                LocalDateTime.of(2026, 10, 10, 10, 45));
        assertEquals(overlapping.size(), 1);
        assertEquals(overlapping.get(0).getUserName(), "Антон");

        // Граница 11:00–11:30 -> только "Петя" (бронь Антона заканчивается ровно в 11:00 — полуоткрытый интервал)
        List<Booking> boundary = bookingRepository.findOverlapping(
                room.getId(), BookingStatus.ACTIVE,
                LocalDateTime.of(2026, 10, 10, 11, 0),
                LocalDateTime.of(2026, 10, 10, 11, 30));
        assertEquals(boundary.size(), 1);
        assertEquals(boundary.get(0).getUserName(), "Петя");

        // Нет пересечений 12:00–12:30
        List<Booking> none = bookingRepository.findOverlapping(
                room.getId(), BookingStatus.ACTIVE,
                LocalDateTime.of(2026, 10, 10, 12, 0),
                LocalDateTime.of(2026, 10, 10, 12, 30));
        assertTrue(none.isEmpty(), "Не должно быть пересекающихся броней");
    }
}
