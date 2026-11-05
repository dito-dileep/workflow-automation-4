# Presentation guide: inputs, DMN decisions, and REST connectors

## The important distinction

The BPMN diagram does not contain one fixed set of input values. Values are supplied when a process instance starts. Workers and REST connectors produce additional variables, DMN evaluates its input variables, and BPMN gateways use the returned DMN output to select a route.

In Operate, select the **process instance root** in Instance History to see the process variables. Selecting a service-task row can show “The Flow Node has no Variables”; that is a node-specific view and does not mean the instance has no variables. Click a business-rule (DMN) activity to inspect that step, and use the root Variables tab for the full input/output set.

## What to show in Camunda Modeler

For each BPMN file:

1. Select the HTTP service task (gear icon).
2. Expand **Task definition** to show `io.camunda:http-json:1`.
3. Expand **Input mapping** to show the HTTP method, local API URL, and request body.
4. Expand **Headers** to show the `resultExpression` that maps the response into process variables.
5. Select the business-rule task (table icon) to show its called decision ID and result variable.
6. Open the corresponding DMN tab and show its input columns, rules, and output column.
7. Select the outgoing BPMN gateway to show that its sequence-flow condition uses the DMN output.

The `localhost:8081` APIs are synthetic Spring Boot mocks bundled with this project; they do not call real credit, payment, or hospital services.

## Lab 1 — Loan approval

**Start variables:** `applicantId`, `monthlyIncome`, `requestedAmount`, `syntheticCreditScore`.

The credit REST connector sends the synthetic score to `/mock/credit-assessments`; the response supplies `creditScore` and `creditStatus`. The `loan-decision` DMN evaluates `monthlyIncome`, `requestedAmount`, and returned `creditScore`, then sets `loanDecision`. The BPMN gateway routes `Approved` to the approved end, `Manual Review` to the Tasklist review, and the remaining result to rejection.

The DMN rules, in priority order, are:

- `Approved`: income at least `50000`, amount at most `500000`, and score at least `700`.
- `Manual Review`: income at least `30000`, amount at most `750000`, and score at least `600`, if the approval rule did not match first.
- `Rejected`: otherwise.

Use this manual-review example:

```powershell
$body = '{"processDefinitionId":"loan-approval","variables":{"applicantId":"APP-DEMO-REVIEW","monthlyIncome":35000,"requestedAmount":600000,"syntheticCreditScore":640}}'
$r = Invoke-RestMethod -Uri 'http://localhost:8080/v2/process-instances' -Method Post -ContentType 'application/json' -Body $body
"Instance key: $($r.processInstanceKey)"
```

Expected values: credit API returns `creditScore=640` and `creditStatus=REFER`; the DMN returns `loanDecision=Manual Review`; Operate shows the token waiting at **Manual loan review**, and Tasklist shows the assigned review task.

For automatic approval, use income `60000`, amount `300000`, and score `720`; expected `creditStatus=CLEAR` and `loanDecision=Approved`. For rejection, use income `25000`, amount `900000`, and score `550`; expected `loanDecision=Rejected`.

## Lab 2 — Order fulfilment

**Start variables:** `customerRef`, `productId`, `quantity`, `unitPrice`, `customerCategory`, `deliveryPreference`, `paymentToken`.

The Java worker validates the order, checks synthetic stock, and calculates `orderTotal = quantity × unitPrice`. The `order-priority` DMN evaluates `orderTotal`, `customerCategory`, and `deliveryPreference` and returns `deliveryPriority`. The BPMN gateway now uses that DMN result: `P1` and `P2` go to **Manual order verification**; `P3` proceeds to fulfilment. The REST connector sends `paymentToken` to `/mock/payments` and returns `paymentStatus` and `transactionRef`.

The DMN rules, in priority order, are: `P1` for total at least `5000` with `GOLD` and `EXPRESS`; `P2` for total at least `2000` with `EXPRESS`; and `P3` otherwise.

For a DMN-driven review, start this example:

```powershell
$body = '{"processDefinitionId":"order-fulfilment","variables":{"customerRef":"CUST-DEMO-HIGH","productId":"SKU-002","quantity":2,"unitPrice":6000,"customerCategory":"GOLD","deliveryPreference":"EXPRESS","paymentToken":"OK"}}'
$r = Invoke-RestMethod -Uri 'http://localhost:8080/v2/process-instances' -Method Post -ContentType 'application/json' -Body $body
"Instance key: $($r.processInstanceKey)"
```

Expected values: `orderTotal=12000`, payment returns `paymentStatus=APPROVED`, the DMN returns `deliveryPriority=P1`, and the BPMN gateway routes to **Manual order verification**.

For the normal route, use quantity `2`, unit price `500`, category `STANDARD`, preference `STANDARD`, and token `OK`; expected `orderTotal=1000`, `deliveryPriority=P3`, and fulfilment. For a decline, set `paymentToken` to `DECLINE`. Quantity `25` demonstrates insufficient stock.

## Lab 3 — Patient appointment and triage

**Start variables:** `patientRef`, `appointmentPreference`, `symptomsSummary`, `urgencyIndicator`, `slotMode`.

The Java worker sets `registrationValid`; a BPMN gateway rejects invalid registration before the DMN. The `triage-decision` DMN evaluates `urgencyIndicator` and `symptomsSummary`, sets `triageCategory`, and the BPMN gateway routes `Routine`, `Priority Review`, or `Urgent Assessment`. Routine and reviewed-priority flows call `/mock/appointments`; its response supplies `slotAvailable`, `availableSlot`, `availableSlots`, and `bookingStatus`.

- `RED` returns `Urgent Assessment`.
- `AMBER` returns `Priority Review`.
- Other urgency values return `Routine`.

- `GREEN` with `slotMode=available` → `Routine`, one mock slot, `SLOT_AVAILABLE`.
- `AMBER` with `slotMode=available` → `Priority Review` Tasklist task, then scheduling.
- `RED` → `Urgent Assessment` Tasklist task.
- `GREEN` with `slotMode=none` → no slots and a staff scheduling-review task.

This is an educational simulation; `SLOT_AVAILABLE` is not a real reservation or clinical recommendation.

## Finding the values in Operate

1. Open the instance in Operate using the instance key printed by PowerShell.
2. In **Instance History**, click the top-level process row, not a service-task row.
3. Open **Variables** to show submitted inputs and calculated outputs. Examples: `monthlyIncome` / `loanDecision`, `orderTotal` / `deliveryPriority`, or `urgencyIndicator` / `triageCategory` / `availableSlots`.
4. Click the decision activity in the diagram or history to show where DMN ran. Use the BPMN diagram and the DMN editor to show the rule and outgoing route.
5. For a waiting human task, open Tasklist and complete the task assigned to `demo`.

An instance that has already completed will not show a waiting user task. Start the relevant review example and open Tasklist while it is active.

## REST connector examples

The three connectors use `POST`:

- Loan: `http://localhost:8081/mock/credit-assessments`, body includes `syntheticCreditScore`.
- Order: `http://localhost:8081/mock/payments`, body includes `paymentToken`.
- Triage: `http://localhost:8081/mock/appointments`, body includes `slotMode`.

The full expressions and incident-control URLs are in the [REST Connector Guide](REST_CONNECTOR_GUIDE.md); complete test cases are in the [Test Matrix](TEST_MATRIX.md).
