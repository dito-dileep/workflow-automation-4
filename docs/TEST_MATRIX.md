# Test Matrix

Run each row after deployment and fill the two evidence columns with the actual instance key and screenshot filename. This preserves an honest record of your own execution.

| ID | Lab | Synthetic input / setup | Expected route | Actual result | Evidence |
|---|---|---|---|---|---|
| L1-01 | Loan | Income 60,000; amount 300,000; score 720 | Approved | Pending execution | — |
| L1-02 | Loan | Missing `applicantId` | Invalid-data rejection | Pending execution | — |
| L1-03 | Loan | Income 35,000; amount 600,000; score 640 | Manual loan review → approve | Pending execution | — |
| L1-04 | Loan | Switch credit mock off before starting | Incident → switch mock on → retry | Pending execution | — |
| L2-01 | Order | Qty 2; unit price 500; token `OK` | Stock → payment → fulfilment | Pending execution | — |
| L2-02 | Order | Qty 25 | Stock unavailable end | Pending execution | — |
| L2-03 | Order | Qty 1; token `DECLINE` | Payment declined end | Pending execution | — |
| L2-04 | Order | Qty 2; unit price 6,000; token `OK` | Manual verification → fulfilment | Pending execution | — |
| L2-05 | Order | Switch payment mock off before starting | Incident → switch mock on → retry | Pending execution | — |
| L3-01 | Triage | `GREEN`; slot available | Routine → appointment booked | Pending execution | — |
| L3-02 | Triage | `AMBER`; slot available | Priority review path → booked | Pending execution | — |
| L3-03 | Triage | `RED` | Immediate clinical assessment | Pending execution | — |
| L3-04 | Triage | `GREEN`; `slotMode=none` | Staff scheduling review | Pending execution | — |
| L3-05 | Triage | Switch scheduling mock off before starting | Incident → switch mock on → retry | Pending execution | — |

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

For Tasklist demonstrations, use the corresponding branch inputs from the test matrix: Lab 1 manual review (income `35000`, amount `600000`, score `640`); Lab 2 verification (quantity `2`, unit price `6000`, category `GOLD`, preference `EXPRESS`); Lab 3 priority review (`AMBER`) or clinical assessment (`RED`).

## Incident setup

For each incident row, open its service control URL from the [REST Connector Guide](REST_CONNECTOR_GUIDE.md) with `down=true` **before** starting the process. Leave `simulateFailure` omitted or set to `false`. When the incident appears in Operate, open the matching control URL with `down=false`, then retry the failed job. The retry uses the original process variables.
