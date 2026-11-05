<div align="center">

<img src="assets/workflow-labs-banner.svg" alt="Camunda 8 Workflow Automation Labs" width="100%" />

![Camunda 8](https://img.shields.io/badge/Camunda-8-0F766E?style=flat-square&logo=camunda&logoColor=white)
![Java](https://img.shields.io/badge/Java-21-0F172A?style=flat-square&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5-16A34A?style=flat-square&logo=springboot&logoColor=white)
![BPMN](https://img.shields.io/badge/BPMN-3%20models-2563EB?style=flat-square)
![REST decisions](https://img.shields.io/badge/REST-Input--driven%20decisions-7C3AED?style=flat-square)

**End-to-end workflow automation for loan approval, e-commerce fulfilment, and appointment triage.**

</div>

---

## What’s inside

This submission implements all three practical-assignment labs using a consistent Camunda 8 architecture. Each lab combines a BPMN process, Java job workers, Camunda's REST Outbound Connector, a local REST API that calculates decisions from the process inputs, Tasklist hand-offs, and an incident-recovery exercise. The three DMN files are retained as reference materials, but the running BPMN processes do not call them.

| Lab | Scenario | BPMN | Runtime decision API | Human task |
|:--:|:--|:--|:--|:--|
| 01 | Loan approval | [Open model](src/main/resources/models/lab-1-loan-approval.bpmn) | Income + amount + credit score → loan decision | Loan review |
| 02 | E-commerce fulfilment | [Open model](src/main/resources/models/lab-2-order-fulfilment.bpmn) | Order total + category + preference → priority | Order verification |
| 03 | Hospital appointment & triage | [Open model](src/main/resources/models/lab-3-patient-triage.bpmn) | Urgency indicator → triage category | Clinical / scheduling review |

> **Decision behavior:** the REST decision endpoints calculate a result for each request from that process instance's input variables. The DMN tables are not used by the executable workflows.

> **Data safety:** all examples are synthetic. This repository contains no real payment details, credentials, patient data, or personal information.

## Architecture at a glance

```text
Process input ──► BPMN orchestration ──► Java job worker
                         │                       │
                         ▼                       ▼
             REST Outbound Connector ──► Mock REST API
                         │               (decision + integrations)
                         ▼
              process variables ──► BPMN gateways
                         │
                         ▼
                 Tasklist / Operate
```

<p align="center">
  <img src="assets/workflow-landscape.png" alt="Conceptual workflow landscape for the three lab scenarios" width="100%" />
  <br />
  <sub><i>Conceptual workflow landscape — executable BPMN models call the local REST APIs; DMN files are reference-only.</i></sub>
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

- Java 21
- [Camunda 8 Run 8.8](https://downloads.camunda.cloud/release/camunda/c8run/8.8/) already unzipped
- Maven 3.9+; the installed copy on this machine is `C:\Users\ditod\Downloads\apache-maven-3.10.0`

### 2. Start Camunda, then the worker

Terminal 1, in PowerShell:

```powershell
Set-Location "$HOME\Downloads\camunda8-run-8.8-windows-x86_64\c8run-8.8.32"
.\c8run.exe start
```

- Operate: http://localhost:8080/operate
- Tasklist: http://localhost:8080/tasklist
- Login: `demo` / `demo`

Terminal 2, in PowerShell:

```powershell
Set-Location "$HOME\OneDrive\Desktop\dataset\BPMN_Exercise_4"
& "$HOME\Downloads\apache-maven-3.10.0\bin\mvn.cmd" spring-boot:run
```

The application deploys the models and exposes the synthetic APIs at `http://localhost:8081/mock/*`. Leave both terminals open during the demo.

### 3. Run the three labs

Create each process instance with the PowerShell commands in the [Test Matrix](docs/TEST_MATRIX.md). Use Operate to monitor instances and recover incidents; use Tasklist to complete the human review tasks assigned to `demo`. Incident recovery steps are in the [REST Connector Guide](docs/REST_CONNECTOR_GUIDE.md).

## Lab snapshots

<details open>
<summary><b>01 · Loan approval</b></summary>

`Validate applicant → credit REST API → input-driven loan-decision REST API → approve / reject / manual review`

The decision endpoint evaluates the submitted income, requested amount, and returned synthetic credit score. Invalid applications close early; borderline applications create a Tasklist review.
</details>

<details>
<summary><b>02 · E-commerce fulfilment</b></summary>

`Validate & reserve stock → input-driven priority REST API → payment REST API → fulfilment / verification`

Orders with unavailable stock or declined payment end cleanly. High-value orders route through an explicit manual-verification task.
</details>

<details>
<summary><b>03 · Hospital appointment & triage</b></summary>

`Validate registration → input-driven triage REST API → mock scheduling API → available / staff review`

`RED` cases go immediately to clinical assessment. No available slot creates a scheduling-review task. This is an educational workflow only—not medical advice or clinical decision support.
</details>

## Documentation

- [Implementation report](docs/IMPLEMENTATION_REPORT.md) — architecture, results approach, assumptions, and limits
- [Variable dictionary](docs/VARIABLE_DICTIONARY.md) — variable names, types, and purpose
- [REST connector guide](docs/REST_CONNECTOR_GUIDE.md) — connector configuration and request/response examples
- [Test matrix](docs/TEST_MATRIX.md) — required normal, exception, and recovery scenarios
- [Evidence and screenshots](evidence/README.md) — genuine Operate screenshots for all three labs and remaining evidence checklist
- [Presentation guide](docs/PRESENTATION_GUIDE.md) — exact sample inputs, REST request/response values, and where to show them in Modeler, Operate, and Tasklist

## Incident recovery demo

To demonstrate an incident, switch the relevant mock service off before starting the process, then switch it back on after the incident appears in Operate. Retry the failed job:

```text
Switch mock off → run instance → Operate incident → switch mock on → retry job → complete instance
```

The integration connectors are configured with one attempt so the incident appears quickly; decision connectors allow three attempts. Retry repeats the HTTP request with the same process variables, so use the mock controls rather than changing `simulateFailure` on the failed instance. Record the incident and successful retry in the test matrix. The [evidence folder](evidence/README.md) contains genuine Operate screenshots from an earlier local run; capture fresh screenshots for this updated deployment.

## Repository description

> Camunda 8 workflow automation labs for loan approval, e-commerce order fulfilment, and appointment triage, with Spring Boot workers, REST Connector tasks, input-driven decision APIs, Tasklist review, and incident recovery.

## Technology note

The project uses the official Camunda Spring Boot Starter and modern `@JobWorker` model deployment approach. Refer to the [Camunda Spring Boot Starter documentation](https://docs.camunda.io/docs/apis-tools/camunda-spring-boot-starter/getting-started/) for runtime and connection options.
