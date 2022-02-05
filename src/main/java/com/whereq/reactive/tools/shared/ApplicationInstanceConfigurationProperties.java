package com.whereq.reactive.tools.shared;

import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Data;

@Component
@Data
@ConfigurationProperties(prefix = "whereq.reactive")
public class ApplicationInstanceConfigurationProperties {
    private List<ApplicationInstance> applicationInstanceList;
}

