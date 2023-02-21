package org.example;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);
    public static void main(String[] args) throws InterruptedException {
        Marker important = MarkerFactory.getMarker("Important");
        logger.atInfo().addMarker(important).log("Some stuff");
    }
}