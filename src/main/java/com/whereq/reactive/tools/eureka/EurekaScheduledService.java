package com.whereq.reactive.tools.eureka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.whereq.reactive.tools.shared.ApplicationInstance;
import com.whereq.reactive.tools.shared.ApplicationInstanceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@ConditionalOnProperty(name = "EUREKA_SCHEDULED_SERVICE_ON", havingValue = "1")
public class EurekaScheduledService {

  private static final Logger log = LoggerFactory.getLogger(EurekaScheduledService.class);

  private static final String APPLICATION_JSON_PATH = "/applications/application";
  private static final String STATUS_PAGE_JSON_PATH = "/statusPageUrl";
  private static final String BUILD_VERSION_JSON_PATH = "/build/version";

  @Autowired WebClient webClient;

  @Autowired ApplicationWebClient applicationWebClient;

  @Autowired
  ApplicationInstanceRepository applicationInstanceRepository;

  @Autowired ApplicationService applicationService;

  @Scheduled(fixedDelayString = "${whereq.reactive.eureka.service.schedule.fixedDelay:10000}")
  public void fetchEurekaApplicationInfo() {

    Flux<?> job =
        this.getEurekaServerList()
            .flatMap(this::getApplicationJson)
            .map(this::getApplicationInstancePerServer)
            .collectList()
            .map(this::flattenNestedList)
            .flatMapMany(Flux::fromIterable)
            .flatMap(this::updateApplicationInstanceStatus)
            .collectList()
            .flatMapMany(this::refreshApplicationInstance);

    job.subscribe();
  }

  private Flux<ApplicationInstance> getEurekaServerList() {
    return applicationInstanceRepository.findByApp("eureka");
  }

  private Mono<ApplicationInstance> getApplicationJson(ApplicationInstance applicationInstance) {
    return applicationService
        .retrieveApplication(applicationInstance.getServer(), applicationInstance.getUri())
        .map(
            applicationInstanceJson -> {
              applicationInstance.setInstanceJson(applicationInstanceJson);
              return applicationInstance;
            });
  }

  private List<ApplicationInstance> getApplicationInstancePerServer(
      ApplicationInstance applicationInstance) {
    try {
      return ApplicationUtils.jsonToApplicationInstanceList(
              applicationInstance.getServer(),
              applicationInstance.getInstanceJson(),
              APPLICATION_JSON_PATH);
    } catch (JsonProcessingException e) {
      log.error("Error occurred when fetching from ", applicationInstance.getUri());
      return Collections.emptyList();
    }
  }

  private List<ApplicationInstance> flattenNestedList(
      List<List<ApplicationInstance>> applicationInstanceList) {
    return applicationInstanceList.stream()
        .flatMap(Collection::stream)
        .collect(Collectors.toList());
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
        log.error("Error occurred when fetching from ", applicationInstance.getStatusUri());
        return Mono.just(applicationInstance);
      });
    } catch (JsonProcessingException e) {
      log.error("Error occurred when fetching from ", applicationInstance.getStatusUri());
    }
    return Mono.empty();
  }

  private Flux<ApplicationInstance> refreshApplicationInstance(
      List<ApplicationInstance> applicationInstanceList) {
    return applicationInstanceRepository
        .deleteNonEurekaApplicationInstance()
        .thenMany(applicationInstanceRepository.saveAll(applicationInstanceList));
  }
}
