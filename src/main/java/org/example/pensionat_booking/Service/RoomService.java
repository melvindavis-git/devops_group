package org.example.pensionat_booking.Service;

import org.example.pensionat_booking.DTO.RoomDTO;
import org.example.pensionat_booking.Model.Room;
import org.example.pensionat_booking.Repository.RoomRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RoomService {

    private final RoomRepository repo;

    public RoomService(RoomRepository repo) {
        this.repo = repo;
    }

    public List<RoomDTO> getAllRooms() {
        return repo.findAll().stream().map(r -> RoomToRoomDTO(r)).toList();
    }

    public RoomDTO RoomToRoomDTO(Room r) {
        return RoomDTO.builder().id(r.getId()).nr(r.getNr()).isDoubleRoom(r.isDoubleRoom()).build();
    }

}
