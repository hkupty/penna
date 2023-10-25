package penna.dev.models;

import com.diogonunes.jcolor.Attribute;
import penna.core.models.PennaLogEvent;

import java.time.Instant;

public record EnhancedLogEvent(
        Attribute accentColor,
        Instant timestamp

) {
    public static EnhancedLogEvent create(PennaLogEvent originalEvent) {
        Attribute color = switch (originalEvent.level) {
            case TRACE -> Attribute.CYAN_TEXT();
            case DEBUG -> Attribute.GREEN_TEXT();
            case INFO -> Attribute.BRIGHT_WHITE_TEXT();
            case WARN -> Attribute.YELLOW_TEXT();
            case ERROR -> Attribute.RED_TEXT();
        };

        return new EnhancedLogEvent(color, Instant.ofEpochMilli(originalEvent.timestamp));
    }
}
