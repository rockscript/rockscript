# ADR-4 JSON script syntax

## Context

Scripts define service orchestration declaratively, with different structures to existing programming languages.
Meanwhile, we support developers who use various languages.
Scripts could use a new language, or an existing data-serialisation format.
A new language would add overhead, both to implement and for developers to learn.
XML-based script languages teach us that we should avoid XML for this.
Meanwhile, despite its disadvantages, JSON is the new XML.
For now, at least.

## Decision

Use JSON types for the script syntax model.

## Status

Accepted

## Consequences

* Use JSON as the canonical script serialisation format.
* Language bindings (language drivers) use JSON types, e.g. via existing JSON libraries.
* Script deployment requires custom script validation (‘compilation’).
* The Java class model will include a representation of the script’s JSON types.
* A good developer experience requires a language driver API for script authoring.
* Consider creating a custom script DSL in the future.
