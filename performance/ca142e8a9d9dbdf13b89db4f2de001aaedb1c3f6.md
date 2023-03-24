# Logger performance

This is a comparison between penna and logback (both `logback-contrib` and `logstash-logback-encoder`) for json logs.

Below are 4 message types:

### `simple`

A very simple log message, no formatting, no extra data:

```java
    logger.atInfo().log("hello world");
```
### `modest`

A relatively simple message, just adding a marker:

```java
    logger.atInfo()
        .addMarker(MarkerFactory.getMarker("For the win!"))
        .log("Some event");
```

### `moderate`

A slightly more complex message, with MDC data and formatting a non-string value to the message.

```java
    MDC.put("SomeKey", "some value");
    logger
        .atInfo()
        .addMarker(MarkerFactory.getMarker("For the win!"))
        .addArgument(UUID::randomUUID)
        .log("Some event: {}");
    MDC.remove("SomeKey");
```

### `large`

A slightly more complex message, with MDC data and formatting a non-string value to the message.

```java
    // previously defined:
    Throwable exception = new RuntimeException("with an exception");

    // during JMH execution
    MDC.put("SomeKey", "some value");
    logger
        .atInfo()
        .addMarker(MarkerFactory.getMarker("For the win!"))
        .addArgument(UUID::randomUUID)
        .log("Some event: {}", exception);
    MDC.remove("SomeKey");
```

## Results

With that in mind, `penna` shows impressive results when compared to logback's json alternatives:

- For the `simple` and `modest` tests, memory consumption is constant and is 33%~60% faster than logback;
- for the `large` test, almost half the time/operation and one order of magnitude less memory consumption than
logstash-logback encoder, which itself is about 20% faster and consumes almost half the memory than `loback-contrib`.
- The `moderate` scenario still looks good, even though MDC usage hasn't been optimzied yet.

```
Benchmark                                          (behavior)  (encoder)  Mode  Cnt      Score       Error   Units
LoggerPerformanceTest.logback                          simple    contrib  avgt    3   1675.304 ±  2044.016   ns/op
LoggerPerformanceTest.logback:·gc.alloc.rate           simple    contrib  avgt    3   6197.701 ±  7381.942  MB/sec
LoggerPerformanceTest.logback:·gc.alloc.rate.norm      simple    contrib  avgt    3  10856.000 ±     0.001    B/op
LoggerPerformanceTest.logback:·gc.count                simple    contrib  avgt    3   1211.000              counts
LoggerPerformanceTest.logback:·gc.time                 simple    contrib  avgt    3    408.000                  ms
LoggerPerformanceTest.logback                          simple   logstash  avgt    3   1037.544 ±  1354.187   ns/op
LoggerPerformanceTest.logback:·gc.alloc.rate           simple   logstash  avgt    3    420.564 ±   551.172  MB/sec
LoggerPerformanceTest.logback:·gc.alloc.rate.norm      simple   logstash  avgt    3    455.992 ±     0.001    B/op
LoggerPerformanceTest.logback:·gc.count                simple   logstash  avgt    3    111.000              counts
LoggerPerformanceTest.logback:·gc.time                 simple   logstash  avgt    3     41.000                  ms
LoggerPerformanceTest.logback                          modest    contrib  avgt    3   1639.658 ±  1072.727   ns/op
LoggerPerformanceTest.logback:·gc.alloc.rate           modest    contrib  avgt    3   6375.324 ±  4199.776  MB/sec
LoggerPerformanceTest.logback:·gc.alloc.rate.norm      modest    contrib  avgt    3  10952.000 ±     0.001    B/op
LoggerPerformanceTest.logback:·gc.count                modest    contrib  avgt    3   1248.000              counts
LoggerPerformanceTest.logback:·gc.time                 modest    contrib  avgt    3    403.000                  ms
LoggerPerformanceTest.logback                          modest   logstash  avgt    3   1119.517 ±  1662.412   ns/op
LoggerPerformanceTest.logback:·gc.alloc.rate           modest   logstash  avgt    3    513.468 ±   798.287  MB/sec
LoggerPerformanceTest.logback:·gc.alloc.rate.norm      modest   logstash  avgt    3    599.991 ±     0.003    B/op
LoggerPerformanceTest.logback:·gc.count                modest   logstash  avgt    3    117.000              counts
LoggerPerformanceTest.logback:·gc.time                 modest   logstash  avgt    3     43.000                  ms
LoggerPerformanceTest.logback                        moderate    contrib  avgt    3   2680.712 ±  1471.402   ns/op
LoggerPerformanceTest.logback:·gc.alloc.rate         moderate    contrib  avgt    3   4348.430 ±  2412.520  MB/sec
LoggerPerformanceTest.logback:·gc.alloc.rate.norm    moderate    contrib  avgt    3  12216.000 ±     0.001    B/op
LoggerPerformanceTest.logback:·gc.count              moderate    contrib  avgt    3    856.000              counts
LoggerPerformanceTest.logback:·gc.time               moderate    contrib  avgt    3    293.000                  ms
LoggerPerformanceTest.logback                        moderate   logstash  avgt    3   1988.846 ±  1445.551   ns/op
LoggerPerformanceTest.logback:·gc.alloc.rate         moderate   logstash  avgt    3    736.895 ±   531.839  MB/sec
LoggerPerformanceTest.logback:·gc.alloc.rate.norm    moderate   logstash  avgt    3   1535.192 ±     0.012    B/op
LoggerPerformanceTest.logback:·gc.count              moderate   logstash  avgt    3    154.000              counts
LoggerPerformanceTest.logback:·gc.time               moderate   logstash  avgt    3     59.000                  ms
LoggerPerformanceTest.logback                           large    contrib  avgt    3  12165.917 ± 13397.489   ns/op
LoggerPerformanceTest.logback:·gc.alloc.rate            large    contrib  avgt    3   4271.501 ±  4727.764  MB/sec
LoggerPerformanceTest.logback:·gc.alloc.rate.norm       large    contrib  avgt    3  54360.010 ±     0.309    B/op
LoggerPerformanceTest.logback:·gc.count                 large    contrib  avgt    3    837.000              counts
LoggerPerformanceTest.logback:·gc.time                  large    contrib  avgt    3    279.000                  ms
LoggerPerformanceTest.logback                           large   logstash  avgt    3   9974.738 ±  1695.158   ns/op
LoggerPerformanceTest.logback:·gc.alloc.rate            large   logstash  avgt    3   2690.112 ±   457.254  MB/sec
LoggerPerformanceTest.logback:·gc.alloc.rate.norm       large   logstash  avgt    3  28136.001 ±     0.231    B/op
LoggerPerformanceTest.logback:·gc.count                 large   logstash  avgt    3    531.000              counts
LoggerPerformanceTest.logback:·gc.time                  large   logstash  avgt    3    185.000                  ms
LoggerPerformanceTest.penna                            simple        N/A  avgt    3    676.393 ±  1520.672   ns/op
LoggerPerformanceTest.penna:·gc.alloc.rate             simple        N/A  avgt    3     ≈ 10⁻⁴              MB/sec
LoggerPerformanceTest.penna:·gc.alloc.rate.norm        simple        N/A  avgt    3     ≈ 10⁻⁴                B/op
LoggerPerformanceTest.penna:·gc.count                  simple        N/A  avgt    3        ≈ 0              counts
LoggerPerformanceTest.penna                            modest        N/A  avgt    3    750.136 ±  1470.167   ns/op
LoggerPerformanceTest.penna:·gc.alloc.rate             modest        N/A  avgt    3     ≈ 10⁻⁴              MB/sec
LoggerPerformanceTest.penna:·gc.alloc.rate.norm        modest        N/A  avgt    3     ≈ 10⁻⁴                B/op
LoggerPerformanceTest.penna:·gc.count                  modest        N/A  avgt    3        ≈ 0              counts
LoggerPerformanceTest.penna                          moderate        N/A  avgt    3   1757.362 ±  3427.750   ns/op
LoggerPerformanceTest.penna:·gc.alloc.rate           moderate        N/A  avgt    3    345.723 ±   707.845  MB/sec
LoggerPerformanceTest.penna:·gc.alloc.rate.norm      moderate        N/A  avgt    3    632.000 ±     0.001    B/op
LoggerPerformanceTest.penna:·gc.count                moderate        N/A  avgt    3     90.000              counts
LoggerPerformanceTest.penna:·gc.time                 moderate        N/A  avgt    3     38.000                  ms
LoggerPerformanceTest.penna                             large        N/A  avgt    3   4957.492 ±  2848.095   ns/op
LoggerPerformanceTest.penna:·gc.alloc.rate              large        N/A  avgt    3    140.134 ±    80.086  MB/sec
LoggerPerformanceTest.penna:·gc.alloc.rate.norm         large        N/A  avgt    3    728.000 ±     0.001    B/op
LoggerPerformanceTest.penna:·gc.count                   large        N/A  avgt    3    462.000              counts
LoggerPerformanceTest.penna:·gc.time                    large        N/A  avgt    3    131.000                  ms
```