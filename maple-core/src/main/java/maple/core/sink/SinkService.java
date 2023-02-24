package maple.core.sink;

import maple.core.models.MapleLogEvent;

public class SinkService {
    private SinkService() {}

    record DirectSinkWrapper() implements SinkProxy {
        private static final ThreadLocal<MapleSink> sink = ThreadLocal.withInitial(MapleSink.Factory::getSink);
        @Override
        public void write(MapleLogEvent logData) {
            sink.get().write(logData);
        }
    }

    public static SinkProxy getProxy() { return new DirectSinkWrapper(); }
}
