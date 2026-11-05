# REST Connector Guide

The BPMN models use Camunda's **REST Outbound Connector** element template (`io.camunda.connectors.HttpJson.v2`, local template version 13). In Modeler, the template appears as an R-badge task. Technically, it remains a BPMN service task and runs with `io.camunda:http-json:1`.

The Spring Boot application exposes local synthetic APIs at `http://localhost:8081/mock`. The decision endpoints calculate a result from each request's inputs; the executable BPMN models do not call the retained DMN reference files.

## Connector configuration

| Lab | BPMN task | Method + URL | Request values | Result expression |
|---|---|---|---|---|
| 1 | Mock credit assessment | `POST /mock/credit-assessments` | `syntheticCreditScore`, `simulateFailure` | `creditScore`, `creditStatus` |
| 1 | Calculate loan decision | `POST /mock/decisions/loan` | `monthlyIncome`, `requestedAmount`, `creditScore` | `loanDecision`, `decisionReason` |
| 2 | Calculate delivery priority | `POST /mock/decisions/order-priority` | `orderTotal`, `customerCategory`, `deliveryPreference` | `deliveryPriority` |
| 2 | Mock payment gateway | `POST /mock/payments` | `paymentToken`, `simulateFailure` | `paymentStatus`, `transactionRef` |
| 3 | Calculate triage category | `POST /mock/decisions/triage` | `urgencyIndicator`, `symptomsSummary` | `triageCategory` |
| 3 | Mock scheduling API | `POST /mock/appointments` | `slotMode`, `simulateFailure` | `slotAvailable`, `availableSlot`, `availableSlots`, `bookingStatus` |

Each task maps the JSON response to process variables in its `resultExpression` task header. The process gateways use those variables for routing. Keep the R-icon element template and do not replace these connector tasks with generic service tasks.

For Camunda running in a separate container, replace `localhost` with `host.docker.internal` so it can reach the API on the Windows host. Do not put real API keys, card data, patient data, or passwords in either the model or repository.

## Decision endpoint examples

### Loan decision

```http
POST /mock/decisions/loan
Content-Type: application/json

{"monthlyIncome":60000,"requestedAmount":300000,"creditScore":720}
```

```json
{"loanDecision":"Approved","decisionReason":"Income, requested amount, and credit score meet the approval thresholds."}
```

### Order priority

```http
POST /mock/decisions/order-priority
Content-Type: application/json

{"orderTotal":12000,"customerCategory":"GOLD","deliveryPreference":"EXPRESS"}
```

```json
{"deliveryPriority":"P1"}
```

### Triage category

```http
POST /mock/decisions/triage
Content-Type: application/json

{"urgencyIndicator":"AMBER","symptomsSummary":"Needs priority review"}
```

```json
{"triageCategory":"Priority Review"}
```

These are educational examples only. Triage is not clinical advice or a medical decision-support function.

## Integration examples

### Credit assessment

```http
POST /mock/credit-assessments
Content-Type: application/json

{"syntheticCreditScore":720,"simulateFailure":false}
```

```json
{"creditScore":720,"creditStatus":"CLEAR"}
```

### Payment gateway

```http
POST /mock/payments
Content-Type: application/json

{"paymentToken":"OK","simulateFailure":false}
```

```json
{"paymentStatus":"APPROVED","transactionRef":"MOCK-…"}
```

Use `"paymentToken":"DECLINE"` for a declined payment, or `"simulateFailure":true` to return HTTP 503.

### Appointment scheduling

```http
POST /mock/appointments
Content-Type: application/json

{"slotMode":"available","simulateFailure":false}
```

The response contains the available slot and `bookingStatus="SLOT_AVAILABLE"`. Using `"slotMode":"none"` returns `slotAvailable:false`, an empty `availableSlots` list, and `bookingStatus="NO_SLOTS"`. This mock reports availability; it does not reserve a real appointment.

## Incident and recovery demonstration

Switch the relevant integration mock off before starting a process. When Operate shows the failed REST job, turn the mock back on and retry the job. The request uses the same process variables; use the mock control endpoint rather than changing variables on a failed instance. The integration tasks have one attempt for a quick incident demonstration; decision tasks retry up to three times.

| Lab | Switch off | Switch on |
|---|---|---|
| 1 | `http://localhost:8081/mock/controls/credit?down=true` | `http://localhost:8081/mock/controls/credit?down=false` |
| 2 | `http://localhost:8081/mock/controls/payment?down=true` | `http://localhost:8081/mock/controls/payment?down=false` |
| 3 | `http://localhost:8081/mock/controls/scheduling?down=true` | `http://localhost:8081/mock/controls/scheduling?down=false` |
