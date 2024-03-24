# Performance

One of our core values is to be unobtrusive, which means we want to deliver logs fast and reduce the impact in the garbage
collector.

For us to achieve that, measuring (instead of guessing) is fundamental.

## Previous tests

[version 0.6 - Logger Performance](0.6-Logger.md)

[version 0.8 - Logger Performance](0.8-Logger.md)

[version 0.8 - Logger Creation Performance](0.8-LoggerCreation.md)

## Where are the tests?

If you want to run or check them, they're in [penna-core/src/jmh](../penna-core/src/jmh/) and can be tested by
running `gradle :penna-core:jmh`.
