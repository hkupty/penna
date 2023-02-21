package org.example;

import maple.core.slf4j.MapleLoggerFactory;
import org.slf4j.*;

public class Main {
    private static final ILoggerFactory loggerFactory = new MapleLoggerFactory();
    private static final Logger logger = loggerFactory.getLogger(Main.class.getName());
    public static void main(String[] args) {
        Runtime.getRuntime().addShutdownHook(new Thread(() ->{
            System.out.println("I'm dying");
        }));
        Marker important = MarkerFactory.getMarker("Important");
        logger.atInfo().addMarker(important).log("Some stuff");
    }
}