# Performance

One of our core values is to be unobtrusive, which means we want to deliver logs fast and reduce the impact in the garbage
collector.

For us to achieve that, measuring (instead of guessing) is fundamental.

## Previous tests

[development/version 0.6](ca142e8a9d9dbdf13b89db4f2de001aaedb1c3f6.md)

## Where are the tests?

If you want to run or check them, they're in [penna-core/src/jmh](../penna-core/src/jmh/) and can be tested by
running `gradle :penna-core:jmh`.