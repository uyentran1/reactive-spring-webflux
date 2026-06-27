package com.reactivespring.controller;

import com.reactivespring.domain.MovieInfo;
import com.reactivespring.repository.MovieInfoRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
 class MovieInfoControllerIntgTest {

    @Autowired
    MovieInfoRepository movieInfoRepository;

    private WebTestClient webTestClient;

    @LocalServerPort
    private int serverPort;

    private static final String MOVIE_INFO_URL = "/v1/movieinfos";

    @BeforeEach
    void setUp() {
        webTestClient = WebTestClient.bindToServer()
                .baseUrl("http://localhost:" + serverPort)
                .build();
        // ensure a clean slate before each test and then insert deterministic test data
        movieInfoRepository.deleteAll().block();

        var movieInfos = List.of(
                new MovieInfo(null, "Batman Begins", 2005, List.of(
                        "Christian Bale", "Michael Cane"), LocalDate.parse("2005-06-15")),
                new MovieInfo(null, "The Dark Knight", 2008, List.of(
                        "Christian Bale", "Heath Ledger"), LocalDate.parse("2008-07-18")),
                new MovieInfo("abc", "Dark Knight Rises", 2012, List.of(
                        "Christian Bale", "Tom Hardy"), LocalDate.parse("2012-07-20")));

        var saved = movieInfoRepository.saveAll(movieInfos).collectList().block();
        assertNotNull(saved);
        assertEquals(3, saved.size());
     }

    @AfterEach
    void tearDown() {
        movieInfoRepository.deleteAll().block();
    }

    @Test
    void getAllMovieInfos() {
        webTestClient.get()
                .uri(MOVIE_INFO_URL)
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBodyList(MovieInfo.class)
                .hasSize(3);
    }

    @Test
    void getMovieInfoByYear() {
        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path(MOVIE_INFO_URL)
                        .queryParam("year", 2005)
                        .build())
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBodyList(MovieInfo.class)
                .hasSize(1);
    }

    @Test
    void getMovieInfoById() {
        String MOVIE_ID = "abc";
        String MOVIE_NAME = "Dark Knight Rises";
        webTestClient.get()
                .uri(MOVIE_INFO_URL + "/{id}", MOVIE_ID)
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.name").isEqualTo(MOVIE_NAME);
    }

    @Test
    void getMovieInfoById_notfound() {
        String MOVIE_ID = "def";

        webTestClient.get()
                .uri(MOVIE_INFO_URL + "/{id}", MOVIE_ID)
                .exchange()
                .expectStatus()
                .isNotFound();
    }

    @Test
    void addMovieInfo() {
        var movieInfo = new MovieInfo(null, "Batman Begins 1", 2005, List.of(
                "Christian Bale", "Michael Cane"), LocalDate.parse("2005-06-15"));

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

        // verify persisted in repository
        var persisted = movieInfoRepository.findById(savedMovieInfo.getMovieInfoId()).block();
        assertNotNull(persisted);
        assertEquals("Batman Begins 1", persisted.getName());
     }

    @Test
    void updateMovieInfo() {
        String MOVIE_ID = "abc";
        String MOVIE_NEW_NAME = "Dark Knight Rises 1";
        var updatedMovieInfo = new MovieInfo(null, MOVIE_NEW_NAME, 2012, List.of(
                "Christian Bale", "Tom Hardy"), LocalDate.parse("2012-07-20"));

        webTestClient.put()
                .uri(MOVIE_INFO_URL + "/{id}", MOVIE_ID)
                .bodyValue(updatedMovieInfo)
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.name").isEqualTo(MOVIE_NEW_NAME);

        // verify persisted
        var persisted = movieInfoRepository.findById(MOVIE_ID).block();
        assertNotNull(persisted);
        assertEquals(MOVIE_NEW_NAME, persisted.getName());
     }

    @Test
    void updateMovieInfo_notfound() {
        String MOVIE_ID = "def";
        var movieInfo = new MovieInfo(null, "ABC", 2012, List.of(
                "Christian Bale", "Tom Hardy"), LocalDate.parse("2012-07-20"));

        webTestClient.put()
                .uri(MOVIE_INFO_URL + "/{id}", MOVIE_ID)
                .bodyValue(movieInfo)
                .exchange()
                .expectStatus()
                .isNotFound();
    }

    @Test
    void deleteMovieInfoById() {
        String MOVIE_ID = "abc";

        webTestClient.delete()
                .uri(MOVIE_INFO_URL + "/{id}", MOVIE_ID)
                .exchange()
                .expectStatus()
                .isNoContent();

        // verify removed from repository
        var found = movieInfoRepository.findById(MOVIE_ID).blockOptional();
        org.junit.jupiter.api.Assertions.assertFalse(found.isPresent());
     }

 }

