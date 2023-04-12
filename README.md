# Penna
![Penna](logo/logo_full.svg)
> a contour feather, a penna; a quill, a feather used for writing; a pen; a pencil.

[![version](https://img.shields.io/maven-central/v/com.hkupty.penna/penna-core?style=flat-square)](https://mvnrepository.com/artifact/com.hkupty.penna)
[![Maintainability](https://api.codeclimate.com/v1/badges/646db2db253b2610143d/maintainability)](https://codeclimate.com/github/hkupty/penna/maintainability)
[![CircleCI](https://dl.circleci.com/status-badge/img/gh/hkupty/penna/tree/dev/0.6.2.svg?style=svg)](https://dl.circleci.com/status-badge/redirect/gh/hkupty/penna/tree/dev/0.6.2)

Penna is an opinionated backend for [slf4j](https://github.com/qos-ch/slf4j/) that focuses on doing one thing right: Logging structured logs in json format to the console.

## Warning!

Penna is currently in alpha and, while usable, *has not been tested in production yet*.

Please use with caution. Feedback, however, is very welcome.

## Why use penna?

Penna presents itself as an alternative to [logback](https://logback.qos.ch/).
It is designed for a specific use case: When you want to have [structured logging](https://stackify.com/what-is-structured-logging-and-why-developers-need-it/), straight to the console.
This might be a common use-case for jvm apps running in kubernetes.
If that is your use case, you might prefer penna over logback because:

- Penna is specialized for this use-case, working out of the box with sane defaults;
- It does not require any json library (or any dependency other than slf4j);
- It is very optimized, with impressive performance when compared to logback;
- It is also designed not consume almost any runtime memory, so it won't cause GC pressure;
- If you want to configure, the extension config library [penna-yaml-config](penna-yaml-config/README.md) allows you to configure penna in yaml,
which might be a more native configuration format for its runtime environment (i.e. kubernetes);

However, penna doesn't try to replace logback for all its use cases. If you have to log in multiple formats, to a file or any other target, logback might still be your tool of choice.


## Usage

Penna is a backend for slf4j, so you don't need to interact with it directly.

In order to use it, add it to the [build manager of your preference](https://mvnrepository.com/artifact/com.hkupty.penna/penna-core/0.6.2), for example:

```groovy
// gradle
runtimeOnly 'com.hkupty.penna:penna-core:0.6.2'

// Penna doesn't have any strict dependencies aside from slf4j.
implementation 'org.slf4j:slf4j-api:2.0.6'
```

:warning: Note that penna is built targeting JVM 17+.

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

Penna has support for logging also a `Counter` to each message, individually marking each message with a monotonically increasing
`long` counting from process startup, but that is disabled by default.

If you want to configure it, penna provides a separate convenience library for configuring your log levels in yaml files:
```yaml
# resources/penna.yaml

# This is for configuring the root level
penna:
  # We don't need to set level because by default it is set to INFO
  fields:
    # Not that it matter for json, but the key-value pairs below will be rendered in this order.
    # So, for human readability in the console, one can tweak the position of the fields:
    - level
    - logger
    - thread
    - data
    - mdc
    - markers
    - message
    - throwable
  loggers:
    # This map will match the logger with the same literal name and all its children loggers, so
    # com.mycompany.myapp as well as com.mycompany.myapp.controllers.MyGreatController and so on..
    com.mycompany.myapp:
      level: debug
      # There's no need to set fields here since it will inherit from the root logger
    com.vendor.noisylib:
      level: warn
      fields:
        # USE WITH CAUTION! You can opt to remove/add fields to the message in different loggers
        # In this example, we're removing `thread`, `data`, `mdc` and `markers` and adding the `counter` field.
        # This means the log messages from `com.vendor.noisylib` will be rendered differently.
        - level
        - logger
        - message
        - throwable
        - counter
```

If you want to use [penna-yaml-config](penna-yaml-config/README.md), you have to add it as a dependency:

```groovy
runtimeOnly 'com.hkupty.penna:penna-yaml-config:0.6.2'

// We have to add a yaml parser to the classpath for `penna-yaml-config` to work properly.
// Currently we only support `jackson-dataformat-yaml`, but we plan on adding support for other libraries.
runtimeOnly 'com.fasterxml.jackson.core:jackson-core:2.14.2'
runtimeOnly 'com.fasterxml.jackson.core:jackson-databind:2.14.2'
runtimeOnly 'com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.14.2'
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
With that in mind, penna tries to be a simple yet effective component in your architecture.
It should not require you to add in more dependencies. Instead, it should work with whatever you have available.
Also, it should make its best effort to consume the fewer resources as possible, being efficient and sparing your app of GC pauses
when under heavy load. [Read more on our performance tests.](performance/)
