package penna.api.config;

import org.jetbrains.annotations.NotNull;
import org.slf4j.event.Level;
import penna.api.models.LogField;

import java.util.Arrays;

/**
 * This is the Logger-level configuration object, that allows the user to set which log level that particular logger
 * will be able to log, as well as which fields it should log. By default, all loggers will inherit
 * the same {@link Config} from the root logger, but this can be changed through configuration and runtime.
 *
 * @param level             minimum {@link org.slf4j.event.Level} that the logger(s) to which this configuration will be applied will log.
 * @param fields            (ordered) array of fields to be logged in the final JSON message.
 * @param exceptionHandling Allows for configuring exception logging. See {@link ExceptionHandling}
 */
public record Config(
        Level level,
        LogField[] fields,
        ExceptionHandling exceptionHandling
) {

    private static final LogField[] defaultFields = new LogField[]{
            //LogField.Counter,
            LogField.TIMESTAMP,
            LogField.LEVEL,
            LogField.MESSAGE,
            LogField.LOGGER_NAME,
            LogField.THREAD_NAME,
            LogField.MDC,
            LogField.MARKERS,
            LogField.KEY_VALUE_PAIRS,
            LogField.THROWABLE
    };

    /**
     * Returns a copy of this {@link Config}, but replacing the {@link Config#level} with the one supplied as a parameter.
     *
     * @param level the new level which this configuration will consider to be minimum.
     * @return a new Config with this value applied and the same {@link Config#fields} and {@link Config#exceptionHandling} as the original.
     */
    public Config replaceLevel(@NotNull Level level) {
        return new Config(level, this.fields, this.exceptionHandling);
    }

    /**
     * Returns a copy of this {@link Config}, but replacing the {@link Config#fields} with the ones supplied as parameters.
     *
     * @param fields a new array of fields to replace the ones in the existing Config.
     * @return a new Config with the same {@link Config#level} and {@link Config#exceptionHandling} as the original, replacing the fields.
     */
    public Config replaceFields(LogField... fields) {
        return new Config(this.level, fields, this.exceptionHandling);
    }

    /**
     * Returns a copy of this {@link Config}, but replacing the {@link Config#exceptionHandling} with the one supplied as a parameter.
     *
     * @param exceptionHandling an {@link ExceptionHandling} object
     * @return a new Config with the same {@link Config#level} and {@link Config#fields} as the original and the {@link Config#exceptionHandling} replaced
     */
    public Config replaceExceptionHandling(ExceptionHandling exceptionHandling) {
        return new Config(this.level, this.fields, exceptionHandling);
    }

    /**
     * Returns a new copy of the default config.
     * <br />
     * For default values, consider:
     * {@code level} to be {@link org.slf4j.event.Level#INFO}
     * {@code fields} to be (in this order):
     * - {@link LogField#TIMESTAMP}
     * - {@link LogField#LEVEL}
     * - {@link LogField#MESSAGE}
     * - {@link LogField#LOGGER_NAME}
     * - {@link LogField#THREAD_NAME}
     * - {@link LogField#MDC}
     * - {@link LogField#MARKERS}
     * - {@link LogField#KEY_VALUE_PAIRS}
     * - {@link LogField#THROWABLE}
     *
     * @return a new Config copy with the default values applied.
     */
    public static Config getDefault() {
        return new Config(Level.INFO, defaultFields, ExceptionHandling.getDefault());
    }

    /**
     * A convenience builder that returns a modified default Config with the supplied fields applied to it.
     *
     * @param fields A vararg selection of fields to be logged
     * @return A new config copy, with the default level and the supplied fields applied
     * @see Config#getDefault()
     */
    public static Config withFields(LogField... fields) {
        return getDefault().replaceFields(fields);
    }

    /**
     * A convenience builder that returns a Config with the supplied values applied to it.
     * It exists for the sole purpose of creating a more user-friendly API, avoiding the creation of an array.
     *
     * @param level  {@link Config#level}
     * @param fields {@link Config#fields}
     * @return a new {@link Config} instance
     */
    public static Config withFields(Level level, LogField... fields) {
        return new Config(level, fields, ExceptionHandling.getDefault());
    }

    /**
     * A convenience builder that returns a Config with the supplied values applied to it.
     * It exists for the sole purpose of creating a more user-friendly API, avoiding the creation of an array.
     *
     * @param level             {@link Config#level}
     * @param exceptionHandling {@link Config#exceptionHandling}
     * @param fields            {@link Config#fields}
     * @return a new {@link Config} instance
     */
    public static Config withFields(Level level, ExceptionHandling exceptionHandling, LogField... fields) {
        return new Config(level, fields, exceptionHandling);
    }

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
