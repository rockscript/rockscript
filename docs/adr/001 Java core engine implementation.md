# ADR-1 Java core engine implementation

## Context

We can choose between popular server development languages that we have experience with.
The newest languages offer different programming styles, such as functional programming, and potential benefits compared to older languages.
Meanwhile, established languages such as Java offer a much richer enterprise integration ecosystem, and far wider adoption in the enterprise environments that we expect to target.

We like building server software Java and do not know of compelling reasons to avoid it.
Besides, an existing team will typically achieve the most success with the language or technology they have the most experience with.

## Decision

We will implement the core engine in Java.

## Status

Accepted

## Consequences

We will select a Java HTTP API to use.
