# ADR-3 Core model naming

## Context

In process engine implementations, we refer to processes or workflows, made up of activities, and executed by cases.
However, not all developers have used processes and workflows as an execution model.
We can initially focus on microservice orchestration by using naming based on script and service execution.

## Decision

Use programmer-centric naming based on microservices and script execution, instead of process engine naming.

## Status

Accepted

## Consequences

* Developers will understand the model more quickly but will not assume standard process engine functionality like asynchronous parallel execution.
* We can produce documentation that explains the engine for people more familiar with workflow or BPM.
* We can present the engine differently for use cases other than microservice orchestration.
