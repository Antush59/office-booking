package com.example.officebooking.repository;

import com.example.officebooking.entity.Booking;
import com.example.officebooking.entity.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    @Query("SELECT b FROM Booking b " +
            "WHERE b.room.id = :roomId " +
            "AND b.status = :status " +
            "AND b.startAt < :endAt " +
            "AND b.endAt > :startAt")
    List<Booking> findOverlapping(@Param("roomId") Long roomId,
                                  @Param("status") BookingStatus status,
                                  @Param("startAt") LocalDateTime startAt,
                                  @Param("endAt") LocalDateTime endAt);
}
