# Script authoring

## Context

Microservice orchestration scripts express both sequential and parallel script actions.
Developers find it easier to write scripts that use a familiar language.
Developers find it easier to read and maintain more declarative script languages.
A custom language syntax creates a learning burden for developers.
The JSON-based syntax ([ADR-4](004 JSON script syntax.md)) provides a language-neutral fallback and basis for other tools.

## Decision

Developers will use language drivers as the primary script authoring interface.

## Status

Superseded by [ADR-13](013 JavaScript-based script language.md).

## Consequences

* This means initially focusing on programmatic script authoring, rather than declarative authoring.
* Consider a web-based editor with autocompletion and language support for IDEs and text editors.
* Graphical or web-based editing will require additional tooling.
* Language drivers must include some kind of script-builder API that constructs scripts from language elements.
