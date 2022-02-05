package com.whereq.reactive;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.r2dbc.connection.init.ConnectionFactoryInitializer;


import io.r2dbc.spi.ConnectionFactory;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.reactive.config.EnableWebFlux;
import org.springframework.web.reactive.function.client.WebClient;

import com.whereq.reactive.tools.shared.ApplicationInstanceConfigurationProperties;
import com.whereq.reactive.tools.shared.ApplicationInstanceRepository;


@EnableWebFlux
@EnableR2dbcRepositories
@EnableScheduling
@SpringBootApplication
public class WhereqReactiveApplication {

    @Autowired
    ApplicationInstanceConfigurationProperties config;

	private static final Logger log = LoggerFactory.getLogger(WhereqReactiveApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(WhereqReactiveApplication.class, args);
	}
	
	@Bean
    public CommandLineRunner initApplication(ApplicationInstanceRepository applicationInstanceRepository) {
		return args -> {
            log.info("init application ..." + this.config.getApplicationInstanceList());

			// save config to database
            applicationInstanceRepository.saveAll(this.config.getApplicationInstanceList()).blockLast();
		};
	}

    @Bean
    public RouteLocator routes(RouteLocatorBuilder builder) {
        final RouteLocatorBuilder.Builder routes = builder.routes();
        final String baseUrl = "/api/{id}/";

        // add API routes for eureka servers
        this.config.getApplicationInstanceList().forEach(app -> {
            if("eureka".equals(app.getApp())) {
                final String prefixPath = baseUrl.replace("{id}", "eureka-" + app.getServer());
                final int slash = (int)prefixPath.chars().filter(ch -> ch == '/').count() - 1;
                
                routes.route(prefixPath, r -> 
                    r.path(prefixPath + "**")
                    .filters(f -> f.stripPrefix(slash))
                    .uri(app.getUri()));

                log.info("route:{}->{}", prefixPath, app.getUri());
            }
        });
        
        return routes.build();
    }

	@Bean
	ConnectionFactoryInitializer initializer(ConnectionFactory connectionFactory) {

		ConnectionFactoryInitializer initializer = new ConnectionFactoryInitializer();
		initializer.setConnectionFactory(connectionFactory);
//		initializer.setDatabasePopulator(new ResourceDatabasePopulator(new ClassPathResource("schema.sql")));

		return initializer;
	}

	@Bean
    WebClient webClient() {
	    return WebClient.builder().defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE).build();
    }
}
