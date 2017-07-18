# ADR-2 Standalone server

## Context

Java developers can use a server component implemented in Java as an embedded JAR dependency or as a separate server with a network interface.
An embedded library can offer a more convenient and simpler API but only for a Java application.
Offering both Java and network interfaces splits development focus and risks one API having second-class status.
Finally, we can more easily present a server as a tangible and hard-to-reproduce product.

## Decision

We will build a strand alone server with a single public network API, rather than a library.

## Status

Accepted

## Consequences

* We will have to choose a network API protocol and architectural style.
* The network layer will impact performance and testability, which we can address with a custom test framework.
* System developers can use a server as a separate microservice.
* We can develop client libraries for different programming languages on an equal footing.
* For some use cases, we will have to consider making the server available as an embedded library in the future.
