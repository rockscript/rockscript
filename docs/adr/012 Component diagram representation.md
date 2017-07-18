# Component diagram representation

## Context

Architects need to understand the role that the execution engine plays in their microservice architectures.
Describing standard architecture patterns will help adoption.

## Decision

In system architectures, as represented by component diagrams, we represent the engine a separate component, as with a database.
We draw the engine as a cube, in the same way that component diagrams represent databases as cylinders.

## Status

Proposed

## Consequences

Architects may choose to model the engine as a single service, in a microservice architecture, or as a dependency embedded within a single microservice.
