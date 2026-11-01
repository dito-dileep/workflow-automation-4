package edu.camunda.workflowlabs.worker;

import io.camunda.client.api.response.ActivatedJob;
import io.camunda.spring.client.annotation.JobWorker;
import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.stereotype.Component;

/** Educational workers: deterministic, synthetic data only; no credentials or real patient/payment data. */
@Component
public class LabWorkers {
  @JobWorker(type = "validate-loan-application")
  public Map<String, Object> validateLoan(ActivatedJob job) {
    var v = job.getVariablesAsMap();
    boolean valid = present(v, "applicantId") && number(v, "monthlyIncome").compareTo(BigDecimal.ZERO) > 0
        && number(v, "requestedAmount").compareTo(BigDecimal.ZERO) > 0;
    return Map.of("applicationValid", valid, "validationNote", valid ? "Synthetic validation passed" : "Required loan input missing or invalid");
  }

  @JobWorker(type = "validate-order-and-stock")
  public Map<String, Object> validateOrderAndStock(ActivatedJob job) {
    var v = job.getVariablesAsMap();
    int quantity = number(v, "quantity").intValue();
    BigDecimal unitPrice = number(v, "unitPrice");
    boolean available = quantity > 0 && quantity <= 20 && present(v, "productId");
    return Map.of("orderValid", present(v, "customerRef") && quantity > 0,
        "stockAvailable", available, "orderTotal", unitPrice.multiply(BigDecimal.valueOf(quantity)),
        "inventoryNote", available ? "Synthetic stock reserved" : "Synthetic stock unavailable");
  }

  @JobWorker(type = "validate-registration-and-availability")
  public Map<String, Object> validateRegistration(ActivatedJob job) {
    var v = job.getVariablesAsMap();
    boolean valid = present(v, "patientRef") && present(v, "appointmentPreference");
    boolean slots = !"none".equalsIgnoreCase(String.valueOf(v.getOrDefault("slotMode", "available")));
    Map<String, Object> result = new LinkedHashMap<>();
    result.put("registrationValid", valid);
    result.put("slotAvailable", slots);
    result.put("availableSlot", slots ? "2026-11-10T10:30:00+05:30" : null);
    result.put("registrationNote", valid ? "Synthetic registration validated" : "Patient reference or preference missing");
    return result;
  }

  private boolean present(Map<String, Object> values, String name) {
    Object value = values.get(name);
    return value != null && !String.valueOf(value).isBlank();
  }
  private BigDecimal number(Map<String, Object> values, String name) {
    Object value = values.getOrDefault(name, 0);
    return new BigDecimal(String.valueOf(value));
  }
}
