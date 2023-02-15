# Principles

Maple is a SLF4J native logger that is designed with a few principles as basis:

## Structured logging
[_metadata_:established-version]:- "0.1"
[_metadata_:target-version]:- "1.0"
Logging is supposed to provide meaningful information and, with the evolution of log processing platforms,
it is oftentimes difficult to convey the right information as written, natural text, in a way that it
both makes sense for humans and is easy for machines to process.

Instead, we should embrace the notion that logs are effectively data and should be treated as such.

## Lightweight configuration
[_metadata_:established-version]:- "0.1"
[_metadata_:target-version]:- "1.0"

Setting up complicated XML files or multiple nested properties to be able to see log messages in a screen is
never fun and oftentimes takes a lot of time. Instead, we want logs to be working by default, out of the box.
The default implementation will log every piece of information as structured json data to stdout.
Some configuration is still required, like which is the default level, for which loggers.
Other properties can be changed, optionally, such as the destination of the logs, or formatting properties
(though sane defaults are set out of the box).

## Runtime toggleable
[_metadata_:established-version]:- "0.1"
[_metadata_:target-version]:- "1.0"
There are a few situations where we want the logs to be on, or off, according to variables like traffic volume,
rate of failure, time of the day or % of resources being used.
While figuring out those rules is something out of the scope of Maple, being able to toggle different log levels
to different loggers programmatically should definitely be in scope.

# Architecture

Maple architecture consists of basically default implementations of SLF4j interfaces, with a few extra components
on top:

## ProxyLogger

This is a simple logger class that proxies the log calls to the actual implementation.
It is needed to allow for runtime hotswap of the actual loggers.
When a logger is completely disabled, it proxies to a [NOPLogger](https://www.slf4j.org/apidocs/org/slf4j/helpers/NOPLogger.html).
If a logger level is set for that logger, it gets one implementation of either TraceLogger, DebugLogger, InfoLogger, WarnLogger or ErrorLogger.
Those implementations are also simple facades that emit a [LogEvent](https://www.slf4j.org/apidocs/org/slf4j/event/LoggingEvent.html)
in a [Publisher](https://docs.oracle.com/javase/9/docs/api/java/util/concurrent/Flow.Publisher.html).
A [Processor](https://docs.oracle.com/javase/9/docs/api/java/util/concurrent/Flow.Processor.html) then translates the
`LogEvent` to Json a structure, which is forward to a [Subscriber](https://docs.oracle.com/javase/9/docs/api/java/util/concurrent/Flow.Subscriber.html)
that writes it to the output stream.

## SingletonConfig

A straightforward API that allows for configuring the logger behavior. It can and should be accessible directly through
importing.