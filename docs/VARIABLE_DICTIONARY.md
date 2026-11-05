# Process Variable Dictionary

## Lab 1 — Loan approval

| Variable | Type | Purpose |
|---|---|---|
| `applicantId` | String | Synthetic applicant reference |
| `requestedAmount`, `monthlyIncome` | Number | Eligibility and loan-decision inputs |
| `syntheticCreditScore`, `creditScore` | Number | Mock API input and returned score |
| `applicationValid` | Boolean | Loan validation REST API result |
| `creditStatus` | String | `CLEAR` or `REFER` from mock API |
| `loanDecision` | String | Loan DMN output: `Approved`, `Manual Review`, or `Rejected`, evaluated from income, amount, and credit score |
| `simulateFailure` | Boolean | Deliberately returns a mock HTTP 503 for recovery testing |

## Lab 2 — E-commerce fulfilment

| Variable | Type | Purpose |
|---|---|---|
| `customerRef`, `productId` | String | Synthetic order identity |
| `quantity`, `unitPrice`, `orderTotal` | Number | Stock request and computed total |
| `customerCategory`, `deliveryPreference` | String | REST priority-decision inputs |
| `orderValid`, `stockAvailable` | Boolean | Order validation REST API results |
| `deliveryPriority` | String | Order DMN output: `P1`, `P2`, or `P3`, evaluated from order total, category, and delivery preference |
| `paymentToken`, `paymentStatus`, `transactionRef` | String | Mock payment request and result |
| `simulateFailure` | Boolean | Deliberately returns a mock HTTP 503 |

## Lab 3 — Appointment and triage

| Variable | Type | Purpose |
|---|---|---|
| `patientRef` | String | Synthetic patient reference only |
| `appointmentPreference` | String | Requested appointment preference |
| `symptomsSummary`, `urgencyIndicator` | String | Educational triage inputs; `RED`, `AMBER`, or other |
| `registrationValid` | Boolean | Registration validation REST API result |
| `triageCategory` | String | Triage DMN output: `Routine`, `Priority Review`, or `Urgent Assessment`, evaluated from urgency and symptoms inputs |
| `slotMode` | String | Mock scheduling request: `available` or `none` |
| `slotAvailable`, `availableSlot`, `availableSlots` | Boolean/String/List | Mock scheduling API response |
| `bookingStatus` | String | Mock response: `SLOT_AVAILABLE` or `NO_SLOTS`; this simulation checks availability and does not reserve a real appointment |
| `simulateFailure` | Boolean | Deliberately returns a mock HTTP 503 |
