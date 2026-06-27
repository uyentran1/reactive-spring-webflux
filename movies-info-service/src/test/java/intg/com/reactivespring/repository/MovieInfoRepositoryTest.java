package com.reactivespring.repository;

import com.reactivespring.domain.MovieInfo;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import reactor.test.StepVerifier;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@ActiveProfiles("test")
class MovieInfoRepositoryIntgTest {

    @Autowired
    MovieInfoRepository movieInfoRepository;

    @BeforeEach
    void setUp() {

        var moviesInfo = List.of(
                new MovieInfo(null, "Batman Begins", 2005, List.of(
                        "Christian Bale", "Michael Cane"), LocalDate.parse("2005-06-15")),
                new MovieInfo(null, "The Dark Knight", 2008, List.of(
                        "Christian Bale", "Heath Ledger"), LocalDate.parse("2008-07-18")),
                new MovieInfo("abc", "Dark Knight Rises", 2012, List.of(
                        "Christian Bale", "Tom Hardy"), LocalDate.parse("2012-07-20")));

        movieInfoRepository.saveAll(moviesInfo)
                .blockLast();
    }

    @AfterEach
    void tearDown() {
        movieInfoRepository.deleteAll().block();
    }

    @Test
    void findAll() {
        var movieInfosList = movieInfoRepository.findAll().collectList().log();

        StepVerifier.create(movieInfosList)
                .assertNext(movieInfos -> {
                    assertEquals(3, movieInfos.size());
                    assertEquals("Batman Begins", movieInfos.get(0).getName());
                    assertEquals("The Dark Knight", movieInfos.get(1).getName());
                    assertEquals("Dark Knight Rises", movieInfos.get(2).getName());
                })
                .verifyComplete();
    }

    @Test
    void findById() {
        String movieId = "abc";

        var movieInfoMono = movieInfoRepository.findById(movieId).log();

        StepVerifier.create(movieInfoMono)
                .assertNext(movieInfo -> {
                    assertEquals("Dark Knight Rises", movieInfo.getName());
                })
                .verifyComplete();
    }

    @Test
    void findByName() {
        String name = "Dark Knight Rises";

        var movieInfoMono = movieInfoRepository.findByName(name);

        StepVerifier.create(movieInfoMono)
                .assertNext(movieInfo -> {
                    assertEquals("abc", movieInfo.getMovieInfoId());
                })
                .verifyComplete();
    }

    @Test
    void findByYear() {
        Integer year = 2005;

        var movieInfoFlux = movieInfoRepository.findByYear(year);

        StepVerifier.create(movieInfoFlux)
                .assertNext(movieInfo -> {
                    assertEquals("Batman Begins", movieInfo.getName());
                })
                .verifyComplete();
    }

    @Test
    void saveMovieInfo() {
        var newMovieInfo = new MovieInfo(null, "Batman Begins 1", 2005, List.of(
                "Christian Bale", "Michael Cane"), LocalDate.parse("2005-06-15"));

        var movieInfoMono = movieInfoRepository.save(newMovieInfo).log();

        StepVerifier.create(movieInfoMono)
                .assertNext(movieInfo -> {
                    assertNotNull(movieInfo.getMovieInfoId());
                    assertEquals("Batman Begins 1", movieInfo.getName());
                })
                .verifyComplete();
    }

    @Test
    void updateMovieInfo() {
        var movieInfo = movieInfoRepository.findById("abc").block();
        movieInfo.setYear(2026);

        var movieInfoMono = movieInfoRepository.save(movieInfo).log();

        StepVerifier.create(movieInfoMono)
                .assertNext(updatedInfo -> {
                    assertEquals(2026, updatedInfo.getYear());
                })
                .verifyComplete();
    }

    @Test
    void deleteMovieInfo() {
        // Add block() to ensure deletion completes before findAll() executes
        movieInfoRepository.deleteById("abc").block();

        var movieInfosFlux = movieInfoRepository.findAll().log();

        StepVerifier.create(movieInfosFlux)
                .expectNextCount(2)
                .verifyComplete();
    }

}
