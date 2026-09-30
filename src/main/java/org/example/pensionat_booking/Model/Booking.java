package org.example.pensionat_booking.Model;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Builder
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    protected Long id;

    @ManyToOne
    @JoinColumn(name = "room_id")
    protected Room room;


    @JoinColumn(name = "customer_id")
    protected Long customerId;


    protected LocalDate startDate;


    protected LocalDate endDate;

    protected int extraBeds;

    public Booking(Room room, Long customerId, LocalDate startDate, LocalDate endDate) {
        this.room = room;
        this.customerId = customerId;
        this.startDate = startDate;
        this.endDate = endDate;
        this.extraBeds = 0;
    }


}
