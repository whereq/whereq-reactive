package com.whereq.reactive.tools.shared;

import org.springframework.data.domain.Example;
import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ApplicationInstanceRepository extends ReactiveCrudRepository<ApplicationInstance, Long> {

    Flux<ApplicationInstance> findByApp(String app);
    Flux<ApplicationInstance> findByAppNot(String app);

    @Query("DELETE FROM application_instance WHERE app != 'eureka'")
    Mono<Integer> deleteNonEurekaApplicationInstance();

    Flux<ApplicationInstance> findAllBy(Pageable pageable);

    Flux<ApplicationInstance> findAllBy(Example<ApplicationInstance> example, Pageable pageable);

}
