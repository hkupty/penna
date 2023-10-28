# Contributing

## Project guidelines

Penna is build from the ground up with two principles in mind:

- Sane defaults/lightweight configuration;
- Unobtrusiveness/lightweight runtime;

The third pillar, structured logging, can also be interpreted as "specialized", in the sense of being focused to 
deliver a great solution for json log messages.
This means that penna shouldn't do more than it needs. The `penna-core` module holds the necessary code for the runtime
and nothing else. There's no need to add more functionality there than necessary.
Accessory code, like additional/opinionated layouts (for example [ECS](https://www.elastic.co/guide/en/ecs/8.10/ecs-reference.html))
should be distributed as a separate jar, added on top of the conventional `penna-core`.
This can be seen by in the `penna-dev` project, replacing entirely the configurations and the formatting for a better development
experience.

### Runtime granularity

The fundamental logic that enables penna to be lightweight is that we should pay the costs associated with our decisions
as early as possible, and as close as possible to when/where it was made.

Picking a log format, for example, is a decision that happens at _bundling time_, when the code is being packaged
in a jar for deployment, for example. So it doesn't make sense to pay this cost at _"write this log to stdout"-time_.

The closest we can get to that is by making the initialization of the logger factory pick up, from the modules/jars
available, what is the format to be used.

Another example of this philosophy is when determining which levels are enabled and which levels are not.
This happens at _configuration time_, not at _print log time_. If we can know - and we can - when the configuration
changes, we can determine the log levels there and not touch it again until we change the log configuration.

This is why the logger uses the [LevelGuards](../penna-core/src/main/java/penna/core/logger/guard/LevelGuard.java).

Another cost that can be avoided at the most critical time is allocating the log object. We can pool and reuse them.

So throughout the codebase, whenever you see something that you can think "Why is this done so early?" or "Why is this
not done at 'log time'?", it is because that is the right "time" to do that.

### Performance as a functional requirement

Penna is designed to be "unobtrusive", a word that is very generic and seldom used in programming.

The choice of this word is intentional as it captures both the throughput aspect (penna should be fast enough,
so we can't notice it running) and the memory usage aspect (even under load, if I measure my application,
I won't notice penna).

Logging is one of the most critical aspects of observing an application, like a health monitor, so it shouldn't 
be as stable as possible, as predictive as possible and should not fail when the application critically needs it.

Increased memory consumption, when under load, snowballs and slows down the application, so the mere act of trying
to signal an error can cause your application to fight for resources, increasing GC pressure.

Of course, not all applications are in the extreme levels, but as we go towards sharing machines in the cloud,
it is not unheard of running several JVMs in a small machine. When the resources are limited, being
lean makes a huge difference.

### Opinionated

The typical use-case for penna is running a JVM app inside a pod in a kubernetes cluster, where the logs are shipped
to loki (or any other log aggregator). Other scenarios like "I want to log to a file" are supported by logback
already. Penna is a specialized library and seeks to continue being.

## How can I help penna?

Penna is very young and doesn't have enough production-hours, meaning that there can be hidden bugs, so testing
penna is a great way of helping!

Also, if you want to get your hands dirty in the code, opening a PR is always welcome. Note a few things:

- Be mindful of the performance. As stated above, performance is a functional requirement, and it can be difficult to
accommodate new features if they impact performance.
- Don't do style changes "just because". A few decisions were made causing the code to look uglier, but have some
benefit in other areas like performance or simplified logic elsewhere.
- It is completely fine opening a draft PR and asking for help. Don't refrain from asking questions mid-way.

Finally, if you have ideas or suggestions, the [discussions](https://github.com/hkupty/penna/discussions) section
is the right place to start.