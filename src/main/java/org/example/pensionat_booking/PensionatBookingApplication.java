package org.example.pensionat_booking;

import org.example.pensionat_booking.Model.Booking;
import org.example.pensionat_booking.Model.Room;
import org.example.pensionat_booking.Repository.BookingRepository;
import org.example.pensionat_booking.Repository.RoomRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;
import org.springframework.beans.factory.annotation.Value;
import java.time.LocalDate;

@SpringBootApplication
public class PensionatBookingApplication {

    public static void main(String[] args) {
        SpringApplication.run(PensionatBookingApplication.class, args);
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    public CommandLineRunner createRooms(RoomRepository roomRepo, BookingRepository bookingRepo) {
        return (args) -> {

            if (roomRepo.count() == 0) {

                LocalDate d1 = LocalDate.of(2026, 5, 18);
                LocalDate d2 = LocalDate.of(2026, 5, 20);
                LocalDate d3 = LocalDate.of(2026, 6, 18);
                LocalDate d4 = LocalDate.of(2026, 6, 20);
                LocalDate d5 = LocalDate.of(2026, 8, 3);
                LocalDate d6 = LocalDate.of(2026, 8, 7);

                Room r1 = roomRepo.save(new Room("A1", true));
                Room r2 = roomRepo.save(new Room("A2", true));
                Room r3 = roomRepo.save(new Room("A3", true));
                Room r4 = roomRepo.save(new Room("A4", true));
                Room r5 = roomRepo.save(new Room("A5", true));
                Room r6 = roomRepo.save(new Room("B6", false));
                Room r7 = roomRepo.save(new Room("B7", false));
                Room r8 = roomRepo.save(new Room("B8", false));
                Room r9 = roomRepo.save(new Room("B9", false));
                Room r10 = roomRepo.save(new Room("B10", false));


                bookingRepo.save(new Booking(r1, 1L, d1, d2));
                bookingRepo.save(new Booking(r4, 2L, d3, d4));
                bookingRepo.save(new Booking(r8, 3L, d5, d6));
            }
        };
    }

}
