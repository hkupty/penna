package com.github.hkupty.maple.logger.provider;

import com.github.hkupty.maple.models.LogField;
import com.github.hkupty.maple.models.frames.KeyValueArrayDataFrame;
import org.slf4j.event.KeyValuePair;
import org.slf4j.event.LoggingEvent;

import java.util.List;

public class KeyValueProvider implements DataFrameProvider<KeyValueArrayDataFrame> {

    private static final KeyValuePair[] reference = new KeyValuePair[0];
    @Override
    public LogField field() {
        return LogField.KeyValuePairs;
    }

    @Override
    public KeyValueArrayDataFrame get(LoggingEvent event) {
        List<KeyValuePair> kvp;
        if (!(kvp = event.getKeyValuePairs()).isEmpty()) {
            return new KeyValueArrayDataFrame(
                    field().name(),
                    event.getKeyValuePairs().toArray(reference)
            );
        }
        return null;
    }
}
