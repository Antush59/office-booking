package com.example.officebooking.repository;

import com.example.officebooking.entity.BookingStatus;
import com.example.officebooking.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface RoomRepository extends JpaRepository<Room, Long> {

    @Query("SELECT r FROM Room r WHERE NOT EXISTS (" +
            "SELECT b FROM Booking b WHERE b.room = r AND b.status = :status " +
            "AND b.startAt < :endAt AND b.endAt > :startAt)")
    List<Room> findFreeRooms(@Param("status") BookingStatus status,
                             @Param("startAt") LocalDateTime startAt,
                             @Param("endAt") LocalDateTime endAt);
}
