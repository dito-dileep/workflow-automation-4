# Test Matrix

Run each row after deployment. The live instance references below are from the earlier v6 deployment, when the BPMN called DMN tables; they are historical baseline evidence, not proof of the updated REST decision flow. The current source tests cover the decision API outputs; rerun these cases after starting the updated Spring Boot application and record fresh instance keys. Capture genuine Modeler, Tasklist, and Operate screenshots from that run.

| ID | Lab | Synthetic input / setup | Expected route | Actual result | Evidence |
|---|---|---|---|---|---|
| L1-01 | Loan | Income 60,000; amount 300,000; score 720 | REST decision returns Approved | Historical v6 PASS — `creditStatus=CLEAR`; `loanDecision=Approved` | Live instance `2251799813728339` (v6) |
| L1-02 | Loan | Missing `applicantId` | Invalid-data rejection | PASS — `applicationValid=false`; completed at invalid-data end | Live instance `2251799813729143` (v6) |
| L1-03 | Loan | Income 35,000; amount 600,000; score 640 | REST decision returns Manual Review → approve | Historical v6 PASS — `loanDecision=Manual Review`; Tasklist task created for `demo` | Live instance `2251799813728328` (v6) |
| L1-04 | Loan | Switch credit mock off before starting | Incident → switch mock on → retry | PASS — credit incident created, mock restored, Retry completed; `loanDecision=Approved` | Live instance `2251799813729523` (v6) |
| L1-05 | Loan | Income 25,000; amount 900,000; score 550 | REST decision returns `Rejected` end | Historical v6 PASS — `loanDecision=Rejected`; completed | Live instance `2251799813728879` (v6) |
| L2-01 | Order | Qty 2; unit price 500; token `OK` | Stock → REST decision returns `P3` → payment → fulfilment | Historical v6 PASS — `orderTotal=1000`, `deliveryPriority=P3`, `paymentStatus=APPROVED`; completed | Live instance `2251799813728364` (v6) |
| L2-02 | Order | Qty 25 | Stock unavailable end | PASS — `stockAvailable=false`; completed without payment | Live instance `2251799813728903` (v6) |
| L2-03 | Order | Qty 1; token `DECLINE` | Payment declined end | PASS — `paymentStatus=DECLINED`; completed at declined end | Live instance `2251799813728926` (v6) |
| L2-04 | Order | Qty 2; unit price 6,000; category `GOLD`; preference `EXPRESS`; token `OK` | REST decision returns `P1` → manual verification → fulfilment | Historical v6 PASS — `orderTotal=12000`, `deliveryPriority=P1`; Tasklist task created for `demo` | Live instance `2251799813728350` (v6) |
| L2-05 | Order | Switch payment mock off before starting | Incident → switch mock on → retry | PASS — payment incident created, mock restored, Retry completed; `paymentStatus=APPROVED` | Live instance `2251799813729547` (v6) |
| L2-06 | Order | Missing `productId` or quantity 0 | Invalid-order end before stock/decision/payment | PASS — `orderValid=false`; completed at invalid-order end | Live instance `2251799813728378` (v6) |
| L2-07 | Order | Qty 1; unit price 2,500; preference `EXPRESS`; token `OK` | REST decision returns `P2` → manual verification → fulfilment | Historical v6 PASS — `orderTotal=2500`, `deliveryPriority=P2`; Tasklist task created for `demo` | Live instance `2251799813729160` (v6) |
| L3-01 | Triage | `GREEN`; slot available | REST decision returns `Routine` → slot available | Historical v6 PASS — `triageCategory=Routine`, `bookingStatus=SLOT_AVAILABLE`; completed | Live instance `2251799813728973` (v6) |
| L3-02 | Triage | `AMBER`; slot available | REST decision returns `Priority Review` → staff review → slot available | Historical v6 PASS — `triageCategory=Priority Review`; Tasklist task created for `demo` | Live instance `2251799813728459` (v6) |
| L3-03 | Triage | `RED` | Immediate clinical assessment | Historical v6 PASS — `triageCategory=Urgent Assessment`; Tasklist task created for `demo` | Live instance `2251799813729005` (v6) |
| L3-04 | Triage | `GREEN`; `slotMode=none` | Staff scheduling review | PASS — `availableSlots=[]`, `bookingStatus=NO_SLOTS`; Tasklist task created for `demo` | Live instance `2251799813728502` (v6) |
| L3-05 | Triage | Switch scheduling mock off before starting | Incident → switch mock on → retry | PASS — scheduling incident created, mock restored, Retry completed; `bookingStatus=SLOT_AVAILABLE` | Live instance `2251799813729583` (v6) |
| L3-06 | Triage | Missing patient reference or urgency indicator | Invalid-registration end before decision/scheduling | PASS — `registrationValid=false`; completed before decision/scheduling | Live instance `2251799813728516` (v6) |

## Current source checks

- `mvn test`: 4 REST decision API unit tests passed (loan approval/review/rejection, order P1/P2/P3, triage categories, and missing-input errors).
- BPMN/DMN XML parsed successfully; sequence-flow references and all six REST template tasks' required mappings and R-icon metadata were checked.
- These checks do not replace a fresh end-to-end run against the updated deployment. Capture new Operate and Tasklist evidence after redeploying.

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

For Tasklist demonstrations, use the corresponding branch inputs from the test matrix: Lab 1 manual review (income `35000`, amount `600000`, score `640`); Lab 2 verification (quantity `2`, unit price `6000`, category `GOLD`, preference `EXPRESS`, which gives REST priority `P1`); Lab 3 priority review (`AMBER`) or clinical assessment (`RED`). The [Presentation Guide](PRESENTATION_GUIDE.md) explains how to inspect the submitted inputs and REST decision outputs.

## Incident setup

For each incident row, open its integration service control URL from the [REST Connector Guide](REST_CONNECTOR_GUIDE.md) with `down=true` **before** starting the process. Leave `simulateFailure` omitted or set to `false`. When the incident appears in Operate, open the matching control URL with `down=false`, then retry the failed job. The retry uses the original process variables.
