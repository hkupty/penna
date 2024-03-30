---
title: "JSON logs for the JVM"
date: 2024-01-31T11:58:57+01:00
description: "Logging JSON from SLF4J"
tags:
    - java
    - json
    - logging
---

## How to output JSON logs?

Use Penna. I know, you're used to setting up XML files and configuring encoders, formatters and whatnot for having your logs output in JSON, so it sounds too simple, right?

Well, it is, but it wouldn't be deserving an article if it was that obvious. The hard part here is that Penna is not as known as Log4j or Logback, so naturally you're skeptical.

So, by the end of article, you'll surely have enough confidence on what Penna is and what problems it solves that you'd be willing to give it a try.


<!-- more -->

## Why JSON logs?

Over the past years systems have increased in both complexity and throughput so it is fundamental that, when observing, we can gather information in a way that debugging is efficient.
This means that we most likely want to add as much information as possible like log level, thread, class source and many runtime/dynamic information extracted from the context, like
user id, trace id, etc. This is fundamental to allow tools like [loki](https://grafana.com/oss/loki/) or [Elastic stack](https://www.elastic.co/elastic-stack) to index the logs so you can cross-correlate information and
group them in different dimensions for both debugging and measuring your applications. This task becomes increasingly hard when the logs are stored in a structured-text format, where
we would need to opt between human-readability or ease of processing.

JSON logs are a good middleground as it is a format we're used to interact with, easy enough to parse and allows for a dynamic log structure.

## What is Penna?

> Penna is a native SLF4J backend, modern codebase, designed for the specialized task of outputting JSON log messages to stdout. It's meant to be easy to configure and fast.

There's a whole article if you're interested in [why I came up with Penna]({{<ref "/blog/why-penna.md" >}} "Why Penna?"). What it is now is defined in the sentence above, which we should unpack as it tells a lot about it, its philosophy and its direction.

### Native SLF4J Backend

Different from some contenders like Log4j, Penna is built from ground up based on SLF4J interfaces and APIs. This means it is a single dependency and there are no adapter layers between them. The fact that it is native is a benefit as it sits side-by-side with logback in being fast and making the best use possible of the SLF4J API.

### Modern codebase

Penna's 0.x series is written in Java17 and the next 1.x versions will be targetting JDK21. It makes use of modern java features and, therefore, leverages the recent performance optimizations. It is also a great chance for the community to leave behind the java 8 days with a codebase that is easier to read and reason about, working both as a showcase of what modern java is capable of as well as an invitation for apps to modernize themselves.

### Writes JSON logs to console

A common use case nowadays is to write structured logs in JSON format to the console. Then, they'd be picked up by kubernetes, docker or other container runtimes and from those the logs will ship to aggregators like Loki, Elasticsearch/Elastic stack, etc. This means that the flexibility and tooling around for managing log files or pattern formatting are unnecessary for that usual scenario. Surely, other scenarios are likely required, like logs going to files, kafka streams, smtp or databases. They can easily be handled by an external tool reading from the stdout file descriptor if one wants to, whereas the opposite would be much more complicated. Embracing simplicity and reducing the complexity on the logger end reduces the chance of exploits and vulnerabilities, aligns with the [Unix philosophy](https://en.wikipedia.org/wiki/Unix_philosophy) of doing one thing well and, as a bonus, enables good performance.

### Easy set up

Most of the configuration is done beforehand so you don't need to do anything. Yet, if you feel like changing details in the configuration, it's configuration mechanism is reachable through a yaml file using `penna-yaml-config` as yaml is likely a more native configuration format to your application. This means that out-of-the-box your logger will know exactly what to do, which is to write `INFO`, `WARN` and `ERROR` logs to stdout. If you need a certain logger to output `DEBUG` or want to tone down a noisy vendor `INFO` logger, you can achieve that through a file format that is native to kubernetes or akin to your docker-compose files.

### Performant, fast, unobtrusive

Penna is designed to be small, slim and specialized; It's meant to not get in your way, so it has no reason to be taxing your application with runtime penalty. Altough not intensively optimized, Penna can boast its high throughput and low memory footprint. In fact, it's constant memory consumption for most use-cases. As mentioned above, doing one thing and being native to SLF4J opens up great opportinities for performance improvements. If you're interested in this subject, I go deeply in the performance aspects of Penna in a series of articles [starting from this one]({{< ref "/blog/performance-insights-branchless.md" >}}).

## How to use Penna?

The only line you need to add to your project is the one below if you want to get started with Penna straight away:

```gradle
runtimeOnly 'com.hkupty.penna:penna-core:<latestPennaVersion>'
```

For more details, check the [getting started guide]({{< ref "getting-started.md" >}} "Getting Started"), which explains how to set up the yaml config as well.

## Closing thoughts

That was simple, right? I hope by now you have a better grasp of Penna.

Let me know your thoughts! Community feedback and support is, of course, always welcome! Don't refrain from opening a [ticket](https://github.com/hkupty/penna/issues) or starting a [discussion](https://github.com/hkupty/penna/discussions) if you have ideas, thoughts or issues! Consider subscribing to my [polar.sh page](https://polar.sh/hkupty), where I dive deeper in the development of Penna!
