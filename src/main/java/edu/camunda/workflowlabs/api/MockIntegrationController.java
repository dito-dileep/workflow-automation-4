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

  @PostMapping("/validations/loan")
  public Map<String, Object> validateLoan(@RequestBody Map<String, Object> request) {
    Integer score = integerOrNull(request.get("syntheticCreditScore"));
    boolean valid = hasText(request, "applicantId")
        && isPositiveNumber(request.get("monthlyIncome"))
        && isPositiveNumber(request.get("requestedAmount"))
        && score != null && score >= 300 && score <= 850;
    return Map.of(
        "applicationValid", valid,
        "validationNote", valid
            ? "Synthetic applicant, financial inputs, and credit score validated"
            : "Applicant ID, positive monthly income, positive requested amount, and a credit score from 300 to 850 are required");
  }

  @PostMapping("/validations/order")
  public Map<String, Object> validateOrder(@RequestBody Map<String, Object> request) {
    int quantity = integerOrZero(request.get("quantity"));
    BigDecimal unitPrice = decimalOrZero(request.get("unitPrice"));
    boolean valid = hasText(request, "customerRef") && hasText(request, "productId")
        && quantity > 0 && unitPrice.signum() > 0
        && isPaymentToken(request.get("paymentToken"));
    boolean available = valid && quantity <= 20;
    return Map.of(
        "orderValid", valid,
        "stockAvailable", available,
        "orderTotal", valid ? unitPrice.multiply(BigDecimal.valueOf(quantity)) : BigDecimal.ZERO,
        "inventoryNote", !valid
            ? "Customer and product references, positive quantity and price, and paymentToken OK or DECLINE are required"
            : available ? "Synthetic stock reserved" : "Synthetic stock unavailable");
  }

  @PostMapping("/validations/registration")
  public Map<String, Object> validateRegistration(@RequestBody Map<String, Object> request) {
    boolean valid = hasText(request, "patientRef") && hasText(request, "appointmentPreference")
        && hasText(request, "symptomsSummary") && hasText(request, "urgencyIndicator")
        && isSlotMode(request.get("slotMode"));
    return Map.of(
        "registrationValid", valid,
        "registrationNote", valid
            ? "Synthetic registration validated"
            : "Patient reference, appointment preference, symptoms summary, urgency indicator, and slotMode available or none are required");
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
    Integer score = integerOrNull(request.get("syntheticCreditScore"));
    if (score == null || score < 300 || score > 850) {
      return ResponseEntity.badRequest().body(Map.of("error", "syntheticCreditScore must be an integer from 300 to 850"));
    }
    return ResponseEntity.ok(Map.of("creditScore", score, "creditStatus", score >= 650 ? "CLEAR" : "REFER"));
  }

  @PostMapping("/payments")
  public ResponseEntity<?> payment(@RequestBody Map<String, Object> request) {
    if (paymentDown.get() || Boolean.TRUE.equals(request.get("simulateFailure"))) return failure();
    Object token = request.get("paymentToken");
    if (!isPaymentToken(token)) {
      return ResponseEntity.badRequest().body(Map.of("error", "paymentToken must be OK or DECLINE"));
    }
    boolean approved = "OK".equalsIgnoreCase(String.valueOf(token));
    return ResponseEntity.ok(Map.of("paymentStatus", approved ? "APPROVED" : "DECLINED", "transactionRef", "MOCK-" + System.currentTimeMillis()));
  }

  @PostMapping("/appointments")
  public ResponseEntity<?> appointment(@RequestBody Map<String, Object> request) {
    if (schedulingDown.get() || Boolean.TRUE.equals(request.get("simulateFailure"))) return failure();
    Object mode = request.get("slotMode");
    if (!isSlotMode(mode)) {
      return ResponseEntity.badRequest().body(Map.of("error", "slotMode must be available or none"));
    }
    boolean available = "available".equalsIgnoreCase(String.valueOf(mode));
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

  private boolean hasText(Map<String, Object> request, String name) {
    Object value = request.get(name);
    return value != null && !String.valueOf(value).isBlank();
  }

  private boolean isPositiveNumber(Object value) {
    if (value == null) {
      return false;
    }
    try {
      return new BigDecimal(String.valueOf(value)).signum() > 0;
    } catch (NumberFormatException exception) {
      return false;
    }
  }

  private BigDecimal decimalOrZero(Object value) {
    if (value == null) {
      return BigDecimal.ZERO;
    }
    try {
      return new BigDecimal(String.valueOf(value));
    } catch (NumberFormatException exception) {
      return BigDecimal.ZERO;
    }
  }

  private int integerOrZero(Object value) {
    if (value == null) {
      return 0;
    }
    try {
      return new BigDecimal(String.valueOf(value)).intValueExact();
    } catch (NumberFormatException | ArithmeticException exception) {
      return 0;
    }
  }

  private Integer integerOrNull(Object value) {
    if (value == null) {
      return null;
    }
    try {
      return new BigDecimal(String.valueOf(value)).intValueExact();
    } catch (NumberFormatException | ArithmeticException exception) {
      return null;
    }
  }

  private boolean isPaymentToken(Object value) {
    return value != null && ("OK".equalsIgnoreCase(String.valueOf(value))
        || "DECLINE".equalsIgnoreCase(String.valueOf(value)));
  }

  private boolean isSlotMode(Object value) {
    return value != null && ("available".equalsIgnoreCase(String.valueOf(value))
        || "none".equalsIgnoreCase(String.valueOf(value)));
  }
}
