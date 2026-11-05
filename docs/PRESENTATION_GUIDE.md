# Presentation guide: three Camunda workflow labs

## What the professor asked for

The tasks that validate inputs or call external services are BPMN service tasks configured with Camunda's **REST Outbound Connector** template (the R badge). The decision activities are **DMN business-rule tasks** (table icon), because the decision rules should be visible in the DMN tables and evaluated using the values of each process instance.

Explain the difference clearly:

- **REST Connector task:** calls the Spring Boot API. Its input mapping shows the HTTP method, URL, and request body; its `resultExpression` maps the JSON response to process variables.
- **DMN decision task:** evaluates a DMN table using the current process variables. Its matching row produces an output that the following BPMN gateway uses to select the path.
- **First task:** is not an empty activity. It calls a validation REST endpoint and returns calculated variables used by the next gateway. In Lab 2 it also calculates `orderTotal` and stock availability.

## Start everything from a blank Windows laptop

You need Java 21, Maven 3.10.0, Camunda 8 Run 8.8.32, and Camunda Modeler. Start two PowerShell windows.

### Window 1 — Camunda

```powershell
Set-Location 'C:\Users\ditod\Downloads\camunda8-run-8.8-windows-x86_64\c8run-8.8.32'
.\c8run.exe start
```

Wait for “All processes are running and healthy”. Leave this window open.

### Window 2 — project and APIs

```powershell
Set-Location 'C:\Users\ditod\OneDrive\Desktop\dataset\BPMN_Exercise_4'
& 'C:\Users\ditod\Downloads\apache-maven-3.10.0\bin\mvn.cmd' spring-boot:run
```

Wait for Spring Boot to finish starting and deploying the models. Leave this window open too. Check the API in a third PowerShell window:

```powershell
Invoke-RestMethod 'http://localhost:8081/mock/status'
```

Open Operate at http://localhost:8080/operate and Tasklist at http://localhost:8080/tasklist. Sign in with `demo` / `demo`.

## Show the diagrams before running cases

In Camunda Modeler, open each `.bpmn` model from:

```text
C:\Users\ditod\OneDrive\Desktop\dataset\BPMN_Exercise_4\src\main\resources\models
```

For **each R-badge service task**:

1. Select the task and show the REST Connector template.
2. Expand the input mapping and point out `POST`, the `/mock/...` URL, and the request body.
3. Show the response `resultExpression` that creates process variables.
4. Explain what the first task calculates (validation; plus order total/stock in Lab 2).

For **each decision task**:

1. Select the table/business-rule task and show its decision ID.
2. Open the matching `.dmn` model in Modeler.
3. Point out the input columns, rules, output column, and the BPMN gateway condition that uses the output.
4. In Operate, show a started instance's input variables and the calculated output to prove the result changes with the input.

R badges belong on the REST service tasks. DMN decision tasks correctly use the business-rule/table symbol; do not turn those into REST calls.

## Lab 1 — Loan approval

**Process inputs:** `applicantId`, `monthlyIncome`, `requestedAmount`, `syntheticCreditScore`.

1. Validation REST task checks the ID and positive income/amount, returning `applicationValid`.
2. Credit REST task returns `creditScore` and `creditStatus`.
3. The loan DMN evaluates `monthlyIncome`, `requestedAmount`, and returned `creditScore`. The BPMN gateway routes the resulting `loanDecision`.

**DMN rules:** Approved if income is at least `50000`, amount is at most `500000`, and score at least `700`; otherwise Manual Review if income is at least `30000`, amount at most `750000`, and score at least `600`; otherwise Rejected.

Run the manual-review case:

```powershell
$body = '{"processDefinitionId":"loan-approval","variables":{"applicantId":"APP-DEMO-REVIEW","monthlyIncome":35000,"requestedAmount":600000,"syntheticCreditScore":640}}'
$r = Invoke-RestMethod -Uri 'http://localhost:8080/v2/process-instances' -Method Post -ContentType 'application/json' -Body $body
"Instance key: $($r.processInstanceKey)"
```

Expected: validation passes, `creditScore=640`, `creditStatus=REFER`, and the DMN returns `loanDecision=Manual Review`. Show the active **Manual loan review** task in Tasklist. For other outcomes, use income/amount/score `60000/300000/720` (Approved) or `25000/900000/550` (Rejected).

**Say:** “The first REST connector validates the application. The credit API returns a score, then the DMN compares this applicant's income, requested amount, and score with the displayed rules. The BPMN gateway follows the DMN result.”

## Lab 2 — E-commerce order fulfilment

**Process inputs:** `customerRef`, `productId`, `quantity`, `unitPrice`, `customerCategory`, `deliveryPreference`, `paymentToken`.

1. The first REST task validates the order, calculates `orderTotal = quantity × unitPrice`, and returns whether stock is available (the synthetic stock limit is 20).
2. The order DMN evaluates `orderTotal`, `customerCategory`, and `deliveryPreference`.
3. The payment REST task returns `paymentStatus`; approved P1/P2 orders go through manual verification.

**DMN rules:** P1 if total is at least `5000`, category is `GOLD`, and preference is `EXPRESS`; P2 if total is at least `2000` and preference is `EXPRESS`; P3 otherwise.

Run a manual-verification case:

```powershell
$body = '{"processDefinitionId":"order-fulfilment","variables":{"customerRef":"CUST-DEMO-HIGH","productId":"SKU-002","quantity":2,"unitPrice":6000,"customerCategory":"GOLD","deliveryPreference":"EXPRESS","paymentToken":"OK"}}'
$r = Invoke-RestMethod -Uri 'http://localhost:8080/v2/process-instances' -Method Post -ContentType 'application/json' -Body $body
"Instance key: $($r.processInstanceKey)"
```

Expected: `orderTotal=12000`, stock available, DMN `deliveryPriority=P1`, and payment approved. Show **Manual order verification** in Tasklist. Use quantity `2`, price `500`, `STANDARD` / `STANDARD` for P3; quantity `1`, price `2500`, `STANDARD` / `EXPRESS` for P2; quantity `25` for out of stock; `paymentToken=DECLINE` for declined payment.

**Say:** “The first connector validates the order and calculates its total and stock result. DMN applies the visible priority rules to those values and the customer's choices. BPMN then calls the payment API and routes the result.”

## Lab 3 — Patient appointment and triage

**Process inputs:** `patientRef`, `appointmentPreference`, `symptomsSummary`, `urgencyIndicator`, `slotMode`.

1. The first REST task validates the registration and returns `registrationValid`.
2. The triage DMN evaluates `urgencyIndicator` and `symptomsSummary`, returning `triageCategory`.
3. Routine cases and reviewed priority cases call the scheduling REST API; no-slot responses route to staff review.

**DMN rules:** RED → Urgent Assessment; AMBER → Priority Review; otherwise Routine. This is an educational example only, not medical advice or clinical decision support.

Run a routine case:

```powershell
$body = '{"processDefinitionId":"patient-triage","variables":{"patientRef":"PAT-DEMO-1","appointmentPreference":"MORNING","symptomsSummary":"Routine follow-up","urgencyIndicator":"GREEN","slotMode":"available"}}'
$r = Invoke-RestMethod -Uri 'http://localhost:8080/v2/process-instances' -Method Post -ContentType 'application/json' -Body $body
"Instance key: $($r.processInstanceKey)"
```

Expected: `registrationValid=true`, `triageCategory=Routine`, and scheduling returns `bookingStatus=SLOT_AVAILABLE`. For a Tasklist priority review, change urgency to `AMBER`. `RED` creates the immediate clinical assessment task. `GREEN` with `slotMode=none` goes to scheduling review.

**Say:** “The first REST task validates the submitted registration. The DMN then maps the urgency input to a category, and BPMN routes that result. Routine and reviewed cases continue to the scheduling API.”

## Show variables and complete tasks

1. In Operate, search for the process instance key printed after starting the case.
2. Open **Instance History**, select the process-instance root, then **Variables**.
3. Show inputs and outputs together: `monthlyIncome` / `creditScore` / `loanDecision`; `quantity` / `unitPrice` / `orderTotal` / `deliveryPriority`; or `urgencyIndicator` / `triageCategory` / `bookingStatus`.
4. For a live human-task route, open Tasklist in a separate tab and complete the task assigned to `demo`.

If a process already completed, it no longer has a waiting task. Start the manual-review sample while presenting and then open Tasklist.

## Incident demo (optional)

Before creating an instance, switch off one mock integration, for example:

```powershell
Invoke-RestMethod 'http://localhost:8081/mock/controls/credit?down=true'
```

Start a valid loan instance and show the failed REST job in Operate. Restore the mock, then retry:

```powershell
Invoke-RestMethod 'http://localhost:8081/mock/controls/credit?down=false'
```

In Operate, select the failed job and click **Retry**. The job retries with its original process variables. Do not leave a mock switched off after the demo.
