# Language drivers

## Context

Supporting multiple programming languages will help adoption, and make the engine available to the most developers.
Moreover, language-specific client APIs can provide a better developer experience than language independent APIs.
However, supporting multiple languages will slow down initial development.
Developing initially for only one language risks tying the implementation to that language.
We can prioritise the most popular languages.

## Decision

Write language drivers for Java and JavaScript (i.e. ECMAScript 2017) to start with.

## Status

Superseded by [ADR-13](013 JavaScript-based script language.md).

## Consequences

* Each language driver must include APIs for script authoring, deployment, execution and testing.
* Writing driver APIs for languages both with and without static types may lead to multiple strategies for ensuring script correctness at runtime.
