package edu.camunda.workflowlabs.api;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/**
 * Synthetic integrations for the three labs. A service can be switched off from the browser,
 * which is the reliable way to show an Operate incident and then recover it with Retry.
 */
@RestController
@RequestMapping("/mock")
public class MockIntegrationController {
  private final AtomicBoolean creditDown = new AtomicBoolean(false);
  private final AtomicBoolean paymentDown = new AtomicBoolean(false);
  private final AtomicBoolean schedulingDown = new AtomicBoolean(false);

  @GetMapping("/status")
  public Map<String, Object> status() {
    Map<String, Object> status = new LinkedHashMap<>();
    status.put("creditDown", creditDown.get());
    status.put("paymentDown", paymentDown.get());
    status.put("schedulingDown", schedulingDown.get());
    return status;
  }

  /** Example: /mock/controls/credit?down=true then later ?down=false before retrying the job. */
  @GetMapping("/controls/{service}")
  public Map<String, Object> control(@PathVariable String service, @RequestParam boolean down) {
    switch (service) {
      case "credit" -> creditDown.set(down);
      case "payment" -> paymentDown.set(down);
      case "scheduling" -> schedulingDown.set(down);
      default -> throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Unknown service: " + service);
    }
    return Map.of("service", service, "down", down);
  }

  @PostMapping("/credit-assessments")
  public ResponseEntity<?> credit(@RequestBody Map<String, Object> request) {
    if (creditDown.get() || Boolean.TRUE.equals(request.get("simulateFailure"))) return failure();
    int score = Integer.parseInt(String.valueOf(request.getOrDefault("syntheticCreditScore", 700)));
    return ResponseEntity.ok(Map.of("creditScore", score, "creditStatus", score >= 650 ? "CLEAR" : "REFER"));
  }

  @PostMapping("/decisions/loan")
  public Map<String, Object> loanDecision(@RequestBody Map<String, Object> request) {
    BigDecimal income = positiveAmount(request, "monthlyIncome");
    BigDecimal amount = positiveAmount(request, "requestedAmount");
    int score = integer(request, "creditScore");
    if (score < 300 || score > 850) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "creditScore must be between 300 and 850");
    }

    String decision;
    String reason;
    if (income.compareTo(new BigDecimal("50000")) >= 0
        && amount.compareTo(new BigDecimal("500000")) <= 0 && score >= 700) {
      decision = "Approved";
      reason = "Income, requested amount, and credit score meet the approval thresholds.";
    } else if (income.compareTo(new BigDecimal("30000")) >= 0
        && amount.compareTo(new BigDecimal("750000")) <= 0 && score >= 600) {
      decision = "Manual Review";
      reason = "The application meets the review thresholds but not all approval thresholds.";
    } else {
      decision = "Rejected";
      reason = "The application does not meet the minimum review thresholds.";
    }
    return Map.of("loanDecision", decision, "decisionReason", reason);
  }

  @PostMapping("/decisions/order-priority")
  public Map<String, Object> orderPriority(@RequestBody Map<String, Object> request) {
    BigDecimal total = positiveAmount(request, "orderTotal");
    String customerCategory = requiredText(request, "customerCategory");
    String deliveryPreference = requiredText(request, "deliveryPreference");

    String priority;
    if (total.compareTo(new BigDecimal("5000")) >= 0
        && "GOLD".equalsIgnoreCase(customerCategory)
        && "EXPRESS".equalsIgnoreCase(deliveryPreference)) {
      priority = "P1";
    } else if (total.compareTo(new BigDecimal("2000")) >= 0
        && "EXPRESS".equalsIgnoreCase(deliveryPreference)) {
      priority = "P2";
    } else {
      priority = "P3";
    }
    return Map.of("deliveryPriority", priority);
  }

  @PostMapping("/decisions/triage")
  public Map<String, Object> triageDecision(@RequestBody Map<String, Object> request) {
    String urgency = requiredText(request, "urgencyIndicator");
    requiredText(request, "symptomsSummary");
    String category = switch (urgency.toUpperCase()) {
      case "RED" -> "Urgent Assessment";
      case "AMBER" -> "Priority Review";
      default -> "Routine";
    };
    return Map.of("triageCategory", category);
  }

  @PostMapping("/payments")
  public ResponseEntity<?> payment(@RequestBody Map<String, Object> request) {
    if (paymentDown.get() || Boolean.TRUE.equals(request.get("simulateFailure"))) return failure();
    boolean approved = !"DECLINE".equalsIgnoreCase(String.valueOf(request.getOrDefault("paymentToken", "OK")));
    return ResponseEntity.ok(Map.of("paymentStatus", approved ? "APPROVED" : "DECLINED", "transactionRef", "MOCK-" + System.currentTimeMillis()));
  }

  @PostMapping("/appointments")
  public ResponseEntity<?> appointment(@RequestBody Map<String, Object> request) {
    if (schedulingDown.get() || Boolean.TRUE.equals(request.get("simulateFailure"))) return failure();
    boolean available = !"none".equalsIgnoreCase(String.valueOf(request.getOrDefault("slotMode", "available")));
    Map<String, Object> response = new LinkedHashMap<>();
    response.put("slotAvailable", available);
    response.put("availableSlot", available ? "2026-11-10T10:30:00+05:30" : null);
    response.put("slot", available ? "2026-11-10T10:30:00+05:30" : null);
    response.put("availableSlots", available
        ? List.of(Map.of("slotId", "SLOT-001", "startTime", "2026-11-10T10:30:00+05:30"))
        : List.of());
    response.put("bookingStatus", available ? "SLOT_AVAILABLE" : "NO_SLOTS");
    return ResponseEntity.ok(response);
  }

  private ResponseEntity<Map<String, Object>> failure() {
    return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(Map.of("error", "Synthetic downstream failure"));
  }

  private BigDecimal positiveAmount(Map<String, Object> request, String name) {
    Object value = request.get(name);
    if (value == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, name + " is required");
    }
    try {
      BigDecimal amount = new BigDecimal(String.valueOf(value));
      if (amount.signum() <= 0) {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, name + " must be greater than zero");
      }
      return amount;
    } catch (NumberFormatException exception) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, name + " must be numeric", exception);
    }
  }

  private int integer(Map<String, Object> request, String name) {
    Object value = request.get(name);
    if (value == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, name + " is required");
    }
    try {
      return new BigDecimal(String.valueOf(value)).intValueExact();
    } catch (NumberFormatException | ArithmeticException exception) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, name + " must be an integer", exception);
    }
  }

  private String requiredText(Map<String, Object> request, String name) {
    Object value = request.get(name);
    if (value == null || String.valueOf(value).isBlank()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, name + " is required");
    }
    return String.valueOf(value).trim();
  }
}
