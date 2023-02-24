package maple.core.models;

import maple.core.logger.IMapleLogger;
import maple.api.models.LogField;
import org.slf4j.Marker;
import org.slf4j.event.KeyValuePair;
import org.slf4j.event.Level;
import org.slf4j.event.LoggingEvent;

import java.util.ArrayList;
import java.util.List;

public class MapleLogEvent implements LoggingEvent {

    public ArrayList<Object> arguments = new ArrayList<>();
    public ArrayList<Marker> markers = new ArrayList<>();
    public ArrayList<KeyValuePair> keyValuePairs = new ArrayList<>();
    public LogField[] fieldsToLog = null;
    public Object extra = null;
    public Level level;
    public String message;
    public String threadName;
    public Throwable throwable;
    public IMapleLogger logger;

    /**
     * Resets all the fields that will change during log creation.
     * <br />
     * Note! ThreadName is never changing for the lifetime of the log object.
     * This is because it is bound to the object pool in the LoggingEventBuilder and,
     * therefore, will never change.
     */
    public void reset() {
        markers.clear();
        arguments.clear();
        keyValuePairs.clear();

        fieldsToLog = null;
        extra = null;
        message = null;
        throwable = null;
        logger = null;
    }


    @Override
    public Level getLevel() {
        return level;
    }

    @Override
    public String getLoggerName() {
        return logger.getName();
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public List<Object> getArguments() {
        return arguments;
    }

    @Override
    public Object[] getArgumentArray() {
        return arguments.toArray();
    }

    @Override
    public List<Marker> getMarkers() {
        return markers;
    }

    @Override
    public List<KeyValuePair> getKeyValuePairs() {
        return keyValuePairs;
    }

    @Override
    public Throwable getThrowable() {
        return throwable;
    }

    @Override
    public long getTimeStamp() {
        return 0;
    }

    @Override
    public String getThreadName() {
        return threadName;
    }
}