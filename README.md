<div align="center">

<img src="assets/workflow-labs-banner.svg" alt="Camunda 8 Workflow Automation Labs" width="100%" />

![Camunda 8](https://img.shields.io/badge/Camunda-8-0F766E?style=flat-square&logo=camunda&logoColor=white)
![Java](https://img.shields.io/badge/Java-21-0F172A?style=flat-square&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5-16A34A?style=flat-square&logo=springboot&logoColor=white)
![BPMN](https://img.shields.io/badge/BPMN-3%20models-2563EB?style=flat-square)
![DMN](https://img.shields.io/badge/DMN-3%20tables-7C3AED?style=flat-square)

**End-to-end workflow automation for loan approval, e-commerce fulfilment, and appointment triage.**

</div>

---

## What’s inside

This submission implements all three practical-assignment labs using a consistent Camunda 8 architecture. Each lab combines a BPMN process, a DMN decision, Java job workers, a REST integration route, Tasklist hand-offs, and a defined incident-recovery exercise.

| Lab | Scenario | BPMN | DMN decision | Human task |
|:--:|:--|:--|:--|:--|
| 01 | Loan approval | [Open model](src/main/resources/models/lab-1-loan-approval.bpmn) | `Approved` / `Rejected` / `Manual Review` | Loan review |
| 02 | E-commerce fulfilment | [Open model](src/main/resources/models/lab-2-order-fulfilment.bpmn) | `P1` / `P2` / `P3` delivery priority | Order verification |
| 03 | Hospital appointment & triage | [Open model](src/main/resources/models/lab-3-patient-triage.bpmn) | `Routine` / `Priority Review` / `Urgent Assessment` | Clinical / scheduling review |

> **Data safety:** all examples are synthetic. This repository contains no real payment details, credentials, patient data, or personal information.

## Architecture at a glance

```text
Process input ──► BPMN orchestration ──► Java job worker ──► process variables
                         │                       │
                         ▼                       ▼
                    DMN decision            Mock REST API
                         │                       │
                         └──► gateways ──► Tasklist / completion
                                              │
                                              ▼
                                       Operate incident recovery
```

<p align="center">
  <img src="assets/workflow-landscape.png" alt="Conceptual workflow landscape for the three lab scenarios" width="100%" />
  <br />
  <sub><i>Conceptual workflow landscape — the executable BPMN and DMN models are linked in the lab table above.</i></sub>
</p>

## Repository map

```text
BPMN_Exercise_4/
├── src/main/resources/models/      # Three BPMN models and three DMN tables
├── src/main/java/                  # Spring Boot app, workers, mock APIs
├── docs/                           # API guide, variables, report, test matrix
├── evidence/                       # Add your own Tasklist / Operate screenshots here
├── assets/                         # README visual assets
├── pom.xml
└── README.md
```

## Quick start

### 1. Prerequisites

- Java 21 (or a compatible JDK ≥17)
- Maven 3.9+
- A reachable Camunda 8 Self-Managed cluster, or adapt `application.yml` for your Camunda SaaS cluster

### 2. Start the worker and mock API

```bash
mvn spring-boot:run
```

The application deploys models on startup and exposes local synthetic integration endpoints at `http://localhost:8081/mock/*`.

### 3. Configure the REST tasks

Open each BPMN model in Camunda Modeler and apply the connector fields in [REST Connector Guide](docs/REST_CONNECTOR_GUIDE.md). This is intentionally documented separately so the cluster-specific URL is easy to adapt.

### 4. Deploy, execute, evidence

Deploy the BPMN and DMN resources, then run the scenarios in [Test Matrix](docs/TEST_MATRIX.md). Complete user tasks in Tasklist; inspect variables, incidents, and retries in Operate. Add screenshots from your own running environment to [`evidence/`](evidence/README.md).

## Lab snapshots

<details open>
<summary><b>01 · Loan approval</b></summary>

`Validate applicant → mock credit assessment → DMN decision → approve / reject / manual review`

The DMN uses educational income, loan amount, and synthetic credit-score thresholds. Invalid applications close early; borderline applications create a Tasklist review.
</details>

<details>
<summary><b>02 · E-commerce fulfilment</b></summary>

`Validate & reserve stock → DMN priority → mock payment → fulfilment / verification`

Orders with unavailable stock or declined payment end cleanly. High-value orders route through an explicit manual-verification task.
</details>

<details>
<summary><b>03 · Hospital appointment & triage</b></summary>

`Validate registration → educational DMN triage → mock scheduling → book / staff review`

`RED` cases go immediately to clinical assessment. No available slot creates a scheduling-review task. This is an educational workflow only—not medical advice or clinical decision support.
</details>

## Documentation

- [Implementation report](docs/IMPLEMENTATION_REPORT.md) — architecture, results approach, assumptions, and limits
- [Variable dictionary](docs/VARIABLE_DICTIONARY.md) — variable names, types, and purpose
- [REST connector guide](docs/REST_CONNECTOR_GUIDE.md) — connector configuration and request/response examples
- [Test matrix](docs/TEST_MATRIX.md) — required normal, exception, and recovery scenarios
- [Evidence checklist](evidence/README.md) — exact screenshots to capture before submission

## Incident recovery demo

Every mock endpoint accepts `simulateFailure: true`. This responds with an intentional HTTP 503 so you can demonstrate the complete recovery path:

```text
Run instance → connector retries exhaust → Operate incident → correct input/service → retry job → complete instance
```

Record the incident and successful retry in the test matrix. The repository does **not** include fake screenshots; submission evidence should be captured from your own deployment.

## Repository description

> Camunda 8 practical assignment: BPMN + DMN workflow automation for loan approval, e-commerce order fulfilment, and hospital appointment triage, with Spring Boot workers, mock REST APIs, Tasklist review, and incident recovery.

## Technology note

The project uses the official Camunda Spring Boot Starter and modern `@JobWorker` model deployment approach. Refer to the [Camunda Spring Boot Starter documentation](https://docs.camunda.io/docs/apis-tools/camunda-spring-boot-starter/getting-started/) for runtime and connection options.
