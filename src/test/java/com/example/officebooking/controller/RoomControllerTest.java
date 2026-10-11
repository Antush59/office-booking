package com.example.officebooking.controller;

import com.example.officebooking.entity.Booking;
import com.example.officebooking.entity.BookingStatus;
import com.example.officebooking.entity.Room;
import com.example.officebooking.repository.BookingRepository;
import com.example.officebooking.repository.RoomRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import com.example.officebooking.BaseIntegrationTest;
import org.springframework.test.web.servlet.MockMvc;
import org.testng.annotations.Test;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
public class RoomControllerTest extends BaseIntegrationTest {

    private static final LocalDate DATE = LocalDate.of(2026, 10, 12);

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Test
    public void listRoomsReturnsAllRooms() throws Exception {
        roomRepository.saveAndFlush(new Room("Переговорка A", 4));
        roomRepository.saveAndFlush(new Room("Переговорка B", 8));

        mockMvc.perform(get("/rooms"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").exists())
                .andExpect(jsonPath("$[0].capacity").exists());
    }

    @Test
    public void getFreeSlotsReturnsExpectedNumberOfSlots() throws Exception {
        Room room = roomRepository.saveAndFlush(new Room("Переговорка A", 4));
        bookingRepository.saveAndFlush(new Booking(room, "Петя",
                DATE.atTime(10, 0), DATE.atTime(11, 0), BookingStatus.ACTIVE));

        mockMvc.perform(get("/rooms/{id}/free-slots", room.getId()).param("date", "2026-10-12"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(40));
    }

    @Test
    public void getFreeRoomsReturnsOnlyFreeRooms() throws Exception {
        Room r1 = roomRepository.saveAndFlush(new Room("Переговорка A", 4));
        Room r2 = roomRepository.saveAndFlush(new Room("Переговорка B", 4));
        bookingRepository.saveAndFlush(new Booking(r1, "Петя",
                DATE.atTime(10, 0), DATE.atTime(11, 0), BookingStatus.ACTIVE));

        mockMvc.perform(get("/rooms/free")
                        .param("date", "2026-10-12")
                        .param("start", "10:30")
                        .param("end", "10:45"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Переговорка B"));
    }
}
