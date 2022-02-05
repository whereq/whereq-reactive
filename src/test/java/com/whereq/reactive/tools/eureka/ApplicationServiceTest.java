package com.whereq.reactive.tools.eureka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.whereq.reactive.tools.shared.ApplicationInstance;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@SpringBootTest
public class ApplicationServiceTest {

    private static final String APPLICATION_JSON_PATH = "/applications/application";

    @Autowired
    WebClient webClient;

    @Autowired
    ApplicationWebClient applicationWebClient;

    @Autowired
    ApplicationService applicationService;

    @Test
    public void testRetrieveApplicationInfo() throws Exception {
        String eurekaUri = "https://registryservicedev.app.whereq.com/eureka/apps/";
        Mono<String> applicationMonoResponse = applicationService.retrieveApplication("dev", eurekaUri);
        applicationMonoResponse.flatMap(
                applicationResponse -> {
                    try {
                        List<ApplicationInstance> applicationInstanceList =
                                ApplicationUtils.jsonToApplicationInstanceList("dev", applicationResponse, APPLICATION_JSON_PATH);

                        for (ApplicationInstance applicationInstance : applicationInstanceList) {
                            String statusUri = ApplicationUtils.getStatusUri(applicationInstance.getInstanceJson(), "/statusPageUrl");
                            Mono<String> statusMonoResponse = applicationWebClient.retrieveApplicationInfo(statusUri, webClient);
                        }

                        return Mono.empty();

                    } catch (JsonProcessingException e) {
                        e.printStackTrace();
                        return Mono.empty();
                    }
                }
        );
        applicationMonoResponse.subscribe(response -> {
            Flux<ApplicationInstance> applicationInstanceFlux = onResponse("dev", response);
            if (applicationInstanceFlux != null) {
                applicationInstanceFlux.subscribe();
                Mono<List<ApplicationInstance>> applicationInstanceMonoList = applicationInstanceFlux.collectList();
                applicationInstanceMonoList.subscribe();
            }
        });
    }

    private Mono<String> processStatusMonoResponse(Mono<String> statusMonoResponse) {
        return statusMonoResponse.flatMap(statusResponse -> {
            System.out.println(statusResponse);
            return Mono.just(statusResponse);
        }).onErrorResume(error -> {
            error.printStackTrace();
            System.out.println(error.getMessage());
            return Mono.just(error.getMessage());
        });
    }

    private Flux<ApplicationInstance> onResponse(String env, String response) {
//        Flux.fromIterable(ApplicationUtils.jsonToApplicationInstanceList(env, response, APPLICATION_JSON_PATH))
        try {
            List<ApplicationInstance> applicationInstanceList =
                    ApplicationUtils.jsonToApplicationInstanceList(env, response, APPLICATION_JSON_PATH);
            for (ApplicationInstance applicationInstance : applicationInstanceList) {
                String statusUri = ApplicationUtils.getStatusUri(applicationInstance.getInstanceJson(), "/statusPageUrl");
                Mono<String> statusMonoResponse = applicationWebClient.retrieveApplicationInfo(statusUri, webClient);
                statusMonoResponse.flatMap(statusResponse -> {
                    System.out.println(statusResponse);
                    return Mono.just(statusResponse);
                }).onErrorResume(error -> {
                    error.printStackTrace();
                    System.out.println(error.getMessage());
                    return Mono.just(error.getMessage());
                }).subscribe();
            }
//            return applicationInstanceRepository.saveAll(applicationInstanceList);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return null;
    }
}
