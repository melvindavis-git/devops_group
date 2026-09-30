package org.example.pensionat_booking.Model;


import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    protected Long id;

    @NotBlank
    protected String nr;

    protected boolean isDoubleRoom;

    public Room(String nr, boolean isDoubleRoom) {
        this.nr = nr;
        this.isDoubleRoom = isDoubleRoom;
    }
}
