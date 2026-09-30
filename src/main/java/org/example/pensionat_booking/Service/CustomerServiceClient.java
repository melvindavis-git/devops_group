package org.example.pensionat_booking.Service;

import lombok.extern.slf4j.Slf4j;
import org.example.pensionat_booking.DTO.CustomerDTO;
import org.example.pensionat_booking.Model.Booking;
import org.example.pensionat_booking.Repository.BookingRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Slf4j
@Service
public class CustomerServiceClient {
    private final BookingRepository bookingRepo;

    private final RestTemplate restTemplate;

    @Value("${customer-service.base-url}")
    String baseUrl;

    public CustomerServiceClient(BookingRepository bookingRepo, RestTemplate restTemplate) {
        this.bookingRepo = bookingRepo;
        this.restTemplate = restTemplate;
    }

    public ResponseEntity<List<CustomerDTO>> getAllCustomers() {
        try {
            List<CustomerDTO> customers = restTemplate.getForObject(baseUrl + "/customers/all", List.class);
            return ResponseEntity.status(HttpStatus.OK).body(customers);

        } catch (HttpClientErrorException.BadRequest e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    public ResponseEntity<CustomerDTO> registerCustomer(CustomerDTO inputCustomer) {

        CustomerDTO savedCst = new CustomerDTO();
        try {
            savedCst = restTemplate.postForObject(baseUrl + "/customers/register", inputCustomer, CustomerDTO.class);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedCst);
        } catch (HttpClientErrorException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(savedCst);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public ResponseEntity<Void> deleteById(Long customerId) {

        for (Booking booking : bookingRepo.findAll()) {
            if (booking.getCustomerId().equals(customerId)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Personen har bokningar");
            }
        }
        try {
            restTemplate.delete(baseUrl + "/customers/delete/{customerId}", customerId);
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body(null);
        } catch (HttpClientErrorException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    public ResponseEntity<CustomerDTO> getCustomerById(Long id) {

        CustomerDTO customerByID;

        try {
            customerByID = restTemplate.getForObject(baseUrl + "/customers/{id}", CustomerDTO.class, id);
        } catch (HttpClientErrorException.NotFound e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        return ResponseEntity.status(HttpStatus.OK).body(customerByID);
    }

    public String getCustomerNameById(Long id) {

        new CustomerDTO();
        CustomerDTO customer;

        try {
            customer = restTemplate.getForObject(baseUrl + "/customers/{id}", CustomerDTO.class, id);
            if (customer.getName() == null || customer.getName().equals("")) {
                return ":(";
            }

        } catch (HttpClientErrorException.NotFound e) {
            return "Kund hittades ej.";
        } catch (HttpClientErrorException.BadRequest e) {
            return "Inte tillgängligt.";
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return customer.getName();
    }

    public ResponseEntity<CustomerDTO> editById(CustomerDTO editedCustomer) {

        try {
            CustomerDTO editedCst = restTemplate.exchange(baseUrl + "/customers/editCst", HttpMethod.PUT, new HttpEntity<>(editedCustomer), CustomerDTO.class).getBody();
            return ResponseEntity.status(HttpStatus.OK).body(editedCst);

        } catch (HttpClientErrorException.NotFound e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        } catch (HttpClientErrorException.BadRequest e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }
}
