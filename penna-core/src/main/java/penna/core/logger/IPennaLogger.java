package penna.core.logger;

import penna.api.config.Config;
import penna.core.models.PennaLogEvent;
import org.slf4j.Logger;
import org.slf4j.spi.LoggingEventAware;

public sealed interface IPennaLogger extends Logger, LoggingEventAware permits PennaLogger {

    Config getConfig();

    void log(PennaLogEvent log);
}
