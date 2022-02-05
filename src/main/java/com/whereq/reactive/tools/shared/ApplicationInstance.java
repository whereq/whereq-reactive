package com.whereq.reactive.tools.shared;

import lombok.Data;
import org.springframework.data.annotation.Id;

@Data
public class ApplicationInstance {
    @Id
    private Long id;

    private String server;

    private String app;

    private Integer instance = 0;

    private String uri;

    private String status;

    private String buildVersion;

    private String instanceJson;

    private String statusUri;

    private String statusJson;
}
