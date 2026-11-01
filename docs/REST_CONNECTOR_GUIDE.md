# REST Connector Guide

All integrations use the local mock API bundled with this project. They accept synthetic values only.

## Camunda HTTP JSON Connector configuration

In Camunda Modeler, configure the **REST connector** service task with the following values. Keep the default retry count at 3 so a temporary mock outage becomes visible as an Operate incident and can be retried after correction.

| Lab | Task | Method + URL | Request body | Response mappings |
|---|---|---|---|---|
| 1 | Mock credit assessment | `POST http://host.docker.internal:8081/mock/credit-assessments` | `={"syntheticCreditScore": creditScore, "simulateFailure": simulateFailure}` | `creditScore`, `creditStatus` |
| 2 | Mock payment gateway | `POST http://host.docker.internal:8081/mock/payments` | `={"paymentToken": paymentToken, "simulateFailure": simulateFailure}` | `paymentStatus`, `transactionRef` |
| 3 | Mock scheduling API | `POST http://host.docker.internal:8081/mock/appointments` | `={"slotMode": slotMode, "simulateFailure": simulateFailure}` | `slotAvailable`, `availableSlot` |

For a locally installed Camunda runtime on the host, replace `host.docker.internal` with `localhost`. Do not put real API keys, card data, patient data, or passwords in either model or repository.

## API examples

### Credit assessment

```http
POST /mock/credit-assessments
Content-Type: application/json

{"syntheticCreditScore": 720, "simulateFailure": false}
```

```json
{"creditScore":720,"creditStatus":"CLEAR"}
```

### Payment gateway

```http
POST /mock/payments
Content-Type: application/json

{"paymentToken":"OK", "simulateFailure":false}
```

```json
{"paymentStatus":"APPROVED","transactionRef":"MOCK-…"}
```

Use `"paymentToken":"DECLINE"` for a declined payment, or `"simulateFailure":true` to return HTTP 503.

### Appointment scheduling

```http
POST /mock/appointments
Content-Type: application/json

{"slotMode":"available", "simulateFailure":false}
```

```json
{"slotAvailable":true,"slot":"2026-11-10T10:30:00+05:30"}
```
