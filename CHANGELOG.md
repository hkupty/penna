# Changelog

All notable changes to Penna will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html) with an addendum:
the PATCH component is omitted when its value is `0`.

## [Unreleased]

### `penna-api`

### `penna-core`

#### Changed

- Remove unnecessary "..." from stacktrace if we break out of the loop due to repetition ([#41](https://github.com/hkupty/penna/pull/41))

#### Fixed

- MDC block was not outputting a comma after running, producing an invalid json ([#41](https://github.com/hkupty/penna/pull/41))

### `penna-yaml-config`

## 0.6 - 2023-03-30

### `penna-api`

### `penna-core`

#### Added

- Internal json writer (no 3rd party dependency); ([#29](https://github.com/hkupty/penna/pull/29))
- Native sink using internal writer; ([#29](https://github.com/hkupty/penna/pull/29))
- `StackTraceFilter` for caching already printed stacktrace lines; ([#29](https://github.com/hkupty/penna/pull/29))

#### Changed

- Added `suppressed` throwables to the logged exceptions; ([#29](https://github.com/hkupty/penna/pull/29))
- Minilogger now logs in json; ([#39](https://github.com/hkupty/penna/pull/39))

#### Removed

- `JacksonSink`, `GsonSink` and `JakartaSink` sinks are removed in favor of the native sink; ([#35](https://github.com/hkupty/penna/pull/35))

#### Fixed

- Clock thread name; ([#29](https://github.com/hkupty/penna/pull/29))

### `penna-yaml-config`

#### Changed

- Added pmd and static analysis checks to `penna-yaml-config`; ([#39](https://github.com/hkupty/penna/pull/39))

#### Fixed

- Fix reading yaml config from `penna.yaml` inside the jar; ([#38](https://github.com/hkupty/penna/pull/38))
