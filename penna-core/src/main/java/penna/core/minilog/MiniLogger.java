package penna.core.minilog;

// Minilogger is our last resource. For now, we print to stderr normally.
@SuppressWarnings("PMD.SystemPrintln")
public final class MiniLogger {
    private MiniLogger() {}
    public static void error(String message) {
        System.err.print("[:Penna.MiniLogger/Error \"");
        System.err.print(message);
        System.err.println("\"]");
    }

    public static void error(String message, Throwable throwable) {
        error(message);
        System.err.print("[:Penna.MiniLogger/Exception ");
        System.err.println(throwable.getMessage());
        var stack = throwable.getStackTrace();
        for(int i = 0; i < stack.length; i++) {
            System.err.println(stack[i].toString());
        }
        System.err.print("]");
    }

    public static void debug(String message) {
        System.err.print("[:Penna/MiniLogger/Debug \"");
        System.err.print(message);
        System.err.println("\"]");
    }
}
