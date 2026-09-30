package org.example.pensionat_booking.Controller;

import org.example.pensionat_booking.DTO.BookingDTO;
import org.example.pensionat_booking.DTO.BookingResponseDTO;
import org.example.pensionat_booking.Exception.RoomNotAvailableException;
import org.example.pensionat_booking.Service.BookingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.QueryTimeoutException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("api/bookings")
public class BookingController {

    private final BookingService bookingService;
    private static final Logger log = LoggerFactory.getLogger(BookingController.class);


    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @GetMapping()
    public ResponseEntity<List<BookingResponseDTO>> getAllBookings() {
        try {
            return ResponseEntity.ok(bookingService.getAllBookings());
        } catch (HttpClientErrorException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Något gick fel.");
        } catch (QueryTimeoutException e) {
            return ResponseEntity.status(HttpStatus.GATEWAY_TIMEOUT).build();
        } catch (RuntimeException e) {
            log.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/available-rooms")
    public ResponseEntity<?> canBook(@RequestParam String startDate, @RequestParam String endDate, @RequestParam boolean doubleRoom) {
        try {
            return ResponseEntity.ok(bookingService.canBook(startDate, endDate, doubleRoom));
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode()).body(e.getReason());
        } catch (QueryTimeoutException e) {
            return ResponseEntity.status(HttpStatus.GATEWAY_TIMEOUT).build();
        }
    }

    @DeleteMapping("/{bookingID}")
    public ResponseEntity<BookingDTO> removeBooking(@PathVariable Long bookingID) {
        log.info("DELETE request to delete booking");
        try {
            bookingService.removeBooking(bookingID);
            return ResponseEntity.ok().build();
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode()).build();
        } catch (DataIntegrityViolationException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        } catch (QueryTimeoutException e) {
            return ResponseEntity.status(HttpStatus.GATEWAY_TIMEOUT).build();
        }
    }

    @PostMapping()
    public ResponseEntity<BookingDTO> bookRoom(@RequestParam String startDate, @RequestParam String endDate,
                                               @RequestParam boolean isDoubleRoom, @RequestParam Long customerId,
                                               @RequestParam(defaultValue = "0") int extraBeds) {
        try {
            BookingDTO bookingDTO = bookingService.createBooking(startDate, endDate, isDoubleRoom, customerId, extraBeds);
            return ResponseEntity.status(HttpStatus.CREATED).body(bookingDTO);
        } catch (DataIntegrityViolationException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        } catch (QueryTimeoutException e) {
            return ResponseEntity.status(HttpStatus.GATEWAY_TIMEOUT).build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> editBooking(@PathVariable Long id, @RequestParam String
            startDate, @RequestParam String endDate) {
        try {
            return ResponseEntity.ok(bookingService.editBooking(id, startDate, endDate));
        } catch (RoomNotAvailableException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        } catch (DataIntegrityViolationException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("En konflikt har inträffat.");
        }
    }
}


