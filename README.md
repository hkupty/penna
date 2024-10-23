# Penna
![Penna](logo/logo_full.svg)
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

Penna presents itself as an alternative to [logback](https://logback.qos.ch/).
It is designed for a specific use case: When you want to have [structured logging](https://stackify.com/what-is-structured-logging-and-why-developers-need-it/), straight to the console.
This might be a common use-case for jvm apps running in kubernetes.
If that is your use case, you might prefer Penna over logback because:

- Penna is specialized for this use-case, working out of the box with sane defaults;
- It does not require any json library (or any dependency other than slf4j);
- It is very optimized, with impressive performance when compared to logback;
- It is also designed not consume almost any runtime memory, so it won't cause GC pressure;
- If you want to configure, the extension config library [penna-yaml-config](penna-yaml-config/README.md) allows you to configure Penna in yaml,
which might be a more native configuration format for its runtime environment (i.e. kubernetes);

However, Penna doesn't try to replace logback for all its use cases. If you have to log in multiple formats, to a file or any other target, logback might still be your tool of choice.


## Usage

Penna is a backend for slf4j, so you don't need to interact with it directly.

In order to use it, add it to the [build manager of your preference](https://mvnrepository.com/artifact/com.hkupty.penna/penna-core/0.7.0), for example:

```groovy
// gradle
runtimeOnly 'com.hkupty.penna:penna-core:0.8.1'

// Penna doesn't have any strict dependencies aside from slf4j.
implementation 'org.slf4j:slf4j-api:2.0.12'
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
runtimeOnly 'com.hkupty.penna:penna-yaml-config:0.8.1'

// penna-yaml-config is a thin layer and uses a yaml parsing libray under the hood.
// You can chose among jackson, snakeyaml (yaml 1.1) or snakeyaml engine (yaml 1.2)

// Jackson
runtimeOnly 'com.fasterxml.jackson.core:jackson-core:2.17.0'
runtimeOnly 'com.fasterxml.jackson.core:jackson-databind:2.17.0'
runtimeOnly 'com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.17.0'

// Snakeyaml
runtimeOnly 'org.yaml:snakeyaml:2.2'

// Snakeyaml engine
runtimeOnly 'org.snakeyaml:snakeyaml-engine:2.7'
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
