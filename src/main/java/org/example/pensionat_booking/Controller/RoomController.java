package org.example.pensionat_booking.Controller;

import org.example.pensionat_booking.DTO.RoomDTO;
import org.example.pensionat_booking.Service.RoomService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("api/rooms")
public class RoomController {

    private final RoomService service;
    private static final Logger log = LoggerFactory.getLogger(RoomController.class);


    public RoomController(RoomService service) {
        this.service = service;
    }

    @GetMapping
    public List<RoomDTO> getAllRooms() {
        log.info("GET request for all rooms");
        List<RoomDTO> roomsDTO = service.getAllRooms();
        log.info("Returned {} rooms", roomsDTO.size());
        return roomsDTO;
    }

}
