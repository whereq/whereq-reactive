package com.whereq.reactive.tools.eureka;

import net.jodah.concurrentunit.Waiter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;


@SpringBootTest
public class ApplicationWebClientTest {


    @Autowired
    WebClient webClient;

    @Autowired
    ApplicationWebClient applicationWebClient;

    @Test
    public void testRetrieveApplicationInfo() throws Exception {
        String eurekaUri = "https://registryservicedev.app.paas.whereq.com/eureka/apps/";

        System.out.println("retrieve from " + eurekaUri);
        final Waiter waiter = new Waiter();

        Mono<String> monoResponse = applicationWebClient.retrieveApplicationInfo(eurekaUri, webClient);

        monoResponse.subscribe(resp -> {
            System.out.println(resp);
            waiter.resume();
        });

        waiter.await(30000, 0);
        System.out.println("done");
    }

}
