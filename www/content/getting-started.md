---
title: "Getting Started"
date: 2023-11-08T04:30:58+01:00
icon: sparkles-outline
---
## What is Penna?
Penna is a fast and lightweight [slf4j](https://www.slf4j.org/) implementation that writes structured json
log messages to stdout.

## When should I use Penna?
Penna is specially useful for when you run you applications on [docker](https://www.docker.com/) containers or [kubernetes](https://kubernetes.io/) pods and your log is captured from stdout to a log aggregator like [loki](https://grafana.com/oss/loki/) or [the elastic stack](https://www.elastic.co/elastic-stack/).

## Adding Penna to your project

1. Add [the latest version](https://central.sonatype.com/artifact/com.hkupty.penna/penna-core/overview) of `penna-dev` to your dependency manager.

2. Enjoy! Penna comes with sane defaults so no initial configuration is needed

## What if I want to change the configuration?

Then you need to add [`penna-yaml-config`](https://central.sonatype.com/artifact/com.hkupty.penna/penna-yaml-config/0.7.0) to your project, alongside jackson-dataformat-yaml:

```gradle
runtimeOnly 'com.hkupty.penna:penna-yaml-config:<latestPennaVersion>'
runtimeOnly 'com.fasterxml.jackson.core:jackson-core:<latestJacksonVersion>'
runtimeOnly 'com.fasterxml.jackson.core:jackson-databind:<latestJacksonVersion>'
runtimeOnly 'com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:<latestJacksonVersion>'
```

And then you can configure a `resources/penna.yaml` file like this:
```yaml
penna:
  loggers:
    com.mycompany.myapp:
        level: debug
    com.vendor.noisylib:
        level: warn
```
<!--more-->

## Can I change more parameters in the configuration?

As mentioned before, penna comes with a set of good defaults so you don't have to worry about them, but if you
really want to tweak some parameters, you can.
```yaml
penna:
  loggers:
    com.mycompany.myapp:
        level: debug
        fields:
            # These are the default fields. You can remove some fields if you want to
            - level
            - logger
            - thread
            - data
            - mdc
            - markers
            - message
            - throwable
        exceptions:
            # Whether penna will try to avoid printing duplicated log messages.
            # By default, this feature is on, which means once it prints a log message it
            # has seen before, it will short-circuit out of printing that stack trace
            deduplicate: true
            # The maximum number of frames in the stack it will print
            maxDepth: 64
            # If the exceptions contains parents, what is the maximum depth
            # Penna will traverse.
            traverseDepth: 2
```
