package com.reactivespring.routes;

import com.reactivespring.domain.Review;
import com.reactivespring.repository.ReviewRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class ReviewsIntgTest {

    @Autowired
    ReviewRepository reviewRepository;

    private WebTestClient webTestClient;

    @LocalServerPort
    private int serverPort;

    private static final String REVIEWS_URL = "/v1/reviews";

    @BeforeEach
    void setUp() {
        webTestClient = WebTestClient.bindToServer()
                .baseUrl("http://localhost:" + serverPort)
                .build();

        var reviewsList = List.of(
                new Review(null, 1L, "Awesome Movie", 9.0),
                new Review(null, 1L, "Awesome Movie1", 9.0),
                new Review(null, 2L, "Excellent Movie", 8.0));

        reviewRepository.saveAll(reviewsList).blockLast();
    }

    @AfterEach
    void tearDown() {
        reviewRepository.deleteAll().block();
    }

    @Test
    void addReview() {
        var review = new Review(null, 1L, "Awesome Movie", 9.0);

        var result = webTestClient.post()
                .uri(REVIEWS_URL)
                .bodyValue(review)
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
                .expectBody(Review.class)
                .returnResult();

        var savedReview = result.getResponseBody();
        assertNotNull(savedReview);
        assertNotNull(savedReview.getReviewId());
    }
}
