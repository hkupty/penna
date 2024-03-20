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
            Thread.ofVirtual().name("Runner").start(() -> {
                logger.debug("Still running running");
                logger.atTrace()
                        .addArgument(counter::get)
                        .log("Counter is at {}");
                var next = counter.incrementAndGet();
                if (next >= 256) {
                    keepRunning.set(false);
                }
            });
            Thread.sleep(Duration.ofMillis(250));
        }
    }
}