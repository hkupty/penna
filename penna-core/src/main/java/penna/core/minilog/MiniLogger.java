package penna.core.minilog;

// Minilogger is our last resource. For now, we print to stderr normally.
@SuppressWarnings("PMD.SystemPrintln")
public final class MiniLogger {
    private MiniLogger() {}
    public static void error(String message) {
        System.err.print("{\"logger\":\"penna.core.MiniLogger\",\"level\":\"ERROR\",\"message\":\"");
        System.err.print(message);
        System.err.println("\"}");
    }

    public static void error(String message, Throwable throwable) {
        error(message);
        System.err.print("{\"logger\":\"penna.core.MiniLogger\",\"level\":\"ERROR\",\"message\":\"");
        System.err.println(throwable.getMessage());
        System.err.println("\", \"throwable\":\"");
        var stack = throwable.getStackTrace();
        for(int i = 0; i < stack.length; i++) {
            System.err.println(stack[i].toString());
            System.err.println("\n");
        }
        System.err.println("\"}");
    }

    public static void debug(String message) {
        System.err.print("{\"logger\":\"penna.core.MiniLogger\",\"level\":\"ERROR\",\"message\":\"");
        System.err.print(message);
        System.err.println("\"}");
    }
}
