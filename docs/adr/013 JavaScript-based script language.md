# ADR-013 JavaScript-based script language

## Context

[ADR-7](007 Script authoring.md) chose language drivers for script authoring.
We later realised that a library-based DSL in Java or JavaScript for generating the JSON would not offer a great developer experience.
JavaScript offers the most familiar and friendly syntax.

## Decision

We will implement a custom script language using the JavaScript syntax, but with different execution semantics to make asynchronous execution the default.

## Status

Accepted

## Consequences

* We will implement a language parser in Antlr using the ECMAscript grammar.
* The script language will not support all ECMAscript features, in general.
