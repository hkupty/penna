package com.github.hkupty.maple.logger.provider;

import com.github.hkupty.maple.models.LogField;
import com.github.hkupty.maple.models.frames.ArrayDataFrame;
import org.slf4j.Marker;
import org.slf4j.event.LoggingEvent;

import java.util.List;

public class MarkerProvider implements DataFrameProvider<ArrayDataFrame<String>> {
    @Override
    public LogField field() {
        return LogField.Markers;
    }

    @Override
    public ArrayDataFrame<String> get(LoggingEvent event) {
        List<Marker> markers;
        if (!(markers = event.getMarkers()).isEmpty()) {
            return new ArrayDataFrame<String>(field().name(),
                    (String[]) markers
                            .stream()
                            .map(Marker::getName)
                            .toArray());
        }
        return null;
    }
}
