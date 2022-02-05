package com.whereq.reactive.tools.eureka;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class ApplicationWebClient {

    private static final Logger log = LoggerFactory.getLogger(ApplicationWebClient.class);

    public Mono<String> retrieveApplicationInfo(String eurekaUri, WebClient webClient) {
        return webClient.get().uri(eurekaUri)
                .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .retrieve().onStatus(HttpStatus::is4xxClientError, response -> {
            log.error("4xx error occurred");
            return response.createException();
        }).bodyToMono(String.class).onErrorMap(response -> {
            log.error("error occurred", response);
            return new RuntimeException(response.getCause());
        });
    }
}
