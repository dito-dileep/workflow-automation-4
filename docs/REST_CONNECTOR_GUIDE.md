# REST Connector Guide

All integrations use the local mock API bundled with this project. They accept synthetic values only.

## Camunda HTTP JSON Connector configuration

The BPMN models already contain these HTTP JSON connector settings. In Camunda Modeler, use the values below if you need to inspect or recreate a service task. The local Camunda 8 Run connector calls the Spring Boot mock API on the same machine.

| Lab | Task | Method + URL | Request body | Result expression |
|---|---|---|---|---|
| 1 | Mock credit assessment | `POST http://localhost:8081/mock/credit-assessments` | `={"syntheticCreditScore": creditScore, "simulateFailure": simulateFailure}` | `={creditScore: response.body.creditScore, creditStatus: response.body.creditStatus}` |
| 2 | Mock payment gateway | `POST http://localhost:8081/mock/payments` | `={"paymentToken": paymentToken, "simulateFailure": simulateFailure}` | `={paymentStatus: response.body.paymentStatus, transactionRef: response.body.transactionRef}` |
| 3 | Mock scheduling API | `POST http://localhost:8081/mock/appointments` | `={"slotMode": slotMode, "simulateFailure": simulateFailure}` | `={slotAvailable: response.body.slotAvailable, availableSlot: response.body.availableSlot}` |

These result expressions are stored in the connector's `resultExpression` task header; do not replace them with BPMN output mappings.

For a connector running in a separate container, replace `localhost` with `host.docker.internal` so it can reach the API on the Windows host. Do not put real API keys, card data, patient data, or passwords in either model or repository.

To show an incident, switch the relevant mock off, run the process with `simulateFailure` omitted or `false`, then switch the mock back on and click **Retry** in Operate. The connector retries the request using the same mapped inputs; changing a process variable after the failure does not alter that request. Each BPMN HTTP task has one attempt configured so the incident appears promptly.

| Lab | Switch off | Switch on |
|---|---|---|
| 1 | http://localhost:8081/mock/controls/credit?down=true | http://localhost:8081/mock/controls/credit?down=false |
| 2 | http://localhost:8081/mock/controls/payment?down=true | http://localhost:8081/mock/controls/payment?down=false |
| 3 | http://localhost:8081/mock/controls/scheduling?down=true | http://localhost:8081/mock/controls/scheduling?down=false |

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
