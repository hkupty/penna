package maple.throughput;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

public class Main {
    public static void main(String[] args) {

        Logger logger = LoggerFactory.getLogger(Main.class);
        long now = System.currentTimeMillis();

        for (long i = 0; i < 50_000_000_000L; i++){
            logger.atTrace().addKeyValue("counter", i).log("Not doing anything {}", i);
        }

    }
}