# REST Connector Guide

The service tasks that validate inputs or call mock external services use Camunda's **REST Outbound Connector** element template (`io.camunda.connectors.HttpJson.v2`, local template version 13). In Modeler these tasks show the R badge and run as `io.camunda:http-json:1`.

The three decision tasks are deliberately **business-rule tasks**, not REST calls. They evaluate the corresponding DMN table against current process variables. This keeps the routing criteria visible and lets you demonstrate the input columns, matching rule, output, and BPMN gateway.

## REST Connector tasks

| Lab | BPMN task | Method + URL | Request inputs | Response variables |
|---|---|---|---|---|
| 1 | Validate applicant information | `POST /mock/validations/loan` | applicant ID, income, requested amount, synthetic credit score | `applicationValid`, `validationNote` |
| 1 | Mock credit assessment | `POST /mock/credit-assessments` | synthetic credit score, failure flag | `creditScore`, `creditStatus` |
| 2 | Validate order and calculate stock | `POST /mock/validations/order` | customer/product references, quantity, unit price, payment token | `orderValid`, `stockAvailable`, `orderTotal`, `inventoryNote` |
| 2 | Mock payment gateway | `POST /mock/payments` | payment token (`OK` or `DECLINE`), failure flag | `paymentStatus`, `transactionRef` |
| 3 | Validate registration | `POST /mock/validations/registration` | patient reference, appointment preference, symptoms, urgency, slot mode | `registrationValid`, `registrationNote` |
| 3 | Mock scheduling API | `POST /mock/appointments` | slot mode (`available` or `none`), failure flag | `slotAvailable`, `availableSlot`, `availableSlots`, `bookingStatus` |

All URLs use `http://localhost:8081` in the BPMN files. The connectors map each response into process variables using a `resultExpression` task header. The BPMN gateways and DMN tables use those variables in subsequent steps.

## Where each decision is calculated

| Lab | DMN | DMN input values | Output used by BPMN |
|---|---|---|---|
| 1 | `loan-decision` | `monthlyIncome`, `requestedAmount`, `creditScore` from the credit REST response | `loanDecision`: Approved, Manual Review, or Rejected |
| 2 | `order-priority` | `orderTotal` calculated by the validation REST API, `customerCategory`, `deliveryPreference` | `deliveryPriority`: P1, P2, or P3 |
| 3 | `triage-decision` | `urgencyIndicator`, `symptomsSummary` | `triageCategory`: Urgent Assessment, Priority Review, or Routine |

The outputs are not fixed when a process starts. To demonstrate, start instances with different inputs, then show the DMN result variable and the matching outgoing path in Operate.

## Start inputs versus calculated outputs

Only provide the scenario inputs when starting a process. Do **not** include output fields such as `paymentStatus`, `orderValid`, `stockAvailable`, `orderTotal`, `applicationValid`, `creditScore`, `loanDecision`, `registrationValid`, or `triageCategory` in the start variables. Operate's Variables panel intentionally displays inputs and outputs together after execution; seeing a result there does not mean it was supplied at process start.

- Lab 1 validates the supplied score (300–850), then the credit REST API returns `creditScore`/`creditStatus`; the loan DMN calculates `loanDecision` from income, amount, and that returned score.
- Lab 2 computes `orderValid`, `stockAvailable`, and `orderTotal` from the order fields. A required `paymentToken` of `OK` or `DECLINE` drives the payment REST API's `paymentStatus`. No token means invalid order; there is no default approval.
- Lab 3 validates the registration and required `slotMode`; the triage DMN calculates `triageCategory`, and the scheduling REST API derives availability from `slotMode`. There is no default slot result.

The REST tasks in the repository's `src/main/resources/models` folder carry the REST Connector element-template ID and version as well as request/response mappings. Open those files in Modeler to see the R badge and Connector configuration. Avoid opening an older extracted copy under `Downloads\workflow-automation-4-main`; that copy is not the configured source used by this project.

For Camunda running in a separate container, replace `localhost` with `host.docker.internal` so it can reach the API on the Windows host. Do not put real API keys, card data, patient data, or passwords in either the model or repository.

## Synthetic integration examples

### Credit assessment

```http
POST /mock/credit-assessments
Content-Type: application/json

{"syntheticCreditScore":720,"simulateFailure":false}
```

```json
{"creditScore":720,"creditStatus":"CLEAR"}
```

The DMN then combines `creditScore` with the income and amount submitted for that process instance.

### Payment gateway

```http
POST /mock/payments
Content-Type: application/json

{"paymentToken":"OK","simulateFailure":false}
```

The response contains the calculated `paymentStatus` (`APPROVED` for `OK`, `DECLINED` for `DECLINE`); it is not a start input. Use `"simulateFailure":true` to return HTTP 503.

### Appointment scheduling

```http
POST /mock/appointments
Content-Type: application/json

{"slotMode":"available","simulateFailure":false}
```

Using `"slotMode":"none"` returns `slotAvailable:false`, an empty `availableSlots` list, and `bookingStatus="NO_SLOTS"`. `slotMode` is required. This mock reports availability; it does not reserve a real appointment.

## Incident and recovery demonstration

Switch the relevant integration mock off before starting a process. When Operate shows the failed REST job, turn the mock back on and retry the job. The request uses the same process variables; use the mock control endpoint rather than changing variables on a failed instance.

| Lab | Switch off | Switch on |
|---|---|---|
| 1 | `http://localhost:8081/mock/controls/credit?down=true` | `http://localhost:8081/mock/controls/credit?down=false` |
| 2 | `http://localhost:8081/mock/controls/payment?down=true` | `http://localhost:8081/mock/controls/payment?down=false` |
| 3 | `http://localhost:8081/mock/controls/scheduling?down=true` | `http://localhost:8081/mock/controls/scheduling?down=false` |
