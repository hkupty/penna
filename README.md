# Penna
![Penna](logo/logo_banner.svg)
> a contour feather, a penna; a quill, a feather used for writing; a pen; a pencil.

[![version](https://img.shields.io/maven-central/v/com.hkupty.penna/penna-core?style=flat-square)](https://mvnrepository.com/artifact/com.hkupty.penna)
[![Maintainability](https://api.codeclimate.com/v1/badges/646db2db253b2610143d/maintainability)](https://codeclimate.com/github/hkupty/penna/maintainability)
[![CircleCI](https://dl.circleci.com/status-badge/img/gh/hkupty/penna/tree/dev/0.9.svg?style=svg)](https://dl.circleci.com/status-badge/redirect/gh/hkupty/penna/tree/dev/0.9)

Penna is an opinionated backend for [slf4j](https://github.com/qos-ch/slf4j/) that focuses on doing one thing right: Logging structured logs in json format to the console.

## A word of caution

Penna is considered to be in beta stage.
It has been observed to be stable, no reports of data loss (loss of log messages) or data corruption (malformed/unprocessable log messages).
Yet, since no major system has reported using Penna under high load, it is only theoretically stable.
Usage is advised with caution when rolling back to an alternate backend is easy.

It would be immensely appreciated if real world usages of Penna were reported in [discussions](https://github.com/hkupty/penna/discussions) section, so we can speed up promoting Penna to 1.x and consider it completely stable.

## Why use Penna?

Penna is an alternative to Logback, designed for a specific use case: **structured logging** straight to the console.

This is particularly useful for JVM applications running in **Kubernetes**, where structured logs are often preferred.

If this is your use case, you might prefer Penna over Logback because:

- **Purpose-built**: Penna is specialized for this use case, working out of the box with sane defaults.
- **Future-proof**: Penna is safe against the [Y2038 bug](https://hkupty.github.io/penna/blog/epochalypse/).
- **Zero extra dependencies**: It does not require any JSON library (or any dependency other than SLF4J).
- **Optimized for performance**: It delivers impressive speed compared to Logback.
- **Minimal memory footprint**: Designed to avoid GC pressure, making it highly efficient.
- **Easy configuration**: If needed, the optional penna-yaml-config extension allows configuration via YAML — a format more native to Kubernetes environments.

That said, **Penna is not a full Logback replacement**.
If you need **multiple log formats**, file-based logging, or other complex logging targets, Logback might still be the right tool.


## Usage

Penna is a backend for slf4j, so you don't need to interact with it directly.

In order to use it, add it to the [build manager of your preference](https://mvnrepository.com/artifact/com.hkupty.penna/penna-core/0.9.2), for example:

```groovy
// slf4j won't detect Penna if it's not packaged as `implementation`
implementation 'com.hkupty.penna:penna-core:0.9.2'

// Penna doesn't have any strict dependencies aside from slf4j.
implementation 'org.slf4j:slf4j-api:2.0.17'
```

:warning: Note that Penna is built targeting JVM 21+.

By default, you will get log level `INFO` enabled as well as the following fields:
- `timestamp`
- `level`
- `message`
- `logger`
- `thread`
- `mdc`
- `markers`
- `data` (slf4j's 2.0 `.addKeyValue()`)
- `throwable`

If you want to configure it, Penna provides a separate convenience library for configuring your log levels in yaml files:
```yaml
# resources/penna.yaml
---
# Since version 0.8, penna-yaml-config supports setting up a file watcher
# so any updates to this file will be reflected immediately
watch: true
loggers:
    # All the loggers under `com.yourapp` will be configured to debug level.
    com.yourapp: { level: debug }
    org.noisylibrary: { level: warn }
```

If you want to use [penna-yaml-config](penna-yaml-config/README.md), you have to add it as a dependency:

```groovy
runtimeOnly 'com.hkupty.penna:penna-yaml-config:0.9.2'

// penna-yaml-config is a thin layer and uses a yaml parsing libray under the hood.
// You can chose among jackson, snakeyaml (yaml 1.1) or snakeyaml engine (yaml 1.2)

// Jackson
runtimeOnly 'com.fasterxml.jackson.core:jackson-core:2.18.3'
runtimeOnly 'com.fasterxml.jackson.core:jackson-databind:2.18.3'
runtimeOnly 'com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.18.3'

// Snakeyaml
runtimeOnly 'org.yaml:snakeyaml:2.4'

// Snakeyaml engine
runtimeOnly 'org.snakeyaml:snakeyaml-engine:2.9'
```

## Principles

### Structured logging

Logging is supposed to provide meaningful information and, with the evolution of log processing platforms,
it is oftentimes difficult to convey the right information as written, natural text, in a way that it
both makes sense for humans and is easy for machines to process.

Instead, we should embrace the notion that logs are effectively data and should be treated as such.

### Lightweight configuration

Penna comes packed with a sane defaults configuration that allows one to plug it and start using immediately.
Although configuration is possible, by rolling with the shipped defaults one can already reap the benefits of structured
logging without having to set up any configuration.

### Unobtrusiveness

The logging framework should not draw out much attention. It should just work.
With that in mind, Penna tries to be a simple yet effective component in your architecture.
It should not require you to add in more dependencies. Instead, it should work with whatever you have available.
Also, it should make its best effort to consume the fewer resources as possible, being efficient and sparing your app of GC pauses
when under heavy load. [Read more on our performance tests.](performance/)

## Star History

[![Star History Chart](https://api.star-history.com/image?repos=hkupty/penna&type=date&legend=top-left)](https://www.star-history.com/?repos=hkupty%2Fpenna&type=date&legend=top-left)
