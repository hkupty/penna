package penna.core.sink;

import java.io.*;
import java.nio.channels.FileChannel;
import java.util.function.Supplier;

@SuppressWarnings("PMD.AvoidFileStream")
public sealed interface OutputManager extends Closeable {

    class Impl {
        // TODO verify performance & output integrity on multiple threads
        static Supplier<OutputManager> defaultImpl = Stdout::new;

        public static void set(Supplier<OutputManager> impl) {
            defaultImpl = impl;
        }
        static OutputManager get() {
            return defaultImpl.get();
        }
    }

    FileChannel getChannel();

    final class Stdout implements OutputManager {
        private final FileOutputStream fos = new FileOutputStream(FileDescriptor.out);

        @Override
        public void close() throws IOException {
            fos.close();
        }

        @Override
        public FileChannel getChannel() {
            return fos.getChannel();
        }
    }

    final class Stderr implements OutputManager {
        private final FileOutputStream fos = new FileOutputStream(FileDescriptor.err);

        @Override
        public void close() throws IOException {
            fos.close();
        }

        @Override
        public FileChannel getChannel() {
            return fos.getChannel();
        }
    }

    final class ToFile implements OutputManager {

        private final FileOutputStream fos;

        public ToFile(File outFile) {
            FileOutputStream fos1;
            try {
                fos1 = new FileOutputStream(outFile);
            } catch (FileNotFoundException e) {
                fos1 = new FileOutputStream(FileDescriptor.err); // for now, stderr
            }

            fos = fos1;

        }


        @Override
        public FileChannel getChannel() {
            return fos.getChannel();
        }



        @Override
        public void close() throws IOException {
            fos.close();
        }
    }

}
