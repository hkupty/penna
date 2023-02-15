package com.github.hkupty.maple.logger;

import com.github.hkupty.maple.slf4j.impl.Config;
import com.github.hkupty.maple.sink.Sink;
import com.github.hkupty.maple.logger.event.LoggingEventBuilderFactory;
import com.github.hkupty.maple.minilog.MiniLogger;
import com.github.hkupty.maple.models.JsonLog;
import com.github.hkupty.maple.sink.SinkFactory;
import com.github.hkupty.maple.sink.providers.LogFieldProvider;
import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.event.LoggingEvent;
import org.slf4j.spi.LoggingEventAware;
import org.slf4j.spi.LoggingEventBuilder;

import java.io.IOException;

import static com.github.hkupty.maple.logger.LogEventAdapter.transform;

public class ProxyLogger extends BaseLogger implements Logger, LoggingEventAware {

    private final Sink sink;

    public ProxyLogger(String name, LogFieldProvider[] providers, LoggingEventBuilderFactory eventBuilderFactory) {
        super(name, eventBuilderFactory);
        sink = SinkFactory.getSink(providers);
    }

    public ProxyLogger(String name, Config config) {
        this(name, config.providers(), config.factory());
    }


    @Override
    public String getName() {
        return name;
    }

    @Override
    public LoggingEventBuilder atTrace() { return eventBuilderFactory.trace(this); }

    @Override
    public boolean isTraceEnabled() {
        return eventBuilderFactory.isTraceEnabled();
    }

    @Override
    public void trace(String msg) {
        atTrace().log(msg);
    }

    @Override
    public void trace(String format, Object arg) {
        atTrace().log(format, arg);
    }

    @Override
    public void trace(String format, Object arg1, Object arg2) {
        atTrace().log(format, arg1, arg2);
    }

    @Override
    public void trace(String format, Object... arguments) {
        atTrace().log(format, arguments);
    }

    @Override
    public void trace(String msg, Throwable t) {
        atTrace().setCause(t).log(msg);
    }

    @Override
    public boolean isTraceEnabled(Marker marker) {
        return eventBuilderFactory.isTraceEnabled();
    }

    @Override
    public void trace(Marker marker, String msg) {
        atTrace().addMarker(marker).log(msg);
    }

    @Override
    public void trace(Marker marker, String format, Object arg) {
        atTrace().addMarker(marker).log(format, arg);
    }

    @Override
    public void trace(Marker marker, String format, Object arg1, Object arg2) {
        atTrace().addMarker(marker).log(format, arg1, arg2);
    }

    @Override
    public void trace(Marker marker, String format, Object... argArray) {
        atTrace().addMarker(marker).log(format, argArray);
    }

    @Override
    public void trace(Marker marker, String msg, Throwable t) {
        atTrace().addMarker(marker).setCause(t).log(msg);
    }

    @Override
    public LoggingEventBuilder atDebug() { return eventBuilderFactory.debug(this); }

    @Override
    public boolean isDebugEnabled() {
        return eventBuilderFactory.isDebugEnabled();
    }

    @Override
    public void debug(String msg) {
        atDebug().log(msg);
    }

    @Override
    public void debug(String format, Object arg) {
        atDebug().log(format, arg);
    }

    @Override
    public void debug(String format, Object arg1, Object arg2) {
        atDebug().log(format, arg1, arg2);
    }

    @Override
    public void debug(String format, Object... arguments) {
        atDebug().log(format, arguments);
    }

    @Override
    public void debug(String msg, Throwable t) {
        atDebug().setCause(t).log(msg);
    }

    @Override
    public boolean isDebugEnabled(Marker marker) {
        return eventBuilderFactory.isDebugEnabled();
    }

    @Override
    public void debug(Marker marker, String msg) {
        atDebug().addMarker(marker).log(msg);
    }

    @Override
    public void debug(Marker marker, String format, Object arg) {
        atDebug().addMarker(marker).log(format, arg);
    }

    @Override
    public void debug(Marker marker, String format, Object arg1, Object arg2) {
        atDebug().addMarker(marker).log(format, arg1, arg2);
    }

    @Override
    public void debug(Marker marker, String format, Object... arguments) {
        atDebug().addMarker(marker).log(format, arguments);
    }

    @Override
    public void debug(Marker marker, String msg, Throwable t) {
        atDebug().addMarker(marker).setCause(t).log(msg);
    }

    @Override
    public LoggingEventBuilder atInfo() { return eventBuilderFactory.info(this); }

    @Override
    public boolean isInfoEnabled() {
        return eventBuilderFactory.isInfoEnabled();
    }

    @Override
    public void info(String msg) {
        atInfo().log(msg);
    }

    @Override
    public void info(String format, Object arg) {
        atInfo().log(format, arg);
    }

    @Override
    public void info(String format, Object arg1, Object arg2) {
        atInfo().log(format, arg1, arg2);
    }

    @Override
    public void info(String format, Object... arguments) {
        atInfo().log(format, arguments);
    }

    @Override
    public void info(String msg, Throwable t) {
        atInfo().setCause(t).log(msg);
    }

    @Override
    public boolean isInfoEnabled(Marker marker) {
        return eventBuilderFactory.isInfoEnabled();
    }

    @Override
    public void info(Marker marker, String msg) {
        atInfo().addMarker(marker).log(msg);
    }

    @Override
    public void info(Marker marker, String format, Object arg) {
        atInfo().addMarker(marker).log(format, arg);
    }

    @Override
    public void info(Marker marker, String format, Object arg1, Object arg2) {
        atInfo().addMarker(marker).log(format, arg1, arg2);
    }

    @Override
    public void info(Marker marker, String format, Object... arguments) {
        atInfo().addMarker(marker).log(format, arguments);
    }

    @Override
    public void info(Marker marker, String msg, Throwable t) {
        atInfo().addMarker(marker).setCause(t).log(msg);
    }

    @Override
    public LoggingEventBuilder atWarn() { return eventBuilderFactory.warn(this); }

    @Override
    public boolean isWarnEnabled() {
        return eventBuilderFactory.isWarnEnabled();
    }

    @Override
    public void warn(String msg) {
        atWarn().log(msg);
    }

    @Override
    public void warn(String format, Object arg) {
        atWarn().log(format, arg);
    }

    @Override
    public void warn(String format, Object arg1, Object arg2) {
        atWarn().log(format, arg1, arg2);
    }

    @Override
    public void warn(String format, Object... arguments) {
        atWarn().log(format, arguments);
    }

    @Override
    public void warn(String msg, Throwable t) {
        atWarn().setCause(t).log(msg);
    }

    @Override
    public boolean isWarnEnabled(Marker marker) {
        return eventBuilderFactory.isWarnEnabled();
    }

    @Override
    public void warn(Marker marker, String msg) {
        atWarn().addMarker(marker).log(msg);
    }

    @Override
    public void warn(Marker marker, String format, Object arg) {
        atWarn().addMarker(marker).log(format, arg);
    }

    @Override
    public void warn(Marker marker, String format, Object arg1, Object arg2) {
        atWarn().addMarker(marker).log(format, arg1, arg2);

    }

    @Override
    public void warn(Marker marker, String format, Object... arguments) {
        atWarn().addMarker(marker).log(format, arguments);
    }

    @Override
    public void warn(Marker marker, String msg, Throwable t) {
        atWarn().addMarker(marker).setCause(t).log(msg);
    }

    @Override
    public LoggingEventBuilder atError() { return eventBuilderFactory.error(this); }

    @Override
    public boolean isErrorEnabled() {
        return eventBuilderFactory.isErrorEnabled();
    }

    @Override
    public void error(String msg) {
        atError().log(msg);
    }

    @Override
    public void error(String format, Object arg) {
        atError().log(format, arg);
    }

    @Override
    public void error(String format, Object arg1, Object arg2) {
        atError().log(format, arg1, arg2);
    }

    @Override
    public void error(String format, Object... arguments) {
        atError().log(format, arguments);
    }

    @Override
    public void error(String msg, Throwable t) {
        atError().setCause(t).log(msg);
    }

    @Override
    public boolean isErrorEnabled(Marker marker) {
        return eventBuilderFactory.isErrorEnabled();
    }

    @Override
    public void error(Marker marker, String msg) {
        atError().addMarker(marker).log(msg);
    }

    @Override
    public void error(Marker marker, String format, Object arg) {
        atError().addMarker(marker).log(format, arg);
    }

    @Override
    public void error(Marker marker, String format, Object arg1, Object arg2) {
        atError().addMarker(marker).log(format, arg1, arg2);
    }

    @Override
    public void error(Marker marker, String format, Object... arguments) {
        atError().addMarker(marker).log(format, arguments);

    }

    @Override
    public void error(Marker marker, String msg, Throwable t) {
        atError().addMarker(marker).setCause(t).log(msg);
    }

    @Override
    public void log(LoggingEvent event) {
        try {
            if (event instanceof JsonLog jsonLog) {
                sink.render(jsonLog);
            } else {
                sink.render(transform(event));
            }
        } catch (IOException ex)  {
            MiniLogger.error("Unable to log", ex);
        }
    }
}