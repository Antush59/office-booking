package com.example.officebooking.service;

import com.example.officebooking.entity.Room;
import com.example.officebooking.repository.RoomRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import com.example.officebooking.BaseIntegrationTest;
import org.testng.annotations.Test;

import java.util.List;

import static org.testng.Assert.assertEquals;

@SpringBootTest
@ActiveProfiles("test")
public class RoomServiceTest extends BaseIntegrationTest {

    @Autowired
    private RoomService roomService;

    @Autowired
    private RoomRepository roomRepository;

    @Test
    public void listRoomsReturnsAllRooms() {
        roomRepository.saveAndFlush(new Room("Переговорка A", 4));
        roomRepository.saveAndFlush(new Room("Переговорка B", 8));

        List<Room> rooms = roomService.listRooms();
        rooms.forEach(System.out::println);
        assertEquals(rooms.size(), 2);
    }
}
