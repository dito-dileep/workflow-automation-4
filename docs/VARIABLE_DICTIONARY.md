# Process Variable Dictionary

Only the **Start input** values belong in the process-instance creation request. The **Calculated output** values are created by the REST connector or DMN task; do not send them as start variables. Operate displays both categories together after a process has run.

## Lab 1 — Loan approval

| Variable | Type | Category and purpose |
|---|---|---|
| `applicantId` | String | Start input: synthetic applicant reference |
| `requestedAmount`, `monthlyIncome` | Number | Start input: eligibility and loan-decision values |
| `syntheticCreditScore` | Number | Start input: synthetic score for the credit mock; required range 300–850 |
| `applicationValid` | Boolean | Calculated output: loan validation REST API result |
| `creditScore` | Number | Calculated output: score returned by the credit REST API |
| `creditStatus` | String | Calculated output: `CLEAR` or `REFER` from the credit mock |
| `loanDecision` | String | Calculated output: loan DMN result (`Approved`, `Manual Review`, or `Rejected`) |
| `simulateFailure` | Boolean | Optional test input: deliberately returns a mock HTTP 503 |

## Lab 2 — E-commerce fulfilment

| Variable | Type | Category and purpose |
|---|---|---|
| `customerRef`, `productId` | String | Start input: synthetic order identity |
| `quantity`, `unitPrice` | Number | Start input: used to calculate total and check stock |
| `customerCategory`, `deliveryPreference` | String | Start input: order-priority DMN inputs |
| `paymentToken` | String | Start input: `OK` or `DECLINE`; selects the mock payment outcome |
| `orderValid`, `stockAvailable` | Boolean | Calculated outputs: order validation REST API results |
| `orderTotal` | Number | Calculated output: quantity × unit price |
| `deliveryPriority` | String | Calculated output: order DMN result (`P1`, `P2`, or `P3`) |
| `paymentStatus`, `transactionRef` | String | Calculated outputs: payment REST API response; never start inputs |
| `simulateFailure` | Boolean | Optional test input: deliberately returns a mock HTTP 503 |

## Lab 3 — Appointment and triage

| Variable | Type | Category and purpose |
|---|---|---|
| `patientRef` | String | Start input: synthetic patient reference only |
| `appointmentPreference` | String | Start input: requested appointment preference |
| `symptomsSummary`, `urgencyIndicator` | String | Start input: educational triage values; `RED`, `AMBER`, or other |
| `slotMode` | String | Start input: required mock scheduling choice, `available` or `none` |
| `registrationValid` | Boolean | Calculated output: registration validation REST API result |
| `triageCategory` | String | Calculated output: DMN result (`Routine`, `Priority Review`, or `Urgent Assessment`) |
| `slotAvailable`, `availableSlot`, `availableSlots` | Boolean/String/List | Calculated outputs: mock scheduling API response |
| `bookingStatus` | String | Calculated output: `SLOT_AVAILABLE` or `NO_SLOTS`; this simulation checks availability and does not reserve a real appointment |
| `simulateFailure` | Boolean | Optional test input: deliberately returns a mock HTTP 503 |
