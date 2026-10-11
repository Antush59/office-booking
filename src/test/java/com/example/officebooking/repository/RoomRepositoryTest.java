package com.example.officebooking.repository;

import com.example.officebooking.entity.Room;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import com.example.officebooking.BaseIntegrationTest;
import org.testng.annotations.Test;

import java.util.Optional;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
public class RoomRepositoryTest extends BaseIntegrationTest {

    @Autowired
    private RoomRepository roomRepository;

    @Test
    public void persistsAndFindsRoom() {
        Room room = new Room("Переговорка 1", 6);
        Room saved = roomRepository.saveAndFlush(room);

        assertNotNull(saved.getId(), "ID должен быть присвоен после сохранения");

        Optional<Room> found = roomRepository.findById(saved.getId());
        assertTrue(found.isPresent(), "Комната должна находиться по ID");
        assertEquals(found.get().getName(), "Переговорка 1");
        assertEquals(found.get().getCapacity(), Integer.valueOf(6));
    }
}
