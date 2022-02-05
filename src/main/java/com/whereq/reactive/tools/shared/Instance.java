package com.whereq.reactive.tools.shared;

import lombok.Data;
import org.springframework.data.annotation.Id;

@Data
public class Instance {

    private int instanceNumber;

    private String instanceId;

    private String hostName;

    private String status;

    private String instanceJson;
}
