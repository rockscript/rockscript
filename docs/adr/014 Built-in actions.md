# ADR-14 Built-in actions

## Context

As well as custom external actions, scripts will support core actions for basic functionality, such as sending HTTP requests.
An HTTP action could use a Java Service Provider Interface (SPI), which would mean including its dependencies on the classpath.
In general, it gets harder to include more dependencies on the classpath without version clashes.
Alternatively, we could implement all actions, including core actions, as separate HTTP or message-based services with a fixed service API.
Using HTTP or messaging services for core actions introduces another layer and more latency to the architecture.

## Decision

We integrate a small number of core actions via an SPI.

## Status

Accepted

## Consequences

Core libraries in the first version must have compatible dependencies.
