# Process Variable Dictionary

## Lab 1 — Loan approval

| Variable | Type | Purpose |
|---|---|---|
| `applicantId` | String | Synthetic applicant reference |
| `requestedAmount`, `monthlyIncome` | Number | Eligibility and loan-decision inputs |
| `syntheticCreditScore`, `creditScore` | Number | Mock API input and returned score |
| `applicationValid` | Boolean | Worker validation result |
| `creditStatus` | String | `CLEAR` or `REFER` from mock API |
| `loanDecision` | String | DMN output: `Approved`, `Manual Review`, or `Rejected` |
| `simulateFailure` | Boolean | Deliberately returns a mock HTTP 503 for recovery testing |

## Lab 2 — E-commerce fulfilment

| Variable | Type | Purpose |
|---|---|---|
| `customerRef`, `productId` | String | Synthetic order identity |
| `quantity`, `unitPrice`, `orderTotal` | Number | Stock request and computed total |
| `customerCategory`, `deliveryPreference` | String | DMN priority inputs |
| `orderValid`, `stockAvailable` | Boolean | Worker validation and inventory result |
| `deliveryPriority` | String | DMN output: `P1`, `P2`, or `P3` |
| `paymentToken`, `paymentStatus`, `transactionRef` | String | Mock payment request and result |
| `simulateFailure` | Boolean | Deliberately returns a mock HTTP 503 |

## Lab 3 — Appointment and triage

| Variable | Type | Purpose |
|---|---|---|
| `patientRef` | String | Synthetic patient reference only |
| `appointmentPreference` | String | Requested appointment preference |
| `symptomsSummary`, `urgencyIndicator` | String | Educational triage inputs; `RED`, `AMBER`, or other |
| `registrationValid` | Boolean | Registration worker result |
| `triageCategory` | String | DMN output: `Routine`, `Priority Review`, or `Urgent Assessment` |
| `slotMode` | String | Mock scheduling request: `available` or `none` |
| `slotAvailable`, `availableSlot`, `availableSlots` | Boolean/String/List | Mock scheduling API response |
| `bookingStatus` | String | Mock response: `SLOT_AVAILABLE` or `NO_SLOTS`; this simulation checks availability and does not reserve a real appointment |
| `simulateFailure` | Boolean | Deliberately returns a mock HTTP 503 |
