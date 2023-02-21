package maple.core.minilog;

public class MiniLogger {
    public static void error(String message, Throwable throwable) {
        System.err.print("[:Maple.MiniLogger/Error \"");
        System.err.print(message);
        System.err.println("\"]");
        System.err.println();
        System.err.print("[:Maple.MiniLogger/Exception ");
        System.err.println(throwable.getMessage());
        var stack = throwable.getStackTrace();
        for(int i = 0; i < stack.length; i++) {
            System.err.println(stack[i].toString());
        }
        System.err.print("]");
    }

    public static void debug(String message) {
        System.err.print("[:Maple/MiniLogger/Debug \"");
        System.err.print(message);
        System.err.println("\"]");
    }
}
