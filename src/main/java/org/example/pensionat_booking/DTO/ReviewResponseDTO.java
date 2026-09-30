package org.example.pensionat_booking.DTO;

import java.time.LocalDate;

public record ReviewResponseDTO(
        Long roomId,
        String reviewContent,
        int stars,
        String name,
        LocalDate createdAt
) {}
