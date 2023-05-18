package penna.core.logger;

import org.slf4j.Logger;
import org.slf4j.spi.LoggingEventAware;

public sealed interface IPennaLogger extends Logger, LoggingEventAware permits PennaLogger {}
