package penna.dev.sink;

import com.diogonunes.jcolor.Attribute;
import org.jetbrains.annotations.TestOnly;
import org.slf4j.MDC;
import penna.core.models.PennaLogEvent;
import penna.core.sink.NonStandardSink;
import penna.core.slf4j.PennaMDCAdapter;
import penna.dev.Formatter;
import penna.dev.models.EnhancedLogEvent;

import java.io.*;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

import static com.diogonunes.jcolor.Ansi.colorize;

public class DevRuntimeSink implements NonStandardSink, Closeable {
    private static final int LOGGER_NAME_FOLDING_THRESHOLD = 50;
    @SuppressWarnings("PMD.AvoidFileStream")
    FileOutputStream fos = new FileOutputStream(FileDescriptor.out);
    private static final DateTimeFormatter timestampFormatter = DateTimeFormatter.ofPattern("HH:mm:ss.SSS").withZone(ZoneId.systemDefault());
    PrintStream ps = new PrintStream(fos, false);
    PennaMDCAdapter mdcAdapter;

    private void init() {
        if (mdcAdapter != null) return;

        if (MDC.getMDCAdapter() instanceof PennaMDCAdapter adapter) {
            mdcAdapter = adapter;
        }
    }

    @TestOnly
    public void replaceOut(FileOutputStream fos) throws IOException {
        this.fos.close();
        this.fos = fos;
    }

    private static Attribute getColor(CharSequence key) {
        return Attribute.TEXT_COLOR((Math.abs(key.hashCode()) % 155) + 40);
    }

    @SuppressWarnings("PMD.DataflowAnomalyAnalysis")
    private static void emitHeader(StringBuilder log, PennaLogEvent logEvent) {
        var enhanced = EnhancedLogEvent.create(logEvent);
        log.append(colorize(String.format("%6s ", logEvent.level.toString()), Attribute.BOLD(), enhanced.accentColor()))
                .append(colorize(timestampFormatter.format(enhanced.timestamp()), Attribute.BOLD()))
                .append(' ')
                .append(colorize(new String(logEvent.threadName), Attribute.BOLD()));
        var logger = new StringBuilder(new String(logEvent.logger));
        if (logEvent.logger.length > LOGGER_NAME_FOLDING_THRESHOLD) {
            var cursor = 0;
            while (cursor != -1) {
                var off = logger.indexOf(".", cursor);
                if (off == -1) break;
                logger.delete(cursor + 1, off);
                cursor = logger.indexOf(".", cursor) + 1;
                if (logger.length() <= LOGGER_NAME_FOLDING_THRESHOLD) break;
            }
        }
        log.append(" ".repeat(Math.max(34 - logEvent.threadName.length - logger.length(), 1)))
                .append(colorize(logger.toString(), Attribute.BOLD(), getColor(new String(logEvent.logger))))
                .append(colorize(" | ", Attribute.BOLD()));

    }

    private static void emitThrowable(StringBuilder log, PennaLogEvent logEvent) {
        var thr = Optional.ofNullable(logEvent.throwable);

        if (thr.isEmpty()) {
            return;
        }

        log.append('\n')
                .append(colorize(" ❌ ", Attribute.BOLD(), Attribute.BRIGHT_RED_TEXT()))
                .append(colorize(logEvent.throwable.getClass().getName(), Attribute.YELLOW_TEXT()));

        thr.map(Throwable::getMessage).ifPresent(message -> {
            log.append(colorize(": ", Attribute.BOLD(), Attribute.YELLOW_TEXT()))
                    .append(colorize(message, Attribute.YELLOW_TEXT()));
        });

        thr.map(Throwable::getStackTrace).ifPresent(stacktrace -> {
            log.append('\n');
            for (StackTraceElement ste : stacktrace) {
                log.append('\t')
                        .append(ste.toString())
                        .append('\n');
            }
            log.deleteCharAt(log.length() - 1);
        });
    }

    @Override
    public void write(PennaLogEvent logEvent) throws IOException {
        init();
        StringBuilder log = new StringBuilder();
        // Headers
        emitHeader(log, logEvent);

        // Metadata
        if (mdcAdapter.isNotEmpty()) {
            log.append(colorize("mdc{", Attribute.ITALIC()));

            mdcAdapter.forEach((key, value) -> {
                log.append(colorize(key, Attribute.BOLD(), getColor(key)))
                        .append('=')
                        .append(colorize(value, Attribute.BOLD()))
                        .append(' ');
            });
            log.deleteCharAt(log.length() - 1)
                    .append(colorize("}", Attribute.ITALIC()));
        }

        if (!logEvent.markers.isEmpty()) {
            logEvent.markers.forEach(key -> {
                var kvColor = getColor(key.getName());
                log.append(colorize("#", Attribute.BOLD(), kvColor))
                        .append(colorize(key.getName(), Attribute.BOLD(), kvColor))
                        .append(' ');
            });
        }

        log.append(Formatter.format(logEvent.message, logEvent.arguments));

        if (!logEvent.keyValuePairs.isEmpty()) {
            log.append(colorize(" kvs{", Attribute.ITALIC()));

            logEvent.keyValuePairs.forEach((kvp) -> {
                log.append(colorize(kvp.key(), Attribute.BOLD(), getColor(kvp.key())))
                        .append('=')
                        .append(colorize(kvp.value().toString(), Attribute.BOLD()))
                        .append(' ');
            });
            log.deleteCharAt(log.length() - 1)
                    .append(colorize("}", Attribute.ITALIC()));
        }

        emitThrowable(log, logEvent);

        ps.println(log);
        ps.flush();
    }

    @Override
    public void close() throws IOException {
        fos.close();
        ps.close();
    }
}
