---
title: "The importance of structured logging"
date: 2024-02-13T21:39:25+01:00
description: "Why should we log JSON instead of plain text"
tags:
    - java
    - json
    - logging
---

## Why log in JSON?

[In my previous article]({{< ref "/blog/json-logging-for-the-jvm.md" >}}) I wrote how to write your logs in JSON and gave a brief description to why you would want to do that,
but in this one I want to dive a little deeper in this subject as there are plenty of reasons to do that.

## What is structured logging?

You might recognize traditional log messages as strings containing a timestamp:

```text
2024-02-13T21:48:50 [main] INFO com.your.app.Logger - Your log message
```

This one line contains multiple pieces of data that have meaning and are formatted together in a human readable string.

If we were to break this string into a key-value pair format, what we'd get back would be something like this:

```text
timestamp:  2024-02-13T21:48:50
thread:     main
level:      INFO
logger:     com.your.app.Logger
message:    Your log message
```

Each of this pieces of data is important as it gives the developer context to monitor and debug a live app.

That log line is _structured_, because it communicates an event with a set of meaningul fields that can be interpreted as data.
However, by opting to use a string format, the keys for those values are positionally inferred.

## Why use JSON as format for structured logs then?

A perfectly valid but harder to understand log line could look something like this

```text
2024-02-13T21:48:50 [WARN] INFO message - debug
```

Well, we know that `[...]`, in this case, means thread name and what comes after the log level and before `-` is the logger name.
but that imposes some cognitive load onto the reader.

The same message can be unambiguously interpreted if written this way:

```json
{"timestamp": "2024-02-13T21:48:50", "thread": "WARN", "level": "INFO", "logger": "message", "message": "debug"}
```

Although not optimized for a quick read, we avoid having to parse the message fields in our heads to understand what each field means.

This is also a good balance between human-readable and machine-readable, as we make it simple for tools like [loki](https://grafana.com/oss/loki/) or [Elastic stack](https://www.elastic.co/elastic-stack) to process the log messages.

## What about MDC and additional context?

That is another benefit of having your structured logs in JSON.
While in the traditional formatted string you have to consider all the possible MDC values in your log template,
the JSON log messages can contain any number of additional context.

Consider [SLF4J](https://slf4j.org/)'s fluent api:

```java
MDC.put("userId", user.getId());
try {
    if (logger.isDebugEnabled()) {
        logger.atDebug()
            .addKeyValue("cartSize", cart.size())
            .log("Processing order");
    }
    // process order
} finally {
    MDC.remove("userId");
}
```

Having the ability to dynamically include both the `userId` and the `cartSize` in the log message is really a plus, but even better if we don't have to anticipate how adding those fields would (or could) affect readability:

```json
{"timestamp": "2024-02-13T22:21:18", "thread": "cart-worker-1", "level": "DEBUG", "logger": "com.myapp.orders.OrderProcessor",
"message": "Processing order", "mdc": {"userId": "01HPJ5KNHE35K0B2QA3FZ18KWT"}, "data": {"cartSize": 32}}
```

## How can Penna help?

Penna is a native SLF4J implementation designed to do one thing well: **Write JSON logs to stdout**. Leveraging structured logs is one of its core values.
Check our [getting started]({{< ref "getting-started.md" >}}) guide for updated information on how to add Penna to your application.
