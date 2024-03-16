package org.example;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        MDC.put("Some", "value");
        logger.debug("Testing!");
        MDC.remove("Some");
    }
}