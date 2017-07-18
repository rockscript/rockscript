# ADR-5 Engine API protocol

## Context

Microservice architectures can choose from many API protocols.
Some messaging protocols offer more efficient communication between services.
HTTP’s widespread popularity makes adoption easiest, due to its simplicity as well as its familiarity.

## Decision

The engine API uses HTTP by default.

## Status

Accepted

## Consequences

* Developers use HTTP in different ways, and we will have to choose an API style.
* Using HTTP may limit performance, so we may want to support other (message-based) protocols in future.
