# Changelog

All notable changes to Penna will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## 0.8.1

### `penna-api`
- Fix version reporting in audit message([#121](https://github.com/hkupty/penna/pull/121)) - Thanks [@marksailes](https://github.com/marksailes)!

## 0.8.0

### `penna-api`

#### Changed

- Replace configuration interfaces([#89](https://github.com/hkupty/penna/pull/89))
- Restructure namespaces([#95](https://github.com/hkupty/penna/pull/95))
- Add audit logger([#97](https://github.com/hkupty/penna/pull/97))

### `penna-core`

#### Changed

- Replace internal MDC storage implementation([#83](https://github.com/hkupty/penna/pull/83))
- Remove ad-hoc configuration mechanism([#89](https://github.com/hkupty/penna/pull/89))
- Remove sink proxy([#94](https://github.com/hkupty/penna/pull/94))
- Minor cleanups and adjusts for JDK21 ([#94](https://github.com/hkupty/penna/pull/94))
- Provide optional runtime controls([#95](https://github.com/hkupty/penna/pull/95))
- Remove Minilogger([#97](https://github.com/hkupty/penna/pull/97))

### `penna-yaml-config`

#### Changed

- Restructure configuration; implement new interface([#89](https://github.com/hkupty/penna/pull/89))
- Use Virtual Thread for the File watcher([#96](https://github.com/hkupty/penna/pull/96))

## 0.7.2

### `penna-core`

#### Changed

- Performance improvements in internal mechanisms ([#72](https://github.com/hkupty/penna/pull/72))
- Replace logger storage([#72](https://github.com/hkupty/penna/pull/72), [#74](https://github.com/hkupty/penna/pull/74), [#75](https://github.com/hkupty/penna/pull/75) & [#76](https://github.com/hkupty/penna/pull/76))


## 0.7.1

### `penna-core`

#### Changed

- Restructured internal controls for better thread safety ([#70](https://github.com/hkupty/penna/pull/70))

## 0.7

### `penna-api`

#### Changed

- Add `ExcaptionHandling` configuration ([#53](https://github.com/hkupty/penna/pull/53))
- Log fields internally return byte arrays instead of strings ([#59](https://github.com/hkupty/penna/pull/59))
- `ExceptionHandling` allow for configuring maximum depth ([#59](https://github.com/hkupty/penna/pull/59))

### `penna-core`

#### Added

- Penna MDC Adapter ([#55](https://github.com/hkupty/penna/pull/55))

#### Changed

- Allow sinks to read config ([#51](https://github.com/hkupty/penna/pull/51))
- Configure stacktrace depth and frame deduplication ([#53](https://github.com/hkupty/penna/pull/53))
- Optimize keyword handling in json logs ([#56](https://github.com/hkupty/penna/pull/56))
- Format logs only when writting the output ([#56](https://github.com/hkupty/penna/pull/56))
- Refactor structure of native MDC ([#59](https://github.com/hkupty/penna/pull/59))
- Move sinks and restructure output creation ([#59](https://github.com/hkupty/penna/pull/59))
- Refactor integer to ascii logic ([#59](https://github.com/hkupty/penna/pull/59))
- Various misc improvements for performance ([#59](https://github.com/hkupty/penna/pull/59))
- Fix formatting for null arguments([#62](https://github.com/hkupty/penna/pull/62))

### `penna-yaml-config`

#### Changed

- Allow for configuring exception handling ([#53](https://github.com/hkupty/penna/pull/53))

### `penna-dev`

#### Added

- Create dev runtime with enhanced readability for logs ([#61](https://github.com/hkupty/penna/pull/61))

#### Changed

- Fix formatting for null arguments([#62](https://github.com/hkupty/penna/pull/62))

## 0.6.2 - 2023-04-12

### `penna-api`

### `penna-core`

#### Changed

- Refactor buffer expansion to avoid breaking on un-accounted for large strings

### `penna-yaml-config`

## 0.6.1 - 2023-03-31

Hotfix release to fix the MDC writing issue.

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
