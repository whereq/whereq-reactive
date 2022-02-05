package com.whereq.reactive.tools.eureka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.whereq.reactive.tools.shared.ApplicationInstance;
import com.whereq.reactive.tools.shared.ApplicationInstanceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@SpringBootTest
public class EurekaServiceTest {

    private static final String APPLICATION_JSON_PATH = "/applications/application";
    private static final String STATUS_PAGE_JSON_PATH = "/statusPageUrl";
    private static final String BUILD_VERSION_JSON_PATH = "/build/version";

    @Autowired
    ApplicationInstanceRepository applicationInstanceRepository;

    @Autowired
    WebClient webClient;

    @Autowired ApplicationService applicationService;

    @Autowired
    ApplicationWebClient applicationWebClient;

    private Flux<ApplicationInstance> getEurekaServerList() {
        return applicationInstanceRepository.findByApp("eureka");
    }


    private Mono<ApplicationInstance> getApplicationJson(ApplicationInstance applicationInstance) {
        return applicationService.retrieveApplication(applicationInstance.getServer(), applicationInstance.getUri()).map(applicationInstanceJson -> {
            applicationInstance.setInstanceJson(applicationInstanceJson);
            return applicationInstance;
        });
    }

    private List<ApplicationInstance> getApplicationInstancePerServer(ApplicationInstance applicationInstance) {
        try {
            return ApplicationUtils.jsonToApplicationInstanceList(applicationInstance.getServer(), applicationInstance.getInstanceJson(), APPLICATION_JSON_PATH);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    private List<ApplicationInstance> flattenNestedList(List<List<ApplicationInstance>> applicationInstanceList) {
        return applicationInstanceList.stream().flatMap(Collection::stream).collect(Collectors.toList());
    }

    private Mono<ApplicationInstance> updateApplicationInstanceStatus(ApplicationInstance applicationInstance) {
        try {
            String statusUri = ApplicationUtils.getStatusUri(applicationInstance.getInstanceJson(), STATUS_PAGE_JSON_PATH);
            applicationInstance.setStatusUri(statusUri);
            Mono<String> statusMonoResponse = applicationWebClient.retrieveApplicationInfo(statusUri, webClient);
            return statusMonoResponse.flatMap(statusResponse -> {
                try {
                    String buildVersion = ApplicationUtils.getNodeValue(statusResponse, BUILD_VERSION_JSON_PATH);
                    applicationInstance.setBuildVersion(buildVersion);
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }
                applicationInstance.setStatusJson(statusResponse);
                return Mono.just(applicationInstance);
            }).onErrorResume(error -> {
                error.printStackTrace();
                return Mono.just(applicationInstance);
            });
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return Mono.empty();
    }

    private Flux<ApplicationInstance> refreshApplicationInstance(List<ApplicationInstance> applicationInstanceList) {
//        return applicationInstanceRepository.deleteNonEurekaApplicationInstance().thenMany(applicationInstanceRepository.saveAll(applicationInstanceList));
        return Flux.fromIterable(applicationInstanceList);
    }


//    @Test
    public void reactor() throws Exception{
        Flux<?> job  =
                this.getEurekaServerList()
                        .flatMap(this::getApplicationJson)
                        .map(this::getApplicationInstancePerServer)
                        .collectList()
                        .map(this::flattenNestedList)
                        .flatMapMany(Flux::fromIterable)
                        .flatMap(this::updateApplicationInstanceStatus)
                        .collectList()
                        .flatMapMany(this::refreshApplicationInstance)
                        .log();

        StepVerifier
                .create(job.then())
                .expectSubscription()
                .verifyComplete();
    }

    public static void main(String args[]) throws Exception{
        System.out.println("test");

        EurekaServiceTest test = new EurekaServiceTest();
        test.reactor();

        
    }
}
