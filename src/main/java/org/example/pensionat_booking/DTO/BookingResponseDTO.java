package org.example.pensionat_booking.DTO;

import org.example.pensionat_booking.Model.Room;

import java.time.LocalDate;

public record BookingResponseDTO(
    Long id,
    Room room,
    LocalDate startDate,
    LocalDate endDate,
    boolean isDoubleRoom,
    Long customerId,
    String customerName,
    int extraBeds
){}
