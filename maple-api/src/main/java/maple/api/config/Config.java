package maple.api.config;

import maple.api.models.LogField;
import org.slf4j.event.Level;

/**
 * This is the Logger-level configuration object, that allows the user to set which log level that particular logger
 * will be able to log, as well as which fields it should log. By default, all loggers will inherit
 * the same {@link Config} from the root logger, but this can be changed through configuration and runtime.
 * @param level
 * @param fields
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
            //LogField.MDC,
            //LogField.Markers,
            LogField.Throwable,
            LogField.KeyValuePairs
    };

    public Config copy(Level level) {
        return new Config(level, this.fields);
    }

    public Config copy(LogField[] fields) {
        return new Config(this.level, fields);
    }

    public static Config getDefault() {
        return new Config(Level.INFO, defaultFields);
    }

    public static Config withFields(LogField... fields) { return getDefault().copy(fields); }

    public static Config withFields(Level level, LogField... fields) { return new Config(level, fields); }


}
