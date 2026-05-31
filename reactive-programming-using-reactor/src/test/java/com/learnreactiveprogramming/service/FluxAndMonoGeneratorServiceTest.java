package com.learnreactiveprogramming.service;

import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

import java.util.List;

class FluxAndMonoGeneratorServiceTest {
    FluxAndMonoGeneratorService fluxAndMonoGeneratorService =
            new FluxAndMonoGeneratorService();

    @Test
    void namesFlux() {
        var namesFlux = fluxAndMonoGeneratorService.namesFlux();

        StepVerifier.create(namesFlux)
//                .expectNext("alex", "ben", "chloe")
                .expectNextCount(3)
                .verifyComplete();
    }

    @Test
    void namesFlux_map() {
        var namesFlux = fluxAndMonoGeneratorService.namesFlux_map(3);

        StepVerifier.create(namesFlux)
                .expectNext("4-ALEX", "5-CHLOE")
                .verifyComplete();

    }

    @Test
    void namesFlux_flatmap() {
        int minLength = 3;

        var namesFlux = fluxAndMonoGeneratorService.namesFlux_flatmap(minLength);

        StepVerifier.create(namesFlux)
                .expectNext("A", "L", "E", "X", "C", "H", "L", "O", "E")
                .verifyComplete();
    }

    @Test
    void namesFlux_flatmap_async() {
        int minLength = 3;

        var namesFlux = fluxAndMonoGeneratorService.namesFlux_flatmap_async(minLength);

        StepVerifier.create(namesFlux)
                .expectNextCount(9)
                .verifyComplete();

    }

    @Test
    void namesFlux_concatmap() {
        int minLength = 3;

        var namesFlux = fluxAndMonoGeneratorService.namesFlux_concatmap(minLength);

        StepVerifier.create(namesFlux)
                .expectNext("A", "L", "E", "X", "C", "H", "L", "O", "E")
                .verifyComplete();

    }

    @Test
    void namesFlux_immutability() {
        var namesFlux = fluxAndMonoGeneratorService.namesFlux_immutability();

        StepVerifier.create(namesFlux)
                .expectNext("alex", "ben", "chloe")
                .verifyComplete();
    }

    @Test
    void namesMono_flatmap() {
        int minLength = 3;

        var namesMono = fluxAndMonoGeneratorService.namesMono_flatmap(minLength);

        StepVerifier.create(namesMono)
                .expectNext(List.of("A", "L", "E", "X"))
                .verifyComplete();
    }

    @Test
    void namesMono_flatMapMany() {
        int minLength = 3;

        var value = fluxAndMonoGeneratorService.namesMono_flatMapMany(minLength);

        StepVerifier.create(value)
                .expectNext("A", "L", "E", "X")
                .verifyComplete();
    }

    @Test
    void namesFlux_transform() {
        int minLength = 3;

        var namesFlux = fluxAndMonoGeneratorService.namesFlux_transform(minLength);

        StepVerifier.create(namesFlux)
                .expectNext("A", "L", "E", "X", "C", "H", "L", "O", "E")
                .verifyComplete();
    }

    @Test
    void namesFlux_transform_1() {
        int minLength = 6;

        var namesFlux = fluxAndMonoGeneratorService
                .namesFlux_transform(minLength);

        StepVerifier.create(namesFlux)
                .expectNext("default")
                .verifyComplete();
    }

    @Test
    void namesFlux_transform_2() {
        int minLength = 6;

        var namesFlux = fluxAndMonoGeneratorService
                .namesFlux_transform_switchIfEmpty(minLength);

        StepVerifier.create(namesFlux)
                .expectNext("D", "E", "F", "A", "U", "L", "T")
                .verifyComplete();
    }

    @Test
    void explore_concat() {
        var concatFlux = fluxAndMonoGeneratorService.explore_concat();

        StepVerifier.create(concatFlux)
                .expectNext("A", "B", "C", "D", "E", "F")
                .verifyComplete();
    }

    @Test
    void explore_concatWith() {
        var concatWithFlux = fluxAndMonoGeneratorService.explore_concatWith();

        StepVerifier.create(concatWithFlux)
                .expectNext("A", "B")
                .verifyComplete();
    }

    @Test
    void explore_merge() {
        var mergeFlux = fluxAndMonoGeneratorService.explore_merge();

        StepVerifier.create(mergeFlux)
                .expectNext("A", "D", "B", "E", "C", "F")
                .verifyComplete();
    }

    @Test
    void explore_mergeWith() {
        var mergeWithFlux = fluxAndMonoGeneratorService.explore_mergeWith();

        StepVerifier.create(mergeWithFlux)
                .expectNext("A", "D", "B", "E", "C", "F")
                .verifyComplete();
    }

    @Test
    void explore_mergeWith_mono() {
        var mergeWithFlux = fluxAndMonoGeneratorService.explore_mergeWith_mono();

        StepVerifier.create(mergeWithFlux)
                .expectNext("A", "B")
                .verifyComplete();
    }

    @Test
    void explore_mergeSequential() {
        var mergeSequential = fluxAndMonoGeneratorService.explore_mergeSequential();

        StepVerifier.create(mergeSequential)
                .expectNext("A", "B", "C", "D", "E", "F")
                .verifyComplete();
    }

    @Test
    void explore_zip() {
        var zipFlux = fluxAndMonoGeneratorService.explore_zip();

        StepVerifier.create(zipFlux)
                .expectNext("AD", "BE", "CF")
                .verifyComplete();
    }

    @Test
    void explore_zip_1() {
        var zipFlux = fluxAndMonoGeneratorService.explore_zip_1();

        StepVerifier.create(zipFlux)
                .expectNext("AD14", "BE25", "CF36")
                .verifyComplete();
    }

    @Test
    void explore_zipWith() {
        var zipWithFlux = fluxAndMonoGeneratorService.explore_zipWith();

        StepVerifier.create(zipWithFlux)
                .expectNext("AD", "BE", "CF")
                .verifyComplete();
    }

    @Test
    void explore_zipWith_mono() {
        var zipWithMono = fluxAndMonoGeneratorService.explore_zipWith_mono();

        StepVerifier.create(zipWithMono)
                .expectNext("AB")
                .verifyComplete();
    }
}
