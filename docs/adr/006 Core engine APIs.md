# Core engine APIs

## Context

The core engine could use various mechanisms for script deployment, execution, extension and queries.
File-based configuration offers convenience while a public APIs decouples specific implementations.
Meanwhile, shared data access typically offers the best performance at the cost of tight coupling to a specific model.

## Decision

The core engine has three public APIs: for script deployment, execution runtime, and activity plugin APIs.

## Status

Accepted

## Consequences

* Querying historical data will use another mechanism.
* The activity plugin API implementation may require a Java-only API.
