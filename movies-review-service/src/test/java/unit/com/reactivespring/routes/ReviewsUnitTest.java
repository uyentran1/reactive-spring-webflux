package com.reactivespring.routes;

import com.reactivespring.domain.Review;
import com.reactivespring.handler.ReviewHandler;
import com.reactivespring.repository.ReviewRepository;
import com.reactivespring.router.ReviewRouter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ReviewsUnitTest {

    WebTestClient webTestClient;

    @Mock
    private ReviewRepository reviewRepository;

    private static final String REVIEWS_URL = "/v1/reviews";

    @BeforeEach
    void setup() {
        var reviewHandler = new ReviewHandler(reviewRepository);
        var routerFunction = new ReviewRouter().reviewsRoute(reviewHandler);
        webTestClient = WebTestClient.bindToRouterFunction(routerFunction).build();
    }

    @Test
    void addReview() {
        var review = new Review(null, 1L, "Awesome Movie", 9.0);
        var savedReview = new Review("mockId", 1L, "Awesome Movie", 9.0);

        when(reviewRepository.save(isA(Review.class))).thenReturn(Mono.just(savedReview));

        var result = webTestClient.post()
                .uri(REVIEWS_URL)
                .bodyValue(review)
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
                .expectBody(Review.class)
                .returnResult();

        var responseReview = result.getResponseBody();
        assertNotNull(responseReview);
        assertNotNull(responseReview.getReviewId());
        assertEquals("mockId", responseReview.getReviewId());
    }

    @Test
    void getReviews() {
        var reviewsList = List.of(
                new Review("abc", 1L, "Awesome Movie", 9.0),
                new Review("def", 1L, "Awesome Movie1", 9.0),
                new Review("ghi", 2L, "Excellent Movie", 8.0));

        when(reviewRepository.findAll()).thenReturn(Flux.fromIterable(reviewsList));

        webTestClient.get()
                .uri(REVIEWS_URL)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
                .expectBodyList(Review.class)
                .hasSize(3);
    }

    @Test
    void getReviewsByMovieInfoId() {
        var reviewsList = List.of(
                new Review("abc", 1L, "Awesome Movie", 9.0),
                new Review("def", 1L, "Awesome Movie1", 9.0));

        when(reviewRepository.findByMovieInfoId(1L)).thenReturn(Flux.fromIterable(reviewsList));

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path(REVIEWS_URL)
                        .queryParam("movieInfoId", 1L)
                        .build())
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
                .expectBodyList(Review.class)
                .hasSize(2);
    }

    @Test
    void updateReview() {
        var id = "abc";
        var review = new Review(null, 1L, "Awesome awesome Movie", 9.5);
        var existingReview = new Review(id, 1L, "Awesome Movie", 9.0);
        var updatedReview = new Review(id, 1L, "Awesome awesome Movie", 9.5);

        when(reviewRepository.findById(id)).thenReturn(Mono.just(existingReview));
        when(reviewRepository.save(isA(Review.class))).thenReturn(Mono.just(updatedReview));

        var result = webTestClient.put()
                .uri(REVIEWS_URL + "/{id}", id)
                .bodyValue(review)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
                .expectBody(Review.class)
                .returnResult();

        var responseReview = result.getResponseBody();
        assertNotNull(responseReview);
        assertEquals(id, responseReview.getReviewId());
        assertEquals("Awesome awesome Movie", responseReview.getComment());
        assertEquals(9.5, responseReview.getRating());
    }

    @Test
    void deleteReview() {
        var id = "abc";

        when(reviewRepository.deleteById(id)).thenReturn(Mono.empty());

        webTestClient.delete()
                .uri(REVIEWS_URL + "/{id}", id)
                .exchange()
                .expectStatus().isNoContent()
                .expectBody().isEmpty();
    }
}
