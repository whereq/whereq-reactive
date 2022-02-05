package com.whereq.reactive.tools.eureka;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.whereq.reactive.tools.shared.Application;
import com.whereq.reactive.tools.shared.ApplicationInstanceRepository;
import com.whereq.reactive.tools.shared.Instance;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;
import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES;

@SpringBootTest
public class ApplicationUtilsTest {

    private static ObjectMapper objectMapper = new ObjectMapper();

    static {
        objectMapper.setSerializationInclusion(NON_NULL);
        objectMapper.configure(FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @Autowired
    ApplicationInstanceRepository applicationInstanceRepository;

    @Test
    public void testSaveApplicationInstance() throws Exception {

    }

    @Test
    public void testApplicationInstance() throws Exception {
        Path path = Paths.get("src/test/resources/application_content.json");
        String jsonString = new String(Files.readAllBytes(path));
        String jsonPath =  "/applications/application";

        JsonNode root = objectMapper.readTree(jsonString);
        JsonNode applicationNodeArray = root.at(jsonPath);

        List<Application> applicationList = new ArrayList<>();
        if (JsonNodeType.ARRAY.equals(applicationNodeArray.getNodeType())) {
            for (JsonNode applicationNode : applicationNodeArray) {
                Application application = new Application();
                application.setName(applicationNode.get("name").asText());

                JsonNode instanceNodeArray = applicationNode.get("instance");
                List<Instance> instanceList = new ArrayList<>();
                if (JsonNodeType.ARRAY.equals(instanceNodeArray.getNodeType())) {
                    int instanceNumber = 0;
                    for (JsonNode instanceNode : instanceNodeArray) {
                        Instance instance = new Instance();
                        instance.setInstanceNumber(instanceNumber++);
                        instance.setInstanceId(instanceNode.get("instanceId").asText());
                        instance.setHostName(instanceNode.get("hostName").asText());
                        instance.setStatus(instanceNode.get("status").asText());
                        instance.setInstanceJson(instanceNode.toString());
                        instanceList.add(instance);
                    }
                }
                application.setInstanceList(instanceList);
                applicationList.add(application);
            }
        }

        System.out.println(applicationList);

    }
}
