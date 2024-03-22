package org.example;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;



public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) throws InterruptedException {
        var keepRunning = new AtomicBoolean(true);
        var counter = new AtomicInteger();

        while (keepRunning.get()) {
            Thread.ofVirtual().name("Trace logger").start(() -> logger.atTrace()
                    .addArgument(counter::get)
                    .log("Counter is at {}"));
            Thread.ofVirtual().name("Debug logger").start(() -> logger.debug("Still running running"));
            Thread.ofVirtual().name("Info logger").start(() -> logger.info("Still running running"));
            Thread.sleep(Duration.ofMillis(100));
            var next = counter.incrementAndGet();
            if (next >= 256) {
                keepRunning.set(false);

            }
        }
    }
}