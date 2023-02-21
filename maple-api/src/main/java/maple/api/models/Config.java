package maple.api.models;

import org.slf4j.event.Level;

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
            //LogField.ThreadName,
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

}
