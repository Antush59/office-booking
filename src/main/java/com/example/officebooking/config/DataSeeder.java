package com.example.officebooking.config;

import com.example.officebooking.entity.Room;
import com.example.officebooking.repository.RoomRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Наполняет БД несколькими комнатами при старте приложения.
 * Не выполняется в тестах (профиль "test"), чтобы не мешать их изоляции.
 */
@Component
@Profile("!test")
public class DataSeeder implements CommandLineRunner {

    private final RoomRepository roomRepository;

    public DataSeeder(RoomRepository roomRepository) {
        this.roomRepository = roomRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (roomRepository.count() == 0) {
            roomRepository.save(new Room("Переговорка 1", 6));
            roomRepository.save(new Room("Переговорка 2", 4));
            roomRepository.save(new Room("Переговорка 3", 10));
        }
    }
}
