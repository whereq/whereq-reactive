package com.whereq.reactive.tools.shared;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.core.publisher.Flux;


@SpringBootTest
public class ApplicationInstanceRepositoryTest {

    @Autowired
    GenericDao genericDao;

    @Autowired
    ApplicationInstanceRepository applicationInstanceRepository;

    @Test
    public void testFindAllApplicationInstance() {
        Flux<ApplicationInstance> applicationInstanceFlux = genericDao.findAllApplicationInstance();
        applicationInstanceFlux.subscribe(applicationInstance -> {
            System.out.println(applicationInstance);
        });
    }
    @Test
    public void testLoadApplicationInstance() {
//       Flux<ApplicationInstance> applicationInstanceFlux = applicationInstanceRepository.findAll();
       Flux<ApplicationInstance> applicationInstanceFlux = applicationInstanceRepository.findByApp("eureka");
       applicationInstanceFlux.subscribe(applicationInstance -> {
           System.out.println(applicationInstance);
       });
    }

    @Test
    public void testDelete() {
        Flux<ApplicationInstance> applicationInstanceFlux = applicationInstanceRepository.findByAppNot("eureka");
        applicationInstanceFlux.subscribe(applicationInstance -> {
            System.out.println(applicationInstance);
        });
    }
}
