package maple.core.logger;

import maple.api.models.LogField;
import maple.core.models.MapleLogEvent;
import org.slf4j.Logger;
import org.slf4j.spi.LoggingEventAware;

public sealed interface IMapleLogger extends Logger, LoggingEventAware permits MapleLogger {

    LogField[] getFieldsToLog();

    void log(MapleLogEvent log);
}
