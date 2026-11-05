# Implementation Report

## Architecture

Each lab uses BPMN for orchestration, a DMN decision table for transparent routing, Java job workers for validation, and a local Spring Boot mock API for REST calls. The process engine owns workflow state; workers return only process variables. Human-exception paths are deliberately represented as Tasklist user tasks.

## Implementation notes

- **Loan approval:** validates core application data, requests a synthetic credit assessment, then applies documented income / amount / score rules.
- **Order fulfilment:** validates an order and simulated stock, determines fulfilment priority, applies a mock payment result, and routes `P1`/`P2` DMN outcomes to verification while `P3` proceeds to fulfilment.
- **Patient triage:** validates the registration through an explicit BPMN gateway, uses synthetic patient references and educational urgency indicators, and routes routine/priority/urgent decisions from the DMN output. The mock scheduling API returns a slot list and availability status; it is not a clinical decision-support system or a real booking system.

## Monitoring and recovery

Set `simulateFailure` to `true` for the relevant mock request. The REST task returns HTTP 503, exhausting the service task retries and creating an incident. Inspect the failed job and variables in Operate, correct the input or mock availability, then retry the job. Capture the incident and post-retry instance state for submission.

## Assumptions and limitations

- The local mocks are deterministic teaching integrations, not production services.
- DMN thresholds are educational rules chosen for reproducible demonstrations.
- Approval, payment, and booking events are simulated; no money, identity, or patient records are processed.
- Screenshots must be captured from the student's own Camunda environment and added to `evidence/` before submission.
