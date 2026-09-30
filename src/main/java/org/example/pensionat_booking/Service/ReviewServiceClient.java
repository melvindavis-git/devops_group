package org.example.pensionat_booking.Service;

import org.example.pensionat_booking.DTO.ReviewRequestDTO;
import org.example.pensionat_booking.DTO.ReviewResponseDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.net.http.HttpClient;
import java.util.List;

@Service
public class ReviewServiceClient {

    @Value("${reviews-service.base-url}")
    private String baseUrl;
    RestTemplate restTemplate;


    public ReviewServiceClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;

    }

    public List<ReviewResponseDTO> getAllReviews(){

        try {
            List<ReviewResponseDTO> reviews = restTemplate.getForObject(baseUrl + "/review", List.class);
            return reviews;
        } catch (RestClientException e) {
             throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Något gick fel");
        }
    }


    public ReviewResponseDTO createReview(ReviewRequestDTO requestDTO) {
        try {
            return restTemplate.postForObject(baseUrl + "/review", requestDTO, ReviewResponseDTO.class);
        } catch (RestClientException e) {
            throw new RestClientException("Error");
        }
    }
}
