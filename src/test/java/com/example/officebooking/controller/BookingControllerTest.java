package com.example.officebooking.controller;

import com.example.officebooking.dto.CreateBookingRequest;
import com.example.officebooking.entity.Booking;
import com.example.officebooking.entity.BookingStatus;
import com.example.officebooking.entity.Room;
import com.example.officebooking.repository.BookingRepository;
import com.example.officebooking.repository.RoomRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.http.MediaType;
import com.example.officebooking.BaseIntegrationTest;
import org.springframework.test.web.servlet.MockMvc;
import org.testng.annotations.Test;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
public class BookingControllerTest extends BaseIntegrationTest {

    private static final LocalDate DATE = LocalDate.of(2026, 10, 12);

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Test
    public void createBookingReturns201() throws Exception {
        Room room = roomRepository.saveAndFlush(new Room("Переговорка A", 4));
        String body = objectMapper.writeValueAsString(new CreateBookingRequest(
                room.getId(), "Антон", DATE.atTime(10, 0), DATE.atTime(11, 0)));

        mockMvc.perform(post("/bookings").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.userName").value("Антон"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    public void createBookingInvalidIntervalReturns400() throws Exception {
        Room room = roomRepository.saveAndFlush(new Room("Переговорка A", 4));
        String body = objectMapper.writeValueAsString(new CreateBookingRequest(
                room.getId(), "Антон", DATE.atTime(11, 0), DATE.atTime(10, 0)));

        mockMvc.perform(post("/bookings").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void createBookingConflictReturns409() throws Exception {
        Room room = roomRepository.saveAndFlush(new Room("Переговорка A", 4));
        bookingRepository.saveAndFlush(new Booking(room, "Петя",
                DATE.atTime(10, 0), DATE.atTime(11, 0), BookingStatus.ACTIVE));

        String body = objectMapper.writeValueAsString(new CreateBookingRequest(
                room.getId(), "Антон", DATE.atTime(10, 30), DATE.atTime(11, 30)));

        mockMvc.perform(post("/bookings").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.userName").value("Петя"))
                .andExpect(jsonPath("$.startAt").exists())
                .andExpect(jsonPath("$.endAt").exists());
    }

    @Test
    public void cancelBookingMarksCancelled() throws Exception {
        Room room = roomRepository.saveAndFlush(new Room("Переговорка A", 4));
        Booking booking = bookingRepository.saveAndFlush(new Booking(room, "Антон",
                DATE.atTime(10, 0), DATE.atTime(11, 0), BookingStatus.ACTIVE));

        mockMvc.perform(delete("/bookings/{id}", booking.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));
    }
}
