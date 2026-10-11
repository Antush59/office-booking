package com.example.officebooking.controller;

import com.example.officebooking.dto.RoomResponse;
import com.example.officebooking.service.BookingService;
import com.example.officebooking.service.RoomService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/rooms")
public class RoomController {

    private final RoomService roomService;
    private final BookingService bookingService;

    public RoomController(RoomService roomService, BookingService bookingService) {
        this.roomService = roomService;
        this.bookingService = bookingService;
    }

    @GetMapping
    public List<RoomResponse> listRooms() {
        return roomService.listRooms().stream()
                .map(RoomResponse::from)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}/free-slots")
    public List<LocalTime> getFreeSlots(@PathVariable Long id,
                                        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return bookingService.getFreeSlots(id, date);
    }

    @GetMapping("/free")
    public List<RoomResponse> getFreeRooms(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                                           @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime start,
                                           @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime end) {
        LocalDateTime startAt = date.atTime(start);
        LocalDateTime endAt = date.atTime(end);
        return bookingService.getFreeRooms(startAt, endAt).stream()
                .map(RoomResponse::from)
                .collect(Collectors.toList());
    }
}
