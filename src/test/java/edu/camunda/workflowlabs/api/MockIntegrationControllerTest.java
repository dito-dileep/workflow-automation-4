package edu.camunda.workflowlabs.api;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Map;
import org.junit.jupiter.api.Test;

class MockIntegrationControllerTest {
  private final MockIntegrationController controller = new MockIntegrationController();

  @Test
  void validatesLoanInputsForBpmnRouting() {
    assertEquals(true, controller.validateLoan(Map.of(
        "applicantId", "APP-1", "monthlyIncome", 60000, "requestedAmount", 300000))
        .get("applicationValid"));
    assertEquals(false, controller.validateLoan(Map.of(
        "monthlyIncome", 60000, "requestedAmount", 300000))
        .get("applicationValid"));
  }

  @Test
  void validatesOrderAndCalculatesTotalAndStock() {
    Map<String, Object> standard = controller.validateOrder(Map.of(
        "customerRef", "CUST-1", "productId", "SKU-1", "quantity", 2, "unitPrice", 500));
    assertEquals(true, standard.get("orderValid"));
    assertEquals(true, standard.get("stockAvailable"));
    assertEquals(0, new java.math.BigDecimal("1000").compareTo(
        (java.math.BigDecimal) standard.get("orderTotal")));

    Map<String, Object> outOfStock = controller.validateOrder(Map.of(
        "customerRef", "CUST-2", "productId", "SKU-2", "quantity", 25, "unitPrice", 100));
    assertEquals(true, outOfStock.get("orderValid"));
    assertEquals(false, outOfStock.get("stockAvailable"));
  }

  @Test
  void invalidOrderDoesNotProduceAValidTotal() {
    Map<String, Object> result = controller.validateOrder(Map.of(
        "customerRef", "CUST-3", "quantity", 0, "unitPrice", 500));
    assertEquals(false, result.get("orderValid"));
    assertEquals(false, result.get("stockAvailable"));
    assertEquals(0, java.math.BigDecimal.ZERO.compareTo(
        (java.math.BigDecimal) result.get("orderTotal")));
  }

  @Test
  void validatesRegistrationInputsForBpmnRouting() {
    assertEquals(true, controller.validateRegistration(Map.of(
        "patientRef", "PAT-1",
        "appointmentPreference", "MORNING",
        "symptomsSummary", "Routine follow-up",
        "urgencyIndicator", "GREEN")).get("registrationValid"));
    assertEquals(false, controller.validateRegistration(Map.of(
        "patientRef", "PAT-2", "urgencyIndicator", "GREEN")).get("registrationValid"));
  }
}
