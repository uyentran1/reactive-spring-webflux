package com.learnreactiveprogramming.service;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;
import java.util.Random;
import java.util.function.Function;

// map(): used for simple transformation from item -> item
// flatMap(): used for transformation from item -> reactive type (Flux/Mono)
// concatMap(): similar to flatMap but preserve ordering
// flatMapMany(): transform mono to flux
// transform(): takes in a Function Functional Interface, which handles the
// operations on a reactive type and produces a reactive type

// concat() and concatWith()
// * used to combine 2 reactive streams into one
// * happens in sequence: first one subscribed & completed then second one
// * concat(): static method in Flux
// * concatWith(): instance method in Flux & Mono

// merge() and mergeWith()
// * unlike concat(), both publishers subscribed at the same time and merge
// happens in an interleaved fashion
// * merge(): static method in Flux
// * mergeWith(): instance method in Flux & Mono

// mergeSequential()
// * used to combine 2 Publishers (Flux) into one
// * static method in Flux
// * both publishers subscribed at the same time (eagerly), merge happens in
// sequence

// zip() and zipWith()
// * zip(): static method in Flux, used to merge up to 2-8 Publishers (Flux or
// Mono) into one
// * zipWith(): instance method in Flux & Mono, used to merge 2 Publishers into one
// Publishers are subscribed eagerly
// waits for all the Publishers involved in the transformation to emit one element
// continues until one publisher sends an OnComplete event

public class FluxAndMonoGeneratorService {

    public Flux<String> namesFlux() {
        return Flux.fromIterable(List.of("alex", "ben", "chloe"));
    }

    public Flux<String> namesFlux_map(int minLength) {
        // filter strings whose length is greater than minLength
        return Flux.fromIterable(List.of("alex", "ben", "chloe"))
                .filter(s -> s.length() > minLength)
                .map(s -> s.length() + "-" + s.toUpperCase())
                .log(); // transform to 4-ALEX, 5-CHLOE
//                .map(String::toUpperCase);
    }

    public Flux<String> namesFlux_immutability() {
        var namesFlux = Flux.fromIterable(List.of("alex", "ben", "chloe"));
        namesFlux.map(String::toUpperCase);
        return namesFlux;
    }

    public Mono<String> namesMono_map_filter(int minLength) {
        return Mono.just("alex")
                .map(String::toUpperCase)
                .filter(s -> s.length() > minLength);
    }

    public Mono<List<String>> namesMono_flatmap(int minLength) {
        return Mono.just("alex")
                .map(String::toUpperCase)
                .filter(s -> s.length() > minLength)
                .flatMap(this::splitStringMono)
                .log(); // Mono<List<A, L, E, X>
    }

    private Mono<List<String>> splitStringMono(String s) {
        var chars = s.split("");
        return Mono.just(List.of(chars));
    }

    public Flux<String> namesMono_flatMapMany(int minLength) {
        return Mono.just("alex")
                .map(String::toUpperCase)
                .filter(s -> s.length() > minLength)
                .flatMapMany(this::splitString)
                .log(); // Flux<A, L, E, X>
    }

    public Flux<String> namesFlux_flatmap(int minLength) {
        // filter strings whose length is greater than minLength
        return Flux.fromIterable(List.of("alex", "ben", "chloe"))
                .map(String::toUpperCase)
                .filter(s -> s.length() > minLength)
                // ALEX, CHLOE -> A, L, E, X, C, H, L, O, E
                .flatMap(this::splitString)
                .log();
    }

    public Flux<String> namesFlux_flatmap_async(int minLength) {
        // filter strings whose length is greater than minLength
        return Flux.fromIterable(List.of("alex", "ben", "chloe"))
                .map(String::toUpperCase)
                .filter(s -> s.length() > minLength)
                // ALEX, CHLOE -> A, L, E, X, C, H, L, O, E
                .flatMap(this::splitString_withDelay)
                .log();
    }

    public Flux<String> namesFlux_concatmap(int minLength) {
        // filter strings whose length is greater than minLength
        return Flux.fromIterable(List.of("alex", "ben", "chloe"))
                .map(String::toUpperCase)
                .filter(s -> s.length() > minLength)
                // ALEX, CHLOE -> A, L, E, X, C, H, L, O, E
                .concatMap(this::splitString_withDelay)
                .log();
    }

    public Flux<String> namesFlux_transform(int minLength) {
        // filter strings whose length is greater than minLength

        Function<Flux<String>, Flux<String>> filterMap = name -> name
                .map(String::toUpperCase)
                .filter(s -> s.length() > minLength);

        return Flux.fromIterable(List.of("alex", "ben", "chloe"))
                .transform(filterMap)
                // ALEX, CHLOE -> A, L, E, X, C, H, L, O, E
                .flatMap(this::splitString)
                .defaultIfEmpty("default")
                .log();
    }

    public Flux<String> namesFlux_transform_switchIfEmpty(int minLength) {
        // filter strings whose length is greater than minLength

        Function<Flux<String>, Flux<String>> filterMap = name -> name
                .map(String::toUpperCase)
                .filter(s -> s.length() > minLength)
                .flatMap(this::splitString);

        Flux<String> defaultFlux = Flux.just("default")
                .transform(filterMap);

        return Flux.fromIterable(List.of("alex", "ben", "chloe"))
                .transform(filterMap)
                .switchIfEmpty(defaultFlux)
                .log();
    }

    // ALEX -> Flux(A, L, E, X)
    private Flux<String> splitString(String s) {
        String[] chars = s.split("");
        return Flux.fromArray(chars);
    }

    private Flux<String> splitString_withDelay(String s) {
        String[] chars = s.split("");
        var delay = new Random().nextInt(1000);
        return Flux.fromArray(chars)
                .delayElements(Duration.ofMillis(delay))
                .log();
    }

    public Mono<String> nameMono() {
        return Mono.just("alex");
    }

    public Flux<String> explore_concat() {
        var abcFlux = Flux.just("A", "B", "C");

        var defFlux = Flux.just("D", "E", "F");

        return Flux.concat(abcFlux, defFlux).log();
    }

    public Flux<String> explore_concatWith() {
        var aMono = Mono.just("A");

        var bMono = Mono.just("B");

        return aMono.concatWith(bMono).log(); // Flux<A, B>
    }

    public Flux<String> explore_merge() {
        var abcFlux = Flux.just("A", "B", "C")
                .delayElements(Duration.ofMillis(100));

        var defFlux = Flux.just("D", "E", "F")
                .delayElements(Duration.ofMillis(125));

        return Flux.merge(abcFlux, defFlux).log();
    }

    public Flux<String> explore_mergeWith() {
        var abcFlux = Flux.just("A", "B", "C")
                .delayElements(Duration.ofMillis(100));

        var defFlux = Flux.just("D", "E", "F")
                .delayElements(Duration.ofMillis(125));

        return abcFlux.mergeWith(defFlux).log();
    }

    public Flux<String> explore_mergeWith_mono() {
        var aMono = Mono.just("A");

        var bMono = Mono.just("B");

        return aMono.mergeWith(bMono).log(); // Flux<A, B>
    }

    public Flux<String> explore_mergeSequential() {
        var abcFlux = Flux.just("A", "B", "C")
                .delayElements(Duration.ofMillis(100));

        var defFlux = Flux.just("D", "E", "F")
                .delayElements(Duration.ofMillis(125));

        return Flux.mergeSequential(abcFlux, defFlux).log();
    }

    public Flux<String> explore_zip() {
        var abcFlux = Flux.just("A", "B", "C");

        var defFlux = Flux.just("D", "E", "F");

        // AD, BE, CF
        // combinator transform tuple of 2 by concatenation
        return Flux.zip(abcFlux, defFlux, (first, second) -> first + second)
                .log();
    }

    public Flux<String> explore_zip_1() {
        var abcFlux = Flux.just("A", "B", "C");

        var defFlux = Flux.just("D", "E", "F");

        var _123Flux = Flux.just("1", "2", "3");

        var _456Flux = Flux.just("4", "5", "6");

        // Flux<(A, D, 1, 4), (B, E, 2, 5), (C, F, 3, 6)>
        return Flux.zip(abcFlux, defFlux, _123Flux, _456Flux)
                // Flux<(AD14), (BE25), (CF36)>
                .map(t4 -> t4.getT1() + t4.getT2() + t4.getT3() + t4.getT4())
                .log();
    }

    public Flux<String> explore_zipWith() {
        var abcFlux = Flux.just("A", "B", "C");

        var defFlux = Flux.just("D", "E", "F");

        // AD, BE, CF
        // combinator transform tuple of 2 by concatenation
        return abcFlux.zipWith(defFlux, (first, second) -> first + second)
                .log();
    }

    public Mono<String> explore_zipWith_mono() {
        var aMono = Mono.just("A");

        var bMono = Mono.just("B");

        return aMono.zipWith(bMono) // Mono<(A, B)>
                .map(t2 -> t2.getT1() + t2.getT2()) // Mono<AB>
                .log();
    }

    public static void main(String[] args) {

        FluxAndMonoGeneratorService generator = new FluxAndMonoGeneratorService();

        generator.namesFlux_map(3)
                .subscribe(name -> System.out.println("Name is: " + name));

        generator.namesFlux_flatmap_async(3)
                .subscribe(System.out::println);

//        Thread.sleep(1000);

        generator.nameMono()
                .subscribe(name -> System.out.println("Mono name is: " + name));
    }
}
