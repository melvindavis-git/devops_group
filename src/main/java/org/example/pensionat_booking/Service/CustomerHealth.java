package org.example.pensionat_booking.Service;

import org.jspecify.annotations.Nullable;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.stereotype.Component;

@Component
public class CustomerHealth implements HealthIndicator {

    private final CustomerServiceClient customerServiceClient;

    public CustomerHealth(CustomerServiceClient customerServiceClient) {
        this.customerServiceClient = customerServiceClient;
    }

    @Override
    public Health health() {
        try {
            customerServiceClient.getAllCustomers();

            return Health.up().
                    withDetail("customerService", "Available").build();
        } catch (Exception e) {
            return Health.unknown().
                    withDetail("customerService", "Unavailable").
                    withDetail("error", e.getMessage()).build();
        }
    }
}
