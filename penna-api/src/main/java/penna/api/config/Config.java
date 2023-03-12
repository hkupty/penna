package penna.api.config;

import penna.api.models.LogField;
import org.slf4j.event.Level;

import java.util.Arrays;

/**
 * This is the Logger-level configuration object, that allows the user to set which log level that particular logger
 * will be able to log, as well as which fields it should log. By default, all loggers will inherit
 * the same {@link Config} from the root logger, but this can be changed through configuration and runtime.
 * @param level minimum {@link org.slf4j.event.Level} that the logger(s) to which this configuration will be applied will log.
 * @param fields (ordered) array of fields to be logged in the final JSON message.
 */
public record Config(
        Level level,
        LogField[] fields
) {

    private static final LogField[] defaultFields = new LogField[]{
            //LogField.Counter,
            LogField.Timestamp,
            LogField.Level,
            LogField.Message,
            LogField.LoggerName,
            LogField.ThreadName,
            LogField.MDC,
            LogField.Markers,
            LogField.KeyValuePairs,
            LogField.Throwable
    };

    /**
     * Returns a copy of this {@link Config}, but replacing the {@link Config#level} with the one supplied as a parameter.
     * @param level the new level which this configuration will consider to be minimum.
     * @return a new Config with this value applied and the same {@link Config#fields} as the original.
     */
    public Config replaceLevel(Level level) {
        return new Config(level, this.fields);
    }

    /**
     * Returns a copy of this {@link Config}, but replacing the {@link Config#fields} with the ones supplied as a parameter.
     * @param fields a new array of fields to replace the ones in the existing Config.
     * @return a new Config with the same {@link Config#level} as the original, replacing the fields.
     */
    public Config replaceFields(LogField... fields) {
        return new Config(this.level, fields);
    }

    /**
     * Returns a new copy of the default config.
     * <br />
     * For default values, consider:
     * {@code level} to be {@link org.slf4j.event.Level#INFO}
     * {@code fields} to be (in this order):
     *   - {@link LogField#Timestamp}
     *   - {@link LogField#Level}
     *   - {@link LogField#Message}
     *   - {@link LogField#LoggerName}
     *   - {@link LogField#ThreadName}
     *   - {@link LogField#MDC}
     *   - {@link LogField#Markers}
     *   - {@link LogField#KeyValuePairs}
     *   - {@link LogField#Throwable}
     * @return a new Config copy with the default values applied.
     */
    public static Config getDefault() {
        return new Config(Level.INFO, defaultFields);
    }

    /**
     * A convenience builder that returns a modified default Config with the supplied fields applied to it.
     * @param fields A vararg selection of fields to be logged
     * @return A new config copy, with the default level and the supplied fields applied
     * @see Config#getDefault()
     */
    public static Config withFields(LogField... fields) { return getDefault().replaceFields(fields); }

    /**
     * A convenience builder that returns a Config with the supplied values applied to it.
     * It exists for the sole purpose of creating a more user-friendly API, avoiding the creation of an array.
     * @param level {@link Config#level}
     * @param fields {@link Config#fields}
     * @return a new {@link Config} instance
     */
    public static Config withFields(Level level, LogField... fields) { return new Config(level, fields); }


    @Override
    public boolean equals(Object other) {
        if (other instanceof Config config) {
            return this == config || (
                    this.level == config.level
                    && Arrays.equals(this.fields, config.fields));
        }
        return false;
    }

    @Override
    public int hashCode() {
        int base = 31;
        base = base + 7 * this.level.hashCode();
        base = base + 7 * Arrays.hashCode(this.fields);

        return base;
    }

    @Override
    public String toString() {
        return "Config{level=" + this.level.toString() + ", fields=" + Arrays.toString(this.fields) + "}";
    }

}
