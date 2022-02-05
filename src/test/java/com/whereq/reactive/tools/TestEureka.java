package com.whereq.reactive.tools;

import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

public class TestEureka {
    public Flux<String> getEurekaServers() {
        // return repository.findEurekaServers();

        String[] eurekas = {"eureka-dev", "eureka-test",  "eureka-prod"};
        return Flux
                .just(eurekas)
                .delaySequence(Duration.ofSeconds(1))
                .log();
    }

    public Flux<String> getAppInstance(String eurekaServer) {
        // return webclient.fetch(eurekaServer).parseJson().returnInstances();
        
        
        String[] services = {"bundle", "common",  "profile"};
        List<String> instance = Stream.of(services).map(service-> service + " " + eurekaServer).collect(Collectors.toList());
        return Flux
                .fromIterable(instance)
                .delaySequence(Duration.ofSeconds(1))
                .log();
    }

    public Mono<String> saveInstances(List<String> instances) {
        // save instance to database
        System.out.println("save instances:" + instances);
        return Mono.just("saved to db").log();        
    }


    @Test
    public void reactor() throws Exception{
        Mono<?> job  = 
            this.getEurekaServers()
            .flatMap(this::getAppInstance)
            .collectList()
            .flatMap(this::saveInstances);

        StepVerifier
            .create(job.then())
            .expectSubscription()
            .verifyComplete();
    }

    public static void main(String args[]) throws Exception{
        System.out.println("test");

        TestEureka test = new TestEureka();
        test.reactor();

        
    }
}
