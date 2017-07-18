# Script action input-output naming

## Context

A script action, such as an HTTP request, has inputs and outputs, such as request and response headers, respectively.
The script action syntax could name these `inputs` and `outputs`, so that different action types have consistent syntax.
Alternatively, the syntax could use the natural input and output names for the action type.
Developers who know enough about HTTP to use an HTTP script action will mostly likely already know that an HTTP request corresponds to script action input.

## Decision

Script actions use their own context to name inputs and outputs, such as `request` and `response` for HTTP actions.

## Status

Accepted

## Consequences

* Action type documentation will describe parameters as input or output parameters.
* Activity pluggability will also have to deal with input and output.
