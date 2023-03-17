package penna.throughput;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.Executors;

public class Main {
    public static void main(String[] args) {

        class Inner {
            Exception data;
            Exception getSuppressed() {
                return new RuntimeException("WTH!!");
            }
            void setData() {
                data = new IllegalArgumentException("wrong '\"type!!!\nok???", new RuntimeException("inner", new ArrayIndexOutOfBoundsException(5)));
                data.addSuppressed(getSuppressed());
            }

            Exception getData() {
                setData();
                return data;
            }
        }
        int time = 10;
        int threads = 1;
        if (args.length > 0) {
            time = Integer.parseInt(args[0]);
        }
        if (args.length > 1) {
            threads = Integer.parseInt(args[1]);
        }
        Logger logger =  LoggerFactory.getLogger(Main.class);
        Instant target = Instant.now().plus(time, ChronoUnit.MILLIS);
        var ex = new Inner().getData();

        for (int i = 0; i < threads; i++) {
            int finalI = i;
            var thread = new Thread(() -> {
                var val = ("[" + finalI + "]").repeat(3000);
                while (Instant.now().isBefore(target)) {
                    logger.info("hello");
//                    logger.atInfo()
//                            .setMessage("hello")
////                            .setCause(ex)
////                            .addKeyValue("key", val)
//                            .log();
                }
            });
            thread.setName("penna-" + i);
            thread.start();
        }
    }
}
