# Changelog

All notable changes to Penna will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html) with an addendum:
the PATCH component is omitted when its value is `0`.

## [Unreleased]

### `penna-api`

### `penna-core`

#### Added

- Internal json writer (no 3rd party dependency); ([#29](https://github.com/hkupty/penna/pull/29))
- Native sink using internal writer; ([#29](https://github.com/hkupty/penna/pull/29))
- `StackTraceFilter` for caching already printed stacktrace lines; ([#29](https://github.com/hkupty/penna/pull/29))

#### Changed

- Added `suppressed` throwables to the logged exceptions; ([#29](https://github.com/hkupty/penna/pull/29))

#### Removed

- `JacksonSink`, `GsonSink` and `JakartaSink` sinks are removed in favor of the native sink; ([#35](https://github.com/hkupty/penna/pull/35))

#### Fixed

- Clock thread name; ([#29](https://github.com/hkupty/penna/pull/29))

### `penna-yaml-config`