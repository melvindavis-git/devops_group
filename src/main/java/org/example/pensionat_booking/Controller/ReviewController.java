package org.example.pensionat_booking.Controller;

import org.example.pensionat_booking.DTO.ReviewRequestDTO;
import org.example.pensionat_booking.DTO.ReviewResponseDTO;
import org.example.pensionat_booking.Service.ReviewServiceClient;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/reviews")
public class ReviewController {

    ReviewServiceClient reviewServiceClient;

    public ReviewController(ReviewServiceClient reviewServiceClient) {
        this.reviewServiceClient = reviewServiceClient;
    }

    @GetMapping()
    public ResponseEntity<?> getAllReviews() {
        try {
            List<ReviewResponseDTO> reviews = reviewServiceClient.getAllReviews();
            return ResponseEntity.ok(reviews);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping()
    public ResponseEntity<ReviewResponseDTO> creatReview(@RequestBody ReviewRequestDTO requestDTO){
        return ResponseEntity.ok(reviewServiceClient.createReview(requestDTO));
    }
}
