package maple.api.logger;

import maple.api.models.LogField;
import maple.api.models.MapleLogEvent;
import org.slf4j.Logger;
import org.slf4j.spi.LoggingEventAware;

public interface IMapleLogger extends Logger, LoggingEventAware {

    LogField[] getFieldsToLog();

    void log(MapleLogEvent log);
}
