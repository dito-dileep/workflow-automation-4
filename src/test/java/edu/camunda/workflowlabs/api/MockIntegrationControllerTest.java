package edu.camunda.workflowlabs.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

class MockIntegrationControllerTest {
  private final MockIntegrationController controller = new MockIntegrationController();

  @Test
  void calculatesLoanDecisionFromEachRequest() {
    assertEquals("Approved", controller.loanDecision(Map.of(
        "monthlyIncome", 60000, "requestedAmount", 300000, "creditScore", 720)).get("loanDecision"));
    assertEquals("Manual Review", controller.loanDecision(Map.of(
        "monthlyIncome", 35000, "requestedAmount", 600000, "creditScore", 640)).get("loanDecision"));
    assertEquals("Rejected", controller.loanDecision(Map.of(
        "monthlyIncome", 25000, "requestedAmount", 900000, "creditScore", 550)).get("loanDecision"));
  }

  @Test
  void calculatesOrderPriorityFromEachRequest() {
    assertEquals("P1", controller.orderPriority(Map.of(
        "orderTotal", 6000, "customerCategory", "GOLD", "deliveryPreference", "EXPRESS"))
        .get("deliveryPriority"));
    assertEquals("P2", controller.orderPriority(Map.of(
        "orderTotal", 2500, "customerCategory", "STANDARD", "deliveryPreference", "EXPRESS"))
        .get("deliveryPriority"));
    assertEquals("P3", controller.orderPriority(Map.of(
        "orderTotal", 1000, "customerCategory", "STANDARD", "deliveryPreference", "STANDARD"))
        .get("deliveryPriority"));
  }

  @Test
  void calculatesTriageCategoryFromUrgencyIndicator() {
    assertEquals("Urgent Assessment", controller.triageDecision(Map.of(
        "urgencyIndicator", "RED", "symptomsSummary", "Needs review")).get("triageCategory"));
    assertEquals("Priority Review", controller.triageDecision(Map.of(
        "urgencyIndicator", "AMBER", "symptomsSummary", "Follow-up")).get("triageCategory"));
    assertEquals("Routine", controller.triageDecision(Map.of(
        "urgencyIndicator", "GREEN", "symptomsSummary", "Routine follow-up")).get("triageCategory"));
  }

  @Test
  void rejectsMissingDecisionInputs() {
    assertThrows(ResponseStatusException.class, () -> controller.loanDecision(Map.of(
        "monthlyIncome", 60000, "creditScore", 720)));
    assertThrows(ResponseStatusException.class, () -> controller.orderPriority(Map.of(
        "orderTotal", 1000, "customerCategory", "STANDARD")));
    assertThrows(ResponseStatusException.class, () -> controller.triageDecision(Map.of(
        "urgencyIndicator", "GREEN")));
  }
}
