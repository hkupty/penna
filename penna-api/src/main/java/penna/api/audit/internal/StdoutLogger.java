package penna.api.audit.internal;

import penna.api.audit.PseudoLogger;

/**
 * Simple string-concat based logger just to print important messages to stdout.
 * See {@link PseudoLogger} for more information.
 */
public class StdoutLogger implements PseudoLogger {
    private final String version = StdoutLogger.class.getPackage().getImplementationVersion();

    /**
     * Builds an audit logger instance.
     * See {@link PseudoLogger} for more information.
     */
    public StdoutLogger() {}

    @Override
    public void report(String level, String event) {
        String sb = "{\"logger\":\"penna.api.audit.Logger\",\"level\":\"" +
                level +
                "\",\"message\":\"" +
                event +
                "\",\"pennaVersion\":\"" +
                version +
                "\"}";
        System.out.println(sb);
    }

    @Override
    public void reportError(String level, String event, Throwable throwable) {
        StringBuilder sb = new StringBuilder();
        sb.append("{\"logger\":\"penna.api.audit.Logger\",\"level\":\"");
        sb.append(level);
        sb.append("\",\"message\":\"");
        sb.append(event);
        sb.append("\",\"error\":\"");
        sb.append(throwable.getMessage());
        sb.append("\",\"stacktrace\":\"");
        for (StackTraceElement stackTraceElement : throwable.getStackTrace()) {
            sb.append(stackTraceElement.toString());
            sb.append("\\n");
        }
        sb.append("\",\"pennaVersion\":\"");
        sb.append(version);
        sb.append("\"}");
        System.out.println(sb);
    }
}
