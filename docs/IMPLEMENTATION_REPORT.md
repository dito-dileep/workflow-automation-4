# Implementation Report

## Architecture

Each lab uses BPMN to orchestrate Camunda REST Outbound Connector tasks, DMN decision tables, and Tasklist user tasks. The Spring Boot application hosts synthetic APIs for input validation and mock external integrations. Every DMN is called from its BPMN business-rule task and evaluates that instance's variables; its output is used by a BPMN gateway.

## Implementation notes

- **Loan approval:** a validation REST call checks the applicant ID and positive income/amount. A credit REST call returns the synthetic score. The loan DMN evaluates income, requested amount, and returned score; its output routes to approval, rejection, or manual review.
- **Order fulfilment:** the first REST call validates order fields, calculates `orderTotal`, and evaluates synthetic stock availability. The order DMN uses total, customer category, and delivery preference to produce `deliveryPriority`. A payment REST call returns the payment result; P1/P2 orders route to verification.
- **Patient appointment and triage:** a registration REST call checks required fields. The triage DMN evaluates urgency and symptoms inputs and routes routine, priority, or urgent cases. The scheduling REST mock returns a slot list and availability; it is not clinical decision support or a real booking system.

## Monitoring and recovery

Credit, payment, and scheduling mocks can be switched off to make their REST tasks fail. Operate then exposes the failed connector job for retry after restoring the mock. The request inputs are unchanged on retry. Capture the incident and post-retry state in the test matrix.

## Assumptions and limitations

- The APIs are deterministic teaching mocks, not production services.
- DMN rules are illustrative and evaluate variables supplied by each process instance.
- Approval, payment, and booking are simulated; no money, identity, or patient records are processed.
- Triage is not medical advice or a clinical decision-support system.
- Screenshots must be captured from the student's Camunda environment and added to `evidence/`.
