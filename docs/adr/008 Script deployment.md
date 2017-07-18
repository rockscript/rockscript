# Script deployment

## Context

The execution engine depends on scripts at runtime.
Similar technologies use various deployment mechanisms: including deploying scripts to a repository or providing script files on the Java classpath.
Deploying from the local file system simplifies deployment but lacks flexibility.
Deploying via an API decouples deployment from the runtime, but adds overhead.

## Decision

Developers will deploy scripts using the engine’s public API, typically as part of their own service start-up.

## Status

Proposed

## Consequences

* The server will persist script versions, so it can load the latest version.
* To avoid loading and parsing scripts for each case, the server will keep the current script version in memory.
* Engine distribution (clustering) must handle script updates.
* Script versioning must handle script updates.
* Developers will have to start the server before deploying scripts.
* Script deployment must use idempotent commands, so that services using a programmatic API can always attempt to deploy without having to know if they’ve deployed before.
* Consider also supporting file-based Java classpath deployment.
