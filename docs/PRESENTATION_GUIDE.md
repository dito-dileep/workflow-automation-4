# Presentation guide: three REST-driven workflow labs

## What to show first

Open the three BPMN files in Camunda Modeler. Explain that each REST call is still a BPMN **service task**, but it has Camunda's **REST Outbound Connector** element template applied (the R badge). The template exposes the HTTP settings and configures the `io.camunda:http-json:1` connector.

For each lab, click the R-badge decision task and show:

1. The REST Connector template and `POST` URL.
2. The request body, which maps the current process instance's input variables.
3. The `resultExpression`, which copies the API decision into a process variable.
4. The outgoing BPMN gateway condition that routes using that returned variable.

The three `.dmn` files remain in the project as reference examples; the running processes do **not** call them. The Spring Boot API calculates each result from the request values. All APIs are synthetic local demos, not real lending, payment, or clinical services.

## Lab 1 — Loan approval

**Start variables:** `applicantId`, `monthlyIncome`, `requestedAmount`, `syntheticCreditScore`.

The credit REST call returns `creditScore` and `creditStatus`. The next REST call sends `monthlyIncome`, `requestedAmount`, and the returned `creditScore` to `/mock/decisions/loan`. The response sets `loanDecision` and `decisionReason`; the BPMN gateway routes Approved, Manual Review, or Rejected.

The API uses these input thresholds:

- **Approved:** monthly income at least `50000`, requested amount at most `500000`, credit score at least `700`.
- **Manual Review:** otherwise, monthly income at least `30000`, requested amount at most `750000`, credit score at least `600`.
- **Rejected:** otherwise.

Start a Manual Review example:

```powershell
$body = '{"processDefinitionId":"loan-approval","variables":{"applicantId":"APP-DEMO-REVIEW","monthlyIncome":35000,"requestedAmount":600000,"syntheticCreditScore":640}}'
$r = Invoke-RestMethod -Uri 'http://localhost:8080/v2/process-instances' -Method Post -ContentType 'application/json' -Body $body
"Instance key: $($r.processInstanceKey)"
```

Expected: `creditScore=640`, `creditStatus=REFER`, `loanDecision=Manual Review`; Operate shows the token waiting at **Manual loan review**, and Tasklist shows the review assigned to `demo`.

For Approved, use income `60000`, amount `300000`, score `720`. For Rejected, use income `25000`, amount `900000`, score `550`.

**Say:** “The process checks the application, obtains a synthetic credit score, then sends income, loan amount, and score to a REST API. That API calculates the decision for this application, and BPMN routes it to approval, rejection, or manual review.”

## Lab 2 — Order fulfilment

**Start variables:** `customerRef`, `productId`, `quantity`, `unitPrice`, `customerCategory`, `deliveryPreference`, `paymentToken`.

The Java worker validates the order and stock and calculates `orderTotal = quantity × unitPrice`. The REST decision call sends `orderTotal`, `customerCategory`, and `deliveryPreference` to `/mock/decisions/order-priority`; its response sets `deliveryPriority`. Payment is then handled by a separate REST Connector call to `/mock/payments`.

- **P1:** total at least `5000`, category `GOLD`, preference `EXPRESS`.
- **P2:** otherwise, total at least `2000` and preference `EXPRESS`.
- **P3:** otherwise.

Start a P1/manual-verification example:

```powershell
$body = '{"processDefinitionId":"order-fulfilment","variables":{"customerRef":"CUST-DEMO-HIGH","productId":"SKU-002","quantity":2,"unitPrice":6000,"customerCategory":"GOLD","deliveryPreference":"EXPRESS","paymentToken":"OK"}}'
$r = Invoke-RestMethod -Uri 'http://localhost:8080/v2/process-instances' -Method Post -ContentType 'application/json' -Body $body
"Instance key: $($r.processInstanceKey)"
```

Expected: `orderTotal=12000`, `deliveryPriority=P1`, `paymentStatus=APPROVED`; the process waits at **Manual order verification**. A standard order with total `1000`, `STANDARD`, `STANDARD`, and token `OK` returns P3 and completes without review.

**Say:** “The worker validates stock and calculates the order total. A REST API uses the total, customer category, and delivery preference to calculate priority. The process then calls the payment API and sends P1 or P2 orders for manual verification.”

## Lab 3 — Patient appointment and triage

**Start variables:** `patientRef`, `appointmentPreference`, `symptomsSummary`, `urgencyIndicator`, `slotMode`.

The Java worker validates registration. The triage REST call sends `urgencyIndicator` and `symptomsSummary` to `/mock/decisions/triage`; the API uses the urgency indicator to return `triageCategory`. `RED` routes to **Immediate clinical assessment**, `AMBER` to **Priority staff review**, and other values to the routine scheduling path. The scheduling REST call returns `slotAvailable`, `availableSlot`, `availableSlots`, and `bookingStatus`.

- `GREEN` with `slotMode=available` → `Routine`, one mock slot, `SLOT_AVAILABLE`.
- `AMBER` with `slotMode=available` → `Priority Review` task, then scheduling.
- `RED` → `Urgent Assessment` task.
- `GREEN` with `slotMode=none` → no slots and a staff scheduling-review task.

This is an educational simulation. It is not medical advice, clinical decision support, or a real appointment reservation.

**Say:** “After registration validation, a REST API uses the urgency indicator to return a triage category. BPMN routes urgent and priority cases to staff, while routine cases continue to the scheduling API. The scheduling result is only a mock availability response.”

## Where to show the results

1. In Operate, open the instance using the key printed by PowerShell.
2. Select the process-instance root in **Instance History**, then open **Variables**. Show both submitted inputs and calculated outputs, such as `monthlyIncome` / `loanDecision`, `orderTotal` / `deliveryPriority`, or `urgencyIndicator` / `triageCategory`.
3. Select the REST task in the BPMN diagram to show where the API was called. In Modeler, show its input mapping and response `resultExpression`.
4. For a waiting human task, open Tasklist and complete the task assigned to `demo`.

If a process already completed, it will not show a waiting user task. Start the matching review example and open Tasklist while it is active.

## REST endpoints

- Loan credit assessment: `POST http://localhost:8081/mock/credit-assessments`
- Loan decision: `POST http://localhost:8081/mock/decisions/loan`
- Order priority: `POST http://localhost:8081/mock/decisions/order-priority`
- Order payment: `POST http://localhost:8081/mock/payments`
- Triage decision: `POST http://localhost:8081/mock/decisions/triage`
- Appointment scheduling: `POST http://localhost:8081/mock/appointments`

The [REST Connector Guide](REST_CONNECTOR_GUIDE.md) documents request/response mappings and incident controls; the [Test Matrix](TEST_MATRIX.md) contains the sample scenarios.
