package com.example.officebooking.dto;

import com.example.officebooking.entity.Room;

public record RoomResponse(Long id, String name, Integer capacity) {

    public static RoomResponse from(Room room) {
        return new RoomResponse(room.getId(), room.getName(), room.getCapacity());
    }
}
