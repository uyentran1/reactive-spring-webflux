package com.reactivespring.controller;

import com.reactivespring.domain.MovieInfo;
import com.reactivespring.exceptionhandler.GlobalErrorHandler;
import com.reactivespring.service.MovieInfoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class MovieInfoControllerUnitTest {

    WebTestClient webTestClient;
    MovieInfoController movieInfoController;

    @Mock
    private MovieInfoService movieInfoServiceMock;

    private static final String MOVIE_INFO_URL = "/v1/movieinfos";

    @BeforeEach
    void setup() {
        movieInfoController = new MovieInfoController(movieInfoServiceMock);
        webTestClient = WebTestClient.bindToController(movieInfoController)
                .controllerAdvice(new GlobalErrorHandler())
                .build();
    }

    @Test
    void getAllMoviesInfo() {
        var movieInfos = List.of(
                new MovieInfo(null, "Batman Begins", 2005, List.of(
                        "Christian Bale", "Michael Cane"), LocalDate.parse("2005-06-15")),
                new MovieInfo(null, "The Dark Knight", 2008, List.of(
                        "Christian Bale", "Heath Ledger"), LocalDate.parse("2008-07-18")),
                new MovieInfo("abc", "Dark Knight Rises", 2012, List.of(
                        "Christian Bale", "Tom Hardy"), LocalDate.parse("2012-07-20")));

        when(movieInfoServiceMock.getAllMovieInfos())
                .thenReturn(Flux.fromIterable(movieInfos));

        webTestClient.get()
                .uri(MOVIE_INFO_URL)
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBodyList(MovieInfo.class)
                .hasSize(3);
    }

    @Test
    void getMovieInfoById() {
        String MOVIE_ID = "abc";
        var movieInfo = new MovieInfo("abc", "Dark Knight Rises", 2012, List.of(
                "Christian Bale", "Tom Hardy"), LocalDate.parse("2012-07-20"));

        when(movieInfoServiceMock.getMovieInfoById(MOVIE_ID))
                .thenReturn(Mono.just(movieInfo));

        webTestClient.get()
                .uri(MOVIE_INFO_URL + "/{id}", MOVIE_ID)
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.name").isEqualTo("Dark Knight Rises");
    }

    @Test
    void addMovieInfo() {
        var movieInfo = new MovieInfo(null, "Batman Begins 1", 2005, List.of(
                "Christian Bale", "Michael Cane"), LocalDate.parse("2005-06-15"));

        var mockMovieInfo = new MovieInfo("mockId", "Batman Begins 1", 2005, List.of(
                "Christian Bale", "Michael Cane"), LocalDate.parse("2005-06-15"));

        when(movieInfoServiceMock.addMovieInfo(isA(MovieInfo.class)))
                .thenReturn(Mono.just(mockMovieInfo));

        var result = webTestClient.post()
                .uri(MOVIE_INFO_URL)
                .bodyValue(movieInfo)
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
                .expectBody(MovieInfo.class)
                .returnResult();

        var savedMovieInfo = result.getResponseBody();
        assertNotNull(savedMovieInfo);
        assertNotNull(savedMovieInfo.getMovieInfoId());
        assertEquals("mockId", savedMovieInfo.getMovieInfoId());
    }

    @Test
    void addMovieInfo_validation() {
        var movieInfo = new MovieInfo(null, "", -2005, List.of(
                ""), LocalDate.parse("2005-06-15"));

        webTestClient.post()
            .uri(MOVIE_INFO_URL)
            .bodyValue(movieInfo)
            .exchange()
            .expectStatus().isBadRequest()
            .expectBody(String.class)
            .consumeWith(result -> {
                var responseBody = result.getResponseBody();
                System.out.println("responseBody: " + responseBody);
                var expectedErrorMessage = "movieInfo.cast must be present, " +
                        "movieInfo.name must be present, " +
                        "movieInfo.year must be a positive value";
                assert responseBody != null;
                assertEquals(expectedErrorMessage, responseBody);
            });
    }

    @Test
    void updateMovieInfo() {
        var movieInfoId = "abc";

        var movieInfo = new MovieInfo(null, "Batman Begins 1", 2005, List.of(
                "Christian Bale", "Michael Cane"), LocalDate.parse("2005-06-15"));

        when(movieInfoServiceMock.updateMovieInfo(isA(MovieInfo.class), isA(String.class)))
                .thenReturn(Mono.just(new MovieInfo(movieInfoId, "Batman Begins 1", 2005, List.of(
                        "Christian Bale", "Michael Cane"), LocalDate.parse("2005-06-15"))));

        var result = webTestClient.put()
                .uri(MOVIE_INFO_URL + "/{id}", movieInfoId)
                .bodyValue(movieInfo)
                .exchange()
                .expectStatus().is2xxSuccessful()
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
                .expectBody(MovieInfo.class)
                .returnResult();

        var updatedMovieInfo = result.getResponseBody();
        assertNotNull(updatedMovieInfo);
        assertNotNull(updatedMovieInfo.getMovieInfoId());
        assertEquals("abc", updatedMovieInfo.getMovieInfoId());
    }

    @Test
    void deleteMovieInfo() {
        var movieInfoId = "abc";

        when(movieInfoServiceMock.deleteMovieInfoById(isA(String.class)))
                .thenReturn(Mono.empty());

        webTestClient.delete()
                .uri(MOVIE_INFO_URL + "/{id}", movieInfoId)
                .exchange()
                .expectStatus().isNoContent();
    }

}
