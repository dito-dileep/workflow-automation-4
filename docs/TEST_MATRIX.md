# Test Matrix

Run each row after deployment. The original instance references from v6 and v7 are retained as historical baseline evidence. Fresh v8 live-run results are recorded below. Capture genuine Modeler, Tasklist, and Operate screenshots before submission; screenshots are not included or fabricated.

| ID | Lab | Synthetic input / setup | Expected route | Actual result | Evidence |
|---|---|---|---|---|---|
| L1-01 | Loan | Income 60,000; amount 300,000; score 720 | DMN evaluates to Approved | Historical v6 PASS — `creditStatus=CLEAR`; `loanDecision=Approved` | Live instance `2251799813728339` (v6) |
| L1-02 | Loan | Missing `applicantId` | Invalid-data rejection | PASS — `applicationValid=false`; completed at invalid-data end | Live instance `2251799813729143` (v6) |
| L1-03 | Loan | Income 35,000; amount 600,000; score 640 | DMN evaluates to Manual Review → approve | Historical v6 PASS — `loanDecision=Manual Review`; Tasklist task created for `demo` | Live instance `2251799813728328` (v6) |
| L1-04 | Loan | Switch credit mock off before starting | Incident → switch mock on → retry | PASS — credit incident created, mock restored, Retry completed; `loanDecision=Approved` | Live instance `2251799813729523` (v6) |
| L1-05 | Loan | Income 25,000; amount 900,000; score 550 | DMN evaluates to `Rejected` end | Historical v6 PASS — `loanDecision=Rejected`; completed | Live instance `2251799813728879` (v6) |
| L2-01 | Order | Qty 2; unit price 500; token `OK` | Stock → DMN evaluates to `P3` → payment → fulfilment | Historical v6 PASS — `orderTotal=1000`, `deliveryPriority=P3`, `paymentStatus=APPROVED`; completed | Live instance `2251799813728364` (v6) |
| L2-02 | Order | Qty 25 | Stock unavailable end | PASS — `stockAvailable=false`; completed without payment | Live instance `2251799813728903` (v6) |
| L2-03 | Order | Qty 1; token `DECLINE` | Payment declined end | PASS — `paymentStatus=DECLINED`; completed at declined end | Live instance `2251799813728926` (v6) |
| L2-04 | Order | Qty 2; unit price 6,000; category `GOLD`; preference `EXPRESS`; token `OK` | DMN evaluates to `P1` → manual verification → fulfilment | Historical v6 PASS — `orderTotal=12000`, `deliveryPriority=P1`; Tasklist task created for `demo` | Live instance `2251799813728350` (v6) |
| L2-05 | Order | Switch payment mock off before starting | Incident → switch mock on → retry | PASS — payment incident created, mock restored, Retry completed; `paymentStatus=APPROVED` | Live instance `2251799813729547` (v6) |
| L2-06 | Order | Missing `productId` or quantity 0 | Invalid-order end before stock/decision/payment | PASS — `orderValid=false`; completed at invalid-order end | Live instance `2251799813728378` (v6) |
| L2-07 | Order | Qty 1; unit price 2,500; preference `EXPRESS`; token `OK` | DMN evaluates to `P2` → manual verification → fulfilment | Historical v6 PASS — `orderTotal=2500`, `deliveryPriority=P2`; Tasklist task created for `demo` | Live instance `2251799813729160` (v6) |
| L3-01 | Triage | `GREEN`; slot available | DMN evaluates to `Routine` → slot available | Historical v6 PASS — `triageCategory=Routine`, `bookingStatus=SLOT_AVAILABLE`; completed | Live instance `2251799813728973` (v6) |
| L3-02 | Triage | `AMBER`; slot available | DMN evaluates to `Priority Review` → staff review → slot available | Historical v6 PASS — `triageCategory=Priority Review`; Tasklist task created for `demo` | Live instance `2251799813728459` (v6) |
| L3-03 | Triage | `RED` | Immediate clinical assessment | Historical v6 PASS — `triageCategory=Urgent Assessment`; Tasklist task created for `demo` | Live instance `2251799813729005` (v6) |
| L3-04 | Triage | `GREEN`; `slotMode=none` | Staff scheduling review | PASS — `availableSlots=[]`, `bookingStatus=NO_SLOTS`; Tasklist task created for `demo` | Live instance `2251799813728502` (v6) |
| L3-05 | Triage | Switch scheduling mock off before starting | Incident → switch mock on → retry | PASS — scheduling incident created, mock restored, Retry completed; `bookingStatus=SLOT_AVAILABLE` | Live instance `2251799813729583` (v6) |
| L3-06 | Triage | Missing patient reference or urgency indicator | Invalid-registration end before decision/scheduling | PASS — `registrationValid=false`; completed before decision/scheduling | Live instance `2251799813728516` (v6) |

## Prior v7 live verification

The three BPMN and three DMN resources deployed to Camunda 8.8.32 as version 7. All nine cases below were started against that deployment; the displayed DMN results and routes were verified in Operate. These runs precede the stricter required-input checks documented below. `ACTIVE` means the process is waiting at a modeled user task, not that it failed. Every instance reported `hasIncident=false`.

| Lab | Input case | Verified API/DMN result | Instance | State |
|---|---|---|---|---|
| Loan | Manual review: income 35,000; amount 600,000; score 640 | `applicationValid=true`; `creditScore=640`; `loanDecision=Manual Review` | `2251799813739499` | ACTIVE — manual review |
| Loan | Approved: income 60,000; amount 300,000; score 720 | `applicationValid=true`; `loanDecision=Approved` | `2251799813739973` | COMPLETED |
| Loan | Rejected: income 25,000; amount 900,000; score 580 | `applicationValid=true`; `loanDecision=Rejected` | `2251799813740014` | COMPLETED |
| Order | P1: quantity 2; unit price 6,000; GOLD/EXPRESS | `orderValid=true`; `orderTotal=12000`; `deliveryPriority=P1`; payment approved | `2251799813739520` | ACTIVE — order verification |
| Order | P2: quantity 2; unit price 1,500; STANDARD/EXPRESS | `orderTotal=3000`; `deliveryPriority=P2` | `2251799813740047` | ACTIVE — order verification |
| Order | P3: quantity 1; unit price 500; STANDARD/STANDARD | `orderTotal=500`; `deliveryPriority=P3`; payment approved | `2251799813740090` | COMPLETED |
| Triage | Routine: GREEN; routine follow-up | `registrationValid=true`; `triageCategory=Routine` | `2251799813739545` | COMPLETED |
| Triage | Priority: AMBER | `registrationValid=true`; `triageCategory=Priority Review` | `2251799813740154` | ACTIVE — staff review |
| Triage | Urgent: RED | `registrationValid=true`; `triageCategory=Urgent Assessment` | `2251799813740214` | ACTIVE — clinical assessment |

## Fresh v8 live verification

The three BPMN and three DMN resources deployed to Camunda 8.8.32 as version 8. These instances were started using scenario inputs only; output variables were calculated by REST or DMN tasks. Every instance reported `hasIncident=false`.

| Lab | Input case | Verified result / route | Instance | State |
|---|---|---|---|---|
| Loan | Approved: income 60,000; amount 300,000; score 720 | `applicationValid=true`; `creditScore=720`; `creditStatus=CLEAR`; DMN `loanDecision=Approved` | `2251799813750843` | COMPLETED |
| Order | P1: quantity 2; unit price 6,000; GOLD/EXPRESS; token `OK` | `orderValid=true`; `stockAvailable=true`; `orderTotal=12000`; `deliveryPriority=P1`; API `paymentStatus=APPROVED` | `2251799813750864` | ACTIVE — manual verification |
| Order | Payment token `DECLINE` | Payment-declined path | `2251799813750888` | COMPLETED |
| Order | Quantity 25 | Out-of-stock path | `2251799813750959` | COMPLETED |
| Order | Missing payment token | Invalid-order path | `2251799813751033` | COMPLETED |
| Triage | GREEN; routine follow-up; slot available | DMN `triageCategory=Routine`; scheduling completed | `2251799813751074` | COMPLETED |
| Triage | GREEN; `slotMode=none` | Scheduling returns no slots; staff review path | `2251799813751105` | ACTIVE — staff scheduling review |
| Triage | RED | DMN urgent-assessment route | `2251799813751153` | ACTIVE — clinical assessment |

## Current source checks

- `mvn test`: 6 API unit tests cover required loan score, order inputs/payment token, calculated payment status, registration/slot mode, and calculated order total/stock outputs.
- BPMN/DMN XML and decision references validate; all six REST service tasks have the REST template, request mappings, result expressions, and R-icon metadata.
- The validation APIs reject missing/unsupported score, payment token, or slot mode instead of supplying default positive/approved/available outcomes.
- Start examples contain only process inputs. Result variables such as `paymentStatus`, `orderValid`, and `stockAvailable` are returned by REST tasks and are not accepted as scenario inputs.
- Restart Spring Boot after model changes to deploy the current BPMN/DMN files. The v6/v7 keys above are historical; the v8 keys are the latest live evidence.
- Capture presentation screenshots from your own Camunda run before submission. For Tasklist proof, complete the waiting review tasks; Operate shows their calculated DMN result variables.

## Starting instances

In PowerShell, run the matching command below to create an instance. The response includes its process instance key. Then view it in Operate; handle any review task in Tasklist. These inputs are synthetic.

**Lab 1 — approved loan**

```powershell
$body = '{"processDefinitionId":"loan-approval","variables":{"applicantId":"APP-1001","monthlyIncome":60000,"requestedAmount":300000,"syntheticCreditScore":720}}'
Invoke-RestMethod -Uri 'http://localhost:8080/v2/process-instances' -Method Post -ContentType 'application/json' -Body $body
```

**Lab 2 — standard order**

```powershell
$body = '{"processDefinitionId":"order-fulfilment","variables":{"customerRef":"CUST-1001","productId":"SKU-001","quantity":2,"unitPrice":500,"customerCategory":"STANDARD","deliveryPreference":"STANDARD","paymentToken":"OK"}}'
Invoke-RestMethod -Uri 'http://localhost:8080/v2/process-instances' -Method Post -ContentType 'application/json' -Body $body
```

**Lab 3 — routine appointment**

```powershell
$body = '{"processDefinitionId":"patient-triage","variables":{"patientRef":"PAT-1001","appointmentPreference":"MORNING","symptomsSummary":"Routine follow-up","urgencyIndicator":"GREEN","slotMode":"available"}}'
Invoke-RestMethod -Uri 'http://localhost:8080/v2/process-instances' -Method Post -ContentType 'application/json' -Body $body
```

For Tasklist demonstrations, use the corresponding branch inputs from the test matrix: Lab 1 manual review (income `35000`, amount `600000`, score `640`); Lab 2 verification (quantity `2`, unit price `6000`, category `GOLD`, preference `EXPRESS`, which gives DMN priority `P1`); Lab 3 priority review (`AMBER`) or clinical assessment (`RED`). The [Presentation Guide](PRESENTATION_GUIDE.md) explains how to inspect inputs and the DMN result variables.

## Incident setup

For each incident row, open its integration service control URL from the [REST Connector Guide](REST_CONNECTOR_GUIDE.md) with `down=true` **before** starting the process. Leave `simulateFailure` omitted or set to `false`. When the incident appears in Operate, open the matching control URL with `down=false`, then retry the failed job. The retry uses the original process variables.
