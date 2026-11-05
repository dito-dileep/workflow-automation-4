# Implementation Report

## Architecture

Each lab uses BPMN to orchestrate Java job workers and Camunda REST Outbound Connector tasks. The decision connector sends process-instance inputs to the local Spring Boot API, which computes and returns a decision variable; BPMN gateways route on that response. The DMN tables are retained as reference artifacts but are not invoked by the executable processes. Human-exception paths are Tasklist user tasks.

## Implementation notes

- **Loan approval:** validates application data, requests a synthetic credit assessment, then sends income, requested amount, and returned credit score to the loan decision API. Its response routes to approval, rejection, or manual review.
- **Order fulfilment:** validates the order and simulated stock, calculates `orderTotal`, requests a delivery-priority decision from the API, then applies the mock payment result. Priority `P1` and `P2` route to verification; `P3` proceeds to fulfilment.
- **Patient appointment and triage:** validates registration, requests a category using the urgency indicator, and routes routine, priority, or urgent cases. The scheduling mock reports a slot list and availability; it is not clinical decision support or a real booking system.

## Monitoring and recovery

The integration mocks can be switched off to make a REST request fail. Operate then exposes the failed connector job for retry after restoring the mock. The request inputs are unchanged on retry. Capture the incident and post-retry state in the test matrix.

## Assumptions and limitations

- The APIs are deterministic teaching mocks, not production services.
- Decision thresholds are educational rules implemented by the API endpoints.
- DMN artifacts are retained for reference but do not determine runtime outcomes.
- Approval, payment, and booking are simulated; no money, identity, or patient records are processed.
- Screenshots must be captured from the student's Camunda environment and added to `evidence/`.
