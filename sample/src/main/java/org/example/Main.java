package org.example;

import jdk.jfr.Configuration;
import jdk.jfr.Recording;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MarkerFactory;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.LockSupport;


public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);
    private static final Long TARGET = 10_000_000L;

    public static void main(String[] args) throws InterruptedException, IOException, ParseException {
        var counter = new AtomicLong();
        var marker = MarkerFactory.getMarker("Important");

        Configuration conf = Configuration.getConfiguration("profile");
        try(Recording rec = new Recording(conf)) {
            rec.setToDisk(true);
            rec.setDumpOnExit(true);
            rec.setDestination(File.createTempFile("sample", ".jfr").toPath());
            rec.start();

            for (int i = 0; i <= 1_000; i++) {
                Thread.ofVirtual().name("Info logger").start(() -> {
                        while (counter.incrementAndGet() <= TARGET) {
                            logger.info(marker, "Still running running");
                        }
                        }
                );
            }

            while (counter.get() <= TARGET) {
                LockSupport.parkNanos(1_000_000);
            }

            rec.stop();
            System.out.println(rec.getDestination());
        }

    }
}
