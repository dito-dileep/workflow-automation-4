package edu.camunda.workflowlabs;

import io.camunda.zeebe.spring.client.annotation.Deployment;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@Deployment(resources = {
    "classpath*:models/*.bpmn",
    "classpath*:models/*.dmn"
})
public class WorkflowLabsApplication {
  public static void main(String[] args) {
    SpringApplication.run(WorkflowLabsApplication.class, args);
  }
}
