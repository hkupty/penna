package com.github.hkupty.maple.minilog;

public class MiniLogger {
    public static void error(String message, Throwable throwable) {
        System.out.print("[:Maple.MiniLogger/Error \"");
        System.out.print(message);
        System.out.println("\"]");
        System.out.println();
        System.out.print("[:Maple.MiniLogger/Exception ");
        System.out.println(throwable.getMessage());
        var stack = throwable.getStackTrace();
        for(int i = 0; i < stack.length; i++) {
            System.out.println(stack[i].toString());
        }
        System.out.print("]");
    }

    public static void debug(String message) {
        System.out.print("[:Maple/MiniLogger/Debug \"");
        System.out.print(message);
        System.out.println("\"]");
    }
}
