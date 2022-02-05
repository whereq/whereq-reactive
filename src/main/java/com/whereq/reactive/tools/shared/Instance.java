package com.whereq.reactive.tools.shared;

import org.h2.tools.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@ConditionalOnProperty(name = "whereq.reactive.h2-console-port")
public class H2Console {
    private Server server;

    @Value("${whereq.reactive.h2-console-port}")
    private Integer port;

    @EventListener(ContextRefreshedEvent.class)
    public void start() throws java.sql.SQLException {
    	log.info("starting h2 console at port {}", port);
        this.server = Server.createWebServer("-webPort", port.toString(), "-tcpAllowOthers").start();
    }

    @EventListener(ContextClosedEvent.class)
    public void stop() {
        if(this.server != null) {
            log.info("stopping h2 console at port {}", port);
            this.server.stop();
        }
    }

}
