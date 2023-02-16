package com.github.hkupty.maple.models.frames;

import com.github.hkupty.maple.sink.Sink;

/**
 * The DataFrame class is a base class for holding key-value pairs of data in place for logging.
 * It should be read-only and know, given a {@link com.github.hkupty.maple.sink.Sink.SinkWriter},
 * how to write themselves to the json object.
 * <br />
 * The idea behind holding data in a DataFrame is that we:
 *  a) can reuse commonly used information, reducing the memory footprint of the library:
 *    - examples of commonly used information are:
 *      - LoggerName -> We can hold a singleton {@link StringFrame} and reuse it for each logger;
 *      - Level -> We can hold an {@link java.util.EnumMap} with a {@link StringFrame} for each level;
 *      - ThreadName -> We can have a {@link ThreadLocal} provider returning a {@link StringFrame} for every thread;
 *    - This should account for a considerable amount of memory allocated repeatedly for each log message;
 * b) have control over which fields are populated:
 *   - in a normal scenario, we can have all providers enabled for maximum readability;
 *   - in an extreme scenario, we might opt out of logging MDC data, ThreadName, markers and timestamp;
 *   - in that case, we skip populating the information, avoiding unnecessary work
 */
public sealed abstract class DataFrame<T> permits ArrayDataFrame, EntryDataFrame, KeyValueArrayDataFrame, LongFrame, StringFrame, ThrowableDataFrame {
    protected String key;
    protected T value;

    public DataFrame(String key, T value) {
        this.key = key;
        this.value = value;
    }


    public abstract void write(Sink.SinkWriter writer);
}
