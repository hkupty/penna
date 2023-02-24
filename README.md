# Maple

[![version](https://img.shields.io/maven-central/v/com.hkupty.maple/maple-core?style=flat-square)]()

Maple is an opinionated backend for [slf4j](https://github.com/qos-ch/slf4j/) that focuses on doing one thing right: Logging structured logs in json format to the console.

## Warning!

Maple is currently in alpha and, while usable, *has not been tested in production yet*.

Please use with caution. Feedback, however, is very welcome.

## Principles

### Structured logging

Logging is supposed to provide meaningful information and, with the evolution of log processing platforms,
it is oftentimes difficult to convey the right information as written, natural text, in a way that it
both makes sense for humans and is easy for machines to process.

Instead, we should embrace the notion that logs are effectively data and should be treated as such.

## Lightweight configuration

Maple comes packed with a sane defaults configuration that allows one to plug it and start using immediately.
Although configuration is possible, by rolling with the shipped defaults one can already reap the benefits of structured
logging without having to set up any configuration.


### Unobtrusiveness

The logging framework should not draw out much attention. It should just work.
With that in mind, maple tries to be a simple yet effective component in your architecture.
It should not require you to add in more dependencies. Instead, it should work with whatever you have available.
Also, it should make its best effort to not consume a lot of resources, being efficient and sparing your app of GC pauses
when under heavy load.

## Roadmap

- 1.0
- [ ] Stable API
- [ ] Configuration mechanism for fine tuning logs based on logger name;
- [ ] Json sinks:
  - [x] Jackson
  - [ ] Gson
  - [ ] Jakarta
- [ ] 40-50% Test coverage

- 2.0
- [ ] Tests
  - [ ] Add [arch unit](https://www.archunit.org/) tests
  - [ ] Add generative tests
  - [ ] 70-80% test coverage
- [ ] Transform log messages:
  - [ ] Allow for custom (through configuration) transformation of log before it is rendered
- [ ] Native implementation:
  - [ ] MDC
  - [ ] Markers
