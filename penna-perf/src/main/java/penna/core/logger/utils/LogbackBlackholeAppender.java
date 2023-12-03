package penna.core.logger.utils;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.OutputStreamAppender;
import org.openjdk.jmh.infra.Blackhole;

public class LogbackBlackholeAppender extends OutputStreamAppender<ILoggingEvent> {
    public static Blackhole bh;

    @Override
    public void start() {
        setOutputStream(new BlackholeOutputStream(bh));
        super.start();
    }

    @Override
    public void stop() {

    }
}
