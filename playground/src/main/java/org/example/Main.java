package org.example;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

public class Main {
    public static void main(String[] args) {
        Logger logger = LoggerFactory.getLogger(Main.class);
        Logger otherLogger = LoggerFactory.getLogger("simple.logger");
        MDC.put("some", "data");
        MDC.put("other", "data");

        logger.atInfo()
                .addKeyValue("something", -43.2)
                .log("Hello world!", new RuntimeException("Something awful!"));
        logger.debug("This should be shown");
        otherLogger.info("Hello world!");
        otherLogger.debug("This should be not shown");

        MDC.remove("some");
        MDC.remove("other");

    }
}