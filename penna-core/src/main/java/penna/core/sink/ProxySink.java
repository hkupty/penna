package penna.core.sink;

import penna.core.models.PennaLogEvent;

import java.io.IOException;

public final class ProxySink implements Sink {

    private Sink realImpl;

    public ProxySink(Sink realImpl) {
        this.realImpl = realImpl;
    }

    void replaceImpl(Sink newImpl) {
        this.realImpl = newImpl;
    }

    @Override
    public void write(PennaLogEvent logEvent) throws IOException {
        this.realImpl.write(logEvent);
    }
}
