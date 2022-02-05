package com.whereq.reactive.tools.eureka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.whereq.reactive.tools.shared.ApplicationInstance;
import com.whereq.reactive.tools.shared.ApplicationInstanceRepository;
import com.whereq.reactive.tools.shared.GenericDao;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
public class ApplicationService {

    private static final String APPLICATION_JSON_PATH = "/applications/application";

    @Autowired
    WebClient webClient;

    @Autowired
    ApplicationWebClient applicationWebClient;

    @Autowired
    ApplicationInstanceRepository applicationInstanceRepository;

    @Autowired
    GenericDao genericDao;

    public Mono<String> retrieveApplication(String env, String eurekaUri) {

        String eurekaAppUrl = "eureka/apps/";
        Mono<String> monoResponse = applicationWebClient.retrieveApplicationInfo(eurekaUri + eurekaAppUrl, webClient);
        return monoResponse;
    }

    public Mono<Page<ApplicationInstance>> getApplicationInstance(PageRequest pageRequest) {
        return this.applicationInstanceRepository.findAllBy(pageRequest.withSort(Sort.by("app")))
                .collectList()
                .zipWith(this.applicationInstanceRepository.count())
                .map(t -> new PageImpl<>(t.getT1(), pageRequest, t.getT2()));
    }

    public Mono<Page<ApplicationInstance>> getApplicationInstance(
            PageRequest pageRequest, ApplicationInstance applicationInstance) {
        Example<ApplicationInstance> applicationInstanceExample = Example.of(applicationInstance, ExampleMatcher.matchingAll());
        ExampleMatcher exampleMatcher = ExampleMatcher.matchingAll();
        if (StringUtils.isNotBlank(applicationInstance.getServer())) {
            exampleMatcher.withMatcher("server", ExampleMatcher.GenericPropertyMatchers.exact());
        }
        if (StringUtils.isNotBlank(applicationInstance.getApp())) {
            exampleMatcher.withMatcher("app", ExampleMatcher.GenericPropertyMatchers.exact());
        }
        if (StringUtils.isNotBlank(applicationInstance.getStatus())) {
            exampleMatcher.withMatcher("status", ExampleMatcher.GenericPropertyMatchers.ignoreCase());
        }
        return this.applicationInstanceRepository
                .findAllBy(Example.of(applicationInstance, exampleMatcher), pageRequest.withSort(Sort.by("app")))
                .collectList()
                .zipWith(this.applicationInstanceRepository.count())
                .map(t -> new PageImpl<>(t.getT1(), pageRequest, t.getT2()));

    }

    public Flux<ApplicationInstance> findApplicationInstance(PageRequest pageRequest, ApplicationInstance applicationInstance) {
        return this.genericDao.findApplicationInstance(pageRequest, applicationInstance);
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
