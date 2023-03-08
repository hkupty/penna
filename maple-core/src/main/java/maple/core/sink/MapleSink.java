package maple.core.sink;

import maple.core.minilog.MiniLogger;
import maple.core.sink.impl.GsonMapleSink;
import maple.core.sink.impl.JakartaMapleSink;
import maple.core.sink.impl.JacksonMapleSink;
import maple.core.sink.impl.NOPSink;

import java.io.*;
import java.util.function.Supplier;

public final class MapleSink {

    public static final class Factory {
        private static Supplier<SinkImpl> defaultImplementation;

        private Factory() {}

        // The methods below are correctly typed.
        @SuppressWarnings("unchecked")
        private static final Supplier<Supplier<SinkImpl>>[] candidates = new Supplier[]{
                Factory::tryJackson,
                Factory::tryGson,
                Factory::tryJakarta
        };

        private static void pickDefaultImplementation() {
            int counter = 0;
            do {
                defaultImplementation = candidates[counter].get();
                counter++;
            } while (counter < candidates.length && defaultImplementation == null);

            if (defaultImplementation == null) {
                MiniLogger.error("""
                           No implementation found.
                           Please add jackson, gson or a jakarta-compatible json library.
                           No log messages will be written because we can't format them.""");
                defaultImplementation = NOPSink::getInstance;
            }
        }

        // From the same ticket that PMD references, https://bugs.openjdk.org/browse/JDK-8080225, it is noted that
        // in JDK 10 the problem was solved. We are targeting JDK 17+, so the problem won't affect us.
        // Plus, any other alternative is significantly slower.
        @SuppressWarnings("PMD.AvoidFileStream")
        public static SinkImpl getSink() {
            if (defaultImplementation == null){
                pickDefaultImplementation();
            }

            try {
                SinkImpl sinkImpl = defaultImplementation.get();
                sinkImpl.init(new FileWriter(FileDescriptor.out));
                return sinkImpl;
            } catch (IOException e) {
                MiniLogger.error("Unable to create logger", e);
            }
            return null;
        }


        private static Supplier<SinkImpl> tryJackson() {
            try {
                Class.forName("com.fasterxml.jackson.core.JsonGenerator");
                return JacksonMapleSink::new;
            } catch (ClassNotFoundException e) {
                return null;
            }
        }

        private static Supplier<SinkImpl> tryGson() {
            try {
                Class.forName("com.google.gson.stream.JsonWriter");
                return GsonMapleSink::new;
            } catch (ClassNotFoundException e) {
                return null;
            }
        }

        private static Supplier<SinkImpl> tryJakarta() {
            try {
                Class.forName("jakarta.json.stream.JsonGenerator");
                return JakartaMapleSink::new;
            } catch (ClassNotFoundException e) {
                return null;
            }
        }
    }
}
