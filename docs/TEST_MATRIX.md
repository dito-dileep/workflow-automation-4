# Test Matrix

Run each row after deployment and fill the two evidence columns with the actual instance key and screenshot filename. This preserves an honest record of your own execution.

| ID | Lab | Synthetic input / setup | Expected route | Actual result | Evidence |
|---|---|---|---|---|---|
| L1-01 | Loan | Income 60,000; amount 300,000; score 720 | Approved | Pending execution | — |
| L1-02 | Loan | Missing `applicantId` | Invalid-data rejection | Pending execution | — |
| L1-03 | Loan | Income 35,000; amount 600,000; score 640 | Manual loan review → approve | Pending execution | — |
| L1-04 | Loan | `simulateFailure=true` at credit connector | Incident → correct flag → retry | Pending execution | — |
| L2-01 | Order | Qty 2; unit price 500; token `OK` | Stock → payment → fulfilment | Pending execution | — |
| L2-02 | Order | Qty 25 | Stock unavailable end | Pending execution | — |
| L2-03 | Order | Qty 1; token `DECLINE` | Payment declined end | Pending execution | — |
| L2-04 | Order | Qty 2; unit price 6,000; token `OK` | Manual verification → fulfilment | Pending execution | — |
| L2-05 | Order | `simulateFailure=true` at payment connector | Incident → correct flag → retry | Pending execution | — |
| L3-01 | Triage | `GREEN`; slot available | Routine → appointment booked | Pending execution | — |
| L3-02 | Triage | `AMBER`; slot available | Priority review path → booked | Pending execution | — |
| L3-03 | Triage | `RED` | Immediate clinical assessment | Pending execution | — |
| L3-04 | Triage | `GREEN`; `slotMode=none` | Staff scheduling review | Pending execution | — |
| L3-05 | Triage | `simulateFailure=true` at scheduling connector | Incident → correct flag → retry | Pending execution | — |
