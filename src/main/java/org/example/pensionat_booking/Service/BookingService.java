package org.example.pensionat_booking.Service;

import org.example.pensionat_booking.DTO.BookingDTO;
import org.example.pensionat_booking.DTO.BookingResponseDTO;
import org.example.pensionat_booking.DTO.CustomerDTO;
import org.example.pensionat_booking.DTO.RoomDTO;
import org.example.pensionat_booking.Exception.RoomNotAvailableException;
import org.example.pensionat_booking.Model.Booking;
import org.example.pensionat_booking.Model.Room;
import org.example.pensionat_booking.Repository.BookingRepository;
import org.example.pensionat_booking.Repository.RoomRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class BookingService {

    private final BookingRepository bookingRepo;
    private final RoomRepository roomRepo;
    private final CustomerServiceClient customerServiceClient;
    private final Logger log = LoggerFactory.getLogger(BookingService.class);


    public BookingService(BookingRepository bookingRepo, RoomRepository roomRepo, CustomerServiceClient customerServiceClient) {
        this.bookingRepo = bookingRepo;
        this.roomRepo = roomRepo;
        this.customerServiceClient = customerServiceClient;
    }

    public List<BookingResponseDTO> getAllBookings() {

        Map<Long, String> customerName;
        List<Booking> bookings = bookingRepo.findAll();
        List<Long> customerIds = bookings.stream().map(b -> b.getCustomerId()).distinct().toList();


        customerName = customerIds.stream().collect(Collectors.toMap(id -> id, id -> {
            try {
                return customerServiceClient.getCustomerNameById(id);
            } catch (RuntimeException e) {
                log.warn("Could not find customer with id: {}. {}", id, e.getMessage());
                return "Ej tillgänligt";
            }
        }));

        log.info("Returned {} bookings for {} customers.", bookings.size(), customerIds.size());

        return bookings.stream().
                map(b -> new BookingResponseDTO(
                        b.getId(),
                        b.getRoom(),
                        b.getStartDate(),
                        b.getEndDate(),
                        b.getRoom().isDoubleRoom(),
                        b.getCustomerId(),
                        customerName.get(b.getCustomerId()),
                        b.getExtraBeds())).toList();
    }

    public BookingDTO BookingToBookingDTO(Booking b) {
        return BookingDTO.builder()
                .id(b.getId())
                .room(new Room(
                        b.getRoom().getId(),
                        b.getRoom().getNr(),
                        b.getRoom().isDoubleRoom()))
                .customerId(b.getCustomerId())
                .startDate(b.getStartDate().toString())
                .endDate(b.getEndDate().toString())
                .extraBeds(b.getExtraBeds())
                .build();
    }

    public List<RoomDTO> canBook(String startDate, String endDate, boolean doubleRoom) {


        List<String> invalidInputs = new ArrayList<>();

        if (!canParseDate(startDate)) {
            log.warn("Invalid start date");
            invalidInputs.add("Inget angivet startdatum.");
        }

        if (!canParseDate(endDate)) {
            log.warn("Invalid end date");
            invalidInputs.add("Inget angivet slutdatum.");
        }

        LocalDate requestedStartDate = LocalDate.parse(startDate);
        LocalDate requestedEndDate = LocalDate.parse(endDate);

        if (requestedEndDate.isBefore(requestedStartDate)) {
            log.warn("End date is before start date.");
            invalidInputs.add("Slutdatum kan inte vara innan startdatum.");
        }

        if (!invalidInputs.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, invalidInputs.toString());
        }

        List<Booking> bookings = bookingRepo.findAll();
        List<Room> validRooms = roomRepo.findAll();

        for (Booking booking : bookings) {
            boolean noConflict = booking.getEndDate().isBefore(requestedStartDate) || booking.getStartDate().isAfter(requestedEndDate);

            if (noConflict) {

            } else {
                validRooms.remove(booking.getRoom());
            }
        }

        validRooms.removeIf(room -> room.isDoubleRoom() != doubleRoom);

        log.info("Returned all valid rooms fitting requirements: {}", validRooms.size());
        return validRooms.stream().map(room -> RoomDTO.builder().id(room.getId()).nr(room.getNr()).isDoubleRoom(room.isDoubleRoom()).build()).toList();
    }

    public BookingDTO createBooking(String startDate, String endDate, boolean isDoubleRoom, Long customerId,
                                    int extraBeds) {

        if (extraBeds > 2) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Max 2 extrasängar.");

        CustomerDTO currentCustomer = null;
        try {
            currentCustomer = customerServiceClient.getCustomerById(customerId).getBody();
        } catch (RuntimeException e) {
            log.error("Could not find customer with id: {}", customerId);
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Kundtjänst ej tillgänglig");
        }

        if (!canParseDate(startDate) && !canParseDate(endDate)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Måste ange både slut och startdatum.");
        }

        List<RoomDTO> availableRooms = canBook(startDate, endDate, isDoubleRoom);
        LocalDate requestedStartDate = LocalDate.parse(startDate);
        LocalDate requestedEndDate = LocalDate.parse(endDate);

        if (availableRooms == null) {
            log.warn("Invalid date / dates");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Felaktig datum input.");
        }

        if (availableRooms.isEmpty()) {
            log.warn("No available matching requirements.");
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Inga rum tillgängliga.");
        }

        Room room = roomRepo.findById(availableRooms.getFirst().getId()).orElse(null);

        Booking currentBooking = new Booking(room, currentCustomer.getId(), requestedStartDate, requestedEndDate);
        currentBooking.setExtraBeds(extraBeds);
        bookingRepo.save(currentBooking);
        log.info("Customer {} created a booking with id: {}", currentBooking.getCustomerId(), currentBooking.getId());
        return BookingToBookingDTO(currentBooking);
    }

    public BookingDTO editBooking(Long bookingID, String startDate, String endDate) {

        if (!canParseDate(startDate) && !canParseDate(endDate)) {
            log.warn("Wrong date syntax.");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Felaktig datum syntax.");
        }

        LocalDate requestedStartDate = LocalDate.parse(startDate);
        LocalDate requestedEndDate = LocalDate.parse(endDate);

        if (requestedEndDate.isBefore(requestedStartDate)) {
            log.warn("End date before start date");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Slutdatum kan inte vara innan startdatum.");
        }

        boolean available = true;

        if (!bookingRepo.findById(bookingID).isPresent()) {
            log.warn("Could not find booking with id: {}", bookingID);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ingen bokning hittades.");
        }

        List<Booking> bookings = bookingRepo.findAll();

        Booking currentBooking = bookingRepo.findAll().stream().filter(booking -> Objects.equals(booking.getId(), bookingID)).findAny().orElse(null);

        bookings.remove(currentBooking);

        for (Booking booking : bookings) {
            boolean noConflict = booking.getEndDate().isBefore(requestedStartDate) || booking.getStartDate().isAfter(requestedEndDate);

            if (!noConflict && currentBooking.getRoom().getId() == booking.getRoom().getId()) {
                available = false;
            }
        }
        if (!available) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Valt rum ej ledigt under önskad period.");
        }

        currentBooking.setStartDate(requestedStartDate);
        currentBooking.setEndDate(requestedEndDate);
        bookingRepo.save(currentBooking);
        return BookingToBookingDTO(currentBooking);
    }


    public void removeBooking(Long bookingID) {
        if (!bookingRepo.existsById(bookingID)) {
            log.warn("No booking with id: {}.", bookingID);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Bokningen hittades ej.");
        }
        bookingRepo.deleteById(bookingID);
        log.info("Deleted booking with id: {}", bookingID);
    }

    public boolean canParseDate(String date) {
        try {
            LocalDate.parse(date);
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    public BookingDTO editById(Long customerId, String startDate, String endDate) {
        Booking editedBooking = bookingRepo.findById(customerId).orElseThrow(() -> new RuntimeException("Bokningen hittades ej"));

        LocalDate requestedStartDate = LocalDate.parse(startDate);
        LocalDate requestedEndDate = LocalDate.parse(endDate);

        if (requestedEndDate.isBefore(requestedStartDate)) {
            throw new RoomNotAvailableException("Slutdatum kan inte vara innan startdatum.");
        }

        editedBooking.setStartDate(requestedStartDate);
        editedBooking.setEndDate(requestedEndDate);

        bookingRepo.save(editedBooking);
        return BookingToBookingDTO(editedBooking);
    }

    public BookingDTO getBookingById(Long id) {
        return BookingToBookingDTO(Objects.requireNonNull(bookingRepo.findById(id).orElse(null)));
    }

    public boolean canAddBeds(Long id, int extraBeds) {

        if (extraBeds < 1) {
            throw new RuntimeException("För få sängar.");
        } else {
            if (extraBeds > 3) {
                throw new RuntimeException("För många sängar.");
            }
        }

        BookingDTO currentBooking = getBookingById(id);
        if (currentBooking.getRoom().isDoubleRoom()) {
            currentBooking.setExtraBeds(extraBeds);
            return true;
        }

        return false;
    }

//    public boolean customerHasBookings(Long customerId) {
//        return bookingRepo.existsByCustomerId(customerId);
//    }
}

