package com.whereq.reactive.tools.eureka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.whereq.reactive.tools.shared.Application;
import com.whereq.reactive.tools.shared.ApplicationInstance;
import com.whereq.reactive.tools.shared.Instance;

import java.util.ArrayList;
import java.util.List;

public class ApplicationUtils {

  private static final ObjectMapper objectMapper = new ObjectMapper();

  public static <T> T jsonToObject(String jsonString, String jsonPath, Class<T> type)
      throws Exception {
    T object = null;
    JsonNode root = objectMapper.readTree(jsonString);
    JsonNode jsonNode = root.at(jsonPath);
    object = objectMapper.treeToValue(jsonNode, type);
    return object;
  }

  public static String getNodeValue(String jsonString, String jsonPath) throws JsonProcessingException {
    JsonNode root = objectMapper.readTree(jsonString);
    return root.at(jsonPath).asText();
  }

  public static List<Application> jsonToApplicationList(String jsonString, String jsonPath) throws JsonProcessingException {
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
    return applicationList;
  }

  public static List<ApplicationInstance> jsonToApplicationInstanceList(String env, String jsonString, String jsonPath) throws JsonProcessingException {
    List<Application> applicationList = jsonToApplicationList(jsonString, jsonPath);
    List<ApplicationInstance> applicationInstanceList = new ArrayList<>();

    for (Application application : applicationList) {
      List<Instance> instanceList = application.getInstanceList();

      for (Instance instance : instanceList) {
        ApplicationInstance applicationInstance = new ApplicationInstance();
        applicationInstance.setServer(env);
        applicationInstance.setApp(application.getName());
        applicationInstance.setInstance(instance.getInstanceNumber());
        applicationInstance.setUri(instance.getHostName());
        applicationInstance.setStatus(instance.getStatus());
        applicationInstance.setInstanceJson(instance.getInstanceJson());
        applicationInstanceList.add(applicationInstance);
      }
    }
    return applicationInstanceList;
  }

  public static String getStatusUri(String instanceJsonString, String jsonPath) throws JsonProcessingException {
    JsonNode root = objectMapper.readTree(instanceJsonString);
    JsonNode statusUriNode = root.at(jsonPath);
    return statusUriNode.asText();
  }
}
