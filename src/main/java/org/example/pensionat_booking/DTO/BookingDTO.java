package org.example.pensionat_booking.DTO;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.pensionat_booking.Model.Room;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BookingDTO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Room room;
    private Long customerId;
    private String startDate;
    private String endDate;
    private int extraBeds;
    private String customerName;
}