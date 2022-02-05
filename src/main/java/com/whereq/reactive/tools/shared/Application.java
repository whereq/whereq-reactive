package com.whereq.reactive.tools.shared;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class Application {
    private String name;

    @JsonProperty("instance")
    private List<Instance> instanceList;
}
