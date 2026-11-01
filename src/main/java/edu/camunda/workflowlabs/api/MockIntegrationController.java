package edu.camunda.workflowlabs.api;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Local mock endpoints used by connector examples. Pass simulateFailure=true to intentionally create a recoverable error. */
@RestController
@RequestMapping("/mock")
public class MockIntegrationController {
  @PostMapping("/credit-assessments")
  public ResponseEntity<?> credit(@RequestBody Map<String, Object> request) {
    if (Boolean.TRUE.equals(request.get("simulateFailure"))) return failure();
    int score = Integer.parseInt(String.valueOf(request.getOrDefault("syntheticCreditScore", 700)));
    return ResponseEntity.ok(Map.of("creditScore", score, "creditStatus", score >= 650 ? "CLEAR" : "REFER"));
  }
  @PostMapping("/payments")
  public ResponseEntity<?> payment(@RequestBody Map<String, Object> request) {
    if (Boolean.TRUE.equals(request.get("simulateFailure"))) return failure();
    boolean approved = !"DECLINE".equalsIgnoreCase(String.valueOf(request.getOrDefault("paymentToken", "OK")));
    return ResponseEntity.ok(Map.of("paymentStatus", approved ? "APPROVED" : "DECLINED", "transactionRef", "MOCK-" + System.currentTimeMillis()));
  }
  @PostMapping("/appointments")
  public ResponseEntity<?> appointment(@RequestBody Map<String, Object> request) {
    if (Boolean.TRUE.equals(request.get("simulateFailure"))) return failure();
    boolean available = !"none".equalsIgnoreCase(String.valueOf(request.getOrDefault("slotMode", "available")));
    Map<String, Object> response = new LinkedHashMap<>();
    response.put("slotAvailable", available);
    response.put("slot", available ? "2026-11-10T10:30:00+05:30" : null);
    return ResponseEntity.ok(response);
  }
  private ResponseEntity<Map<String, Object>> failure() {
    return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(Map.of("error", "Synthetic downstream failure"));
  }
}
