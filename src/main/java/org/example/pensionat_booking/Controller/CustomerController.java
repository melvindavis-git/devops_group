package org.example.pensionat_booking.Controller;

import jakarta.validation.Valid;
import org.example.pensionat_booking.DTO.CustomerDTO;
import org.example.pensionat_booking.Service.CustomerServiceClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {

    private final CustomerServiceClient service;
    private static final Logger log = LoggerFactory.getLogger(CustomerController.class);

    public CustomerController(CustomerServiceClient service) {
        this.service = service;
    }

    @GetMapping()
    public ResponseEntity<List<CustomerDTO>> getAllCustomers() {
        log.info("GET request for all customers");
        return service.getAllCustomers();
    }

    @GetMapping("/{id}")
    public ResponseEntity<CustomerDTO> getById(@PathVariable Long id) {
        return service.getCustomerById(id);
    }


    @PostMapping("/register")
    public ResponseEntity<?> registerCustomers(@Valid @RequestBody CustomerDTO customerDTO) {
        log.info("POST request to register customer");
        try {
            return service.registerCustomer(customerDTO);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }

    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable Long id) {
        return service.deleteById(id);
    }

    @PutMapping("/edit")
    public ResponseEntity<CustomerDTO> editCustomer(@RequestBody CustomerDTO customerDTO) {
        return service.editById(customerDTO);
    }


}
