package com.github.hkupty.maple.sink.providers;

import com.github.hkupty.maple.models.JsonLog;
import com.github.hkupty.maple.sink.Sink;


public class MarkerProvider implements LogFieldProvider {
    private final String markerFieldName;

    public MarkerProvider(String markerFieldName) {
        this.markerFieldName = markerFieldName;
    }

    public MarkerProvider() {
        this("tags");
    }

    @Override
    public void logField(Sink.SinkWriter writer, JsonLog log) {
        if (log.markers().length > 0) {
            writer.startArray(markerFieldName);
            for(int i = 0; i < log.markers().length ; i++) {
                var marker = log.markers()[i];
                writer.writeString(marker.getName());
            }
            writer.endArray();
        }

    }
}
