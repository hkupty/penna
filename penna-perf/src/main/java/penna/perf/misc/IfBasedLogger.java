package penna.perf.misc;

import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.event.Level;
import org.slf4j.event.LoggingEvent;
import org.slf4j.spi.LoggingEventAware;
import org.slf4j.spi.LoggingEventBuilder;
import penna.api.models.Config;
import penna.core.internals.LogUnitContextPool;
import penna.core.logger.LogUnitContext;
import penna.core.models.LogConfig;

public class IfBasedLogger implements Logger, LoggingEventAware {

    private static final LogUnitContextPool logUnits = new LogUnitContextPool();
    transient final byte[] nameAsChars;
    transient final String name;
    transient Config config;
    transient LogConfig logConfig;

    public IfBasedLogger(String name, Config config) {
        this.name = name;
        this.nameAsChars = name.getBytes();
        this.config = config;
        this.logConfig = LogConfig.fromConfig(config);
    }


    @Override
    public String getName() {
        return "";
    }

    private LogUnitContext get(Level level) {
        var eventBuilder = logUnits.get();
        eventBuilder.logEvent().reset(this.nameAsChars, this.logConfig, level, Thread.currentThread());
        return eventBuilder;
    }

    @Override
    public LoggingEventBuilder atTrace() {
        return get(Level.TRACE);
    }

    @Override
    public boolean isTraceEnabled() {
        return config.level().toInt() == Level.TRACE.toInt();
    }


    @Override
    public void trace(String msg) {
        if (isTraceEnabled()) {
            atTrace().log(msg);
        }
    }

    @Override
    public void trace(String format, Object arg) {
        if (isTraceEnabled()) {
            atTrace()
                    .addArgument(arg)
                    .log(format);
        }
    }

    @Override
    public void trace(String format, Object arg1, Object arg2) {
        if (isTraceEnabled()) {
            atTrace()
                    .addArgument(arg1)
                    .addArgument(arg2)
                    .log(format);
        }
    }

    @Override
    public void trace(String format, Object... arguments) {
        if (isTraceEnabled()) {
            atTrace().log(format, arguments);
        }
    }

    @Override
    public void trace(String msg, Throwable t) {
        if (isTraceEnabled()) {
            atTrace()
                    .setCause(t)
                    .log(msg);
        }
    }

    @Override
    public boolean isTraceEnabled(Marker marker) {
        return isTraceEnabled();
    }

    @Override
    public void trace(Marker marker, String msg) {
        if (isTraceEnabled()) {
            atTrace()
                    .addMarker(marker)
                    .log(msg);
        }
    }

    @Override
    public void trace(Marker marker, String format, Object arg) {
        if (isTraceEnabled()) {
            atTrace()
                    .addArgument(arg)
                    .addMarker(marker)
                    .log(format);
        }
    }

    @Override
    public void trace(Marker marker, String format, Object arg1, Object arg2) {
        if (isTraceEnabled()) {
            atTrace()
                    .addArgument(arg1)
                    .addArgument(arg2)
                    .addMarker(marker)
                    .log(format);
        }
    }

    @Override
    public void trace(Marker marker, String format, Object... argArray) {
        if (isTraceEnabled()) {
            atTrace()
                    .addMarker(marker)
                    .log(format, argArray);
        }

    }

    @Override
    public void trace(Marker marker, String msg, Throwable t) {
        if (isTraceEnabled()) {
            atTrace()
                    .setCause(t)
                    .addMarker(marker)
                    .log(msg);
        }
    }

    @Override
    public LoggingEventBuilder atDebug() {
        return get(Level.DEBUG);
    }

    @Override
    public boolean isDebugEnabled() {
        return config.level().toInt() >= Level.DEBUG.toInt();
    }

    @Override
    public void debug(String msg) {
        if (isDebugEnabled()) {
            atDebug().log(msg);
        }
    }

    @Override
    public void debug(String format, Object arg) {
        if (isDebugEnabled()) {
            atDebug()
                    .addArgument(arg)
                    .log(format);
        }
    }

    @Override
    public void debug(String format, Object arg1, Object arg2) {
        if (isDebugEnabled()) {
            atDebug()
                    .addArgument(arg1)
                    .addArgument(arg2)
                    .log(format);
        }
    }

    @Override
    public void debug(String format, Object... arguments) {
        if (isDebugEnabled()) {
            atDebug().log(format, arguments);
        }
    }

    @Override
    public void debug(String msg, Throwable t) {
        if (isDebugEnabled()) {
            atDebug()
                    .setCause(t)
                    .log(msg);
        }
    }

    @Override
    public boolean isDebugEnabled(Marker marker) {
        return isDebugEnabled();
    }

    @Override
    public void debug(Marker marker, String msg) {
        if (isDebugEnabled()) {
            atDebug()
                    .addMarker(marker)
                    .log(msg);
        }
    }

    @Override
    public void debug(Marker marker, String format, Object arg) {
        if (isDebugEnabled()) {
            atDebug()
                    .addArgument(arg)
                    .addMarker(marker)
                    .log(format);
        }
    }

    @Override
    public void debug(Marker marker, String format, Object arg1, Object arg2) {
        if (isDebugEnabled()) {
            atDebug()
                    .addArgument(arg1)
                    .addArgument(arg2)
                    .addMarker(marker)
                    .log(format);
        }
    }

    @Override
    public void debug(Marker marker, String format, Object... argArray) {
        if (isDebugEnabled()) {
            atDebug()
                    .addMarker(marker)
                    .log(format, argArray);
        }

    }

    @Override
    public void debug(Marker marker, String msg, Throwable t) {
        if (isDebugEnabled()) {
            atDebug()
                    .setCause(t)
                    .addMarker(marker)
                    .log(msg);
        }
    }

    @Override
    public LoggingEventBuilder atInfo() {
        return get(Level.INFO);
    }

    @Override
    public boolean isInfoEnabled() {
        return config.level().toInt() >= Level.INFO.toInt();
    }


    @Override
    public void info(String msg) {
        if (isInfoEnabled()) {
            atInfo().log(msg);
        }
    }

    @Override
    public void info(String format, Object arg) {
        if (isInfoEnabled()) {
            atInfo()
                    .addArgument(arg)
                    .log(format);
        }
    }

    @Override
    public void info(String format, Object arg1, Object arg2) {
        if (isInfoEnabled()) {
            atInfo()
                    .addArgument(arg1)
                    .addArgument(arg2)
                    .log(format);
        }
    }

    @Override
    public void info(String format, Object... arguments) {
        if (isInfoEnabled()) {
            atInfo().log(format, arguments);
        }
    }

    @Override
    public void info(String msg, Throwable t) {
        if (isInfoEnabled()) {
            atInfo()
                    .setCause(t)
                    .log(msg);
        }
    }

    @Override
    public boolean isInfoEnabled(Marker marker) {
        return isInfoEnabled();
    }

    @Override
    public void info(Marker marker, String msg) {
        if (isInfoEnabled()) {
            atInfo()
                    .addMarker(marker)
                    .log(msg);
        }
    }

    @Override
    public void info(Marker marker, String format, Object arg) {
        if (isInfoEnabled()) {
            atInfo()
                    .addArgument(arg)
                    .addMarker(marker)
                    .log(format);
        }
    }

    @Override
    public void info(Marker marker, String format, Object arg1, Object arg2) {
        if (isInfoEnabled()) {
            atInfo()
                    .addArgument(arg1)
                    .addArgument(arg2)
                    .addMarker(marker)
                    .log(format);
        }
    }

    @Override
    public void info(Marker marker, String format, Object... argArray) {
        if (isInfoEnabled()) {
            atInfo()
                    .addMarker(marker)
                    .log(format, argArray);
        }

    }

    @Override
    public void info(Marker marker, String msg, Throwable t) {
        if (isInfoEnabled()) {
            atInfo()
                    .addMarker(marker)
                    .setCause(t)
                    .log(msg);
        }
    }

    @Override
    public LoggingEventBuilder atWarn() {
        return get(Level.WARN);
    }

    @Override
    public boolean isWarnEnabled() {
        return config.level().toInt() >= Level.WARN.toInt();
    }


    @Override
    public void warn(String msg) {
        if (isWarnEnabled()) {
            atWarn().log(msg);
        }
    }

    @Override
    public void warn(String format, Object arg) {
        if (isWarnEnabled()) {
            atWarn()
                    .addArgument(arg)
                    .log(format);
        }
    }

    @Override
    public void warn(String format, Object arg1, Object arg2) {
        if (isWarnEnabled()) {
            atWarn()
                    .addArgument(arg1)
                    .addArgument(arg2)
                    .log(format);
        }
    }

    @Override
    public void warn(String format, Object... arguments) {
        if (isWarnEnabled()) {
            atWarn().log(format, arguments);
        }
    }

    @Override
    public void warn(String msg, Throwable t) {
        if (isWarnEnabled()) {
            atWarn()
                    .setCause(t)
                    .log(msg);
        }
    }

    @Override
    public boolean isWarnEnabled(Marker marker) {
        return isWarnEnabled();
    }

    @Override
    public void warn(Marker marker, String msg) {
        if (isWarnEnabled()) {
            atWarn()
                    .addMarker(marker)
                    .log(msg);
        }
    }

    @Override
    public void warn(Marker marker, String format, Object arg) {
        if (isWarnEnabled()) {
            atWarn()
                    .addArgument(arg)
                    .addMarker(marker)
                    .log(format);
        }
    }

    @Override
    public void warn(Marker marker, String format, Object arg1, Object arg2) {
        if (isWarnEnabled()) {
            atWarn()
                    .addArgument(arg1)
                    .addArgument(arg2)
                    .addMarker(marker)
                    .log(format);
        }
    }

    @Override
    public void warn(Marker marker, String format, Object... argArray) {
        if (isWarnEnabled()) {
            atWarn()
                    .addMarker(marker)
                    .log(format, argArray);
        }

    }

    @Override
    public void warn(Marker marker, String msg, Throwable t) {
        if (isWarnEnabled()) {
            atWarn()
                    .addMarker(marker)
                    .setCause(t)
                    .log(msg);
        }
    }

    @Override
    public LoggingEventBuilder atError() {
        return get(Level.ERROR);
    }

    @Override
    public boolean isErrorEnabled() {
        return config.level().toInt() >= Level.ERROR.toInt();
    }


    @Override
    public void error(String msg) {
        if (isErrorEnabled()) {
            atError().log(msg);
        }
    }

    @Override
    public void error(String format, Object arg) {
        if (isErrorEnabled()) {
            atError()
                    .addArgument(arg)
                    .log(format);
        }
    }

    @Override
    public void error(String format, Object arg1, Object arg2) {
        if (isErrorEnabled()) {
            atError()
                    .addArgument(arg1)
                    .addArgument(arg2)
                    .log(format);
        }
    }

    @Override
    public void error(String format, Object... arguments) {
        if (isErrorEnabled()) {
            atError().log(format, arguments);
        }
    }

    @Override
    public void error(String msg, Throwable t) {
        if (isErrorEnabled()) {
            atError()
                    .setCause(t)
                    .log(msg);
        }
    }

    @Override
    public boolean isErrorEnabled(Marker marker) {
        return isErrorEnabled();
    }

    @Override
    public void error(Marker marker, String msg) {
        if (isErrorEnabled()) {
            atError()
                    .addMarker(marker)
                    .log(msg);
        }
    }

    @Override
    public void error(Marker marker, String format, Object arg) {
        if (isErrorEnabled()) {
            atError()
                    .addArgument(arg)
                    .addMarker(marker)
                    .log(format);
        }
    }

    @Override
    public void error(Marker marker, String format, Object arg1, Object arg2) {
        if (isErrorEnabled()) {
            atError()
                    .addArgument(arg1)
                    .addArgument(arg2)
                    .addMarker(marker)
                    .log(format);
        }
    }

    @Override
    public void error(Marker marker, String format, Object... argArray) {
        if (isErrorEnabled()) {
            atError()
                    .addMarker(marker)
                    .log(format, argArray);
        }

    }

    @Override
    public void error(Marker marker, String msg, Throwable t) {
        if (isErrorEnabled()) {
            atError()
                    .addMarker(marker)
                    .setCause(t)
                    .log(msg);
        }
    }

    @Override
    public void log(LoggingEvent event) {
        get(event.getLevel()).fromLoggingEvent(event);
    }
}
