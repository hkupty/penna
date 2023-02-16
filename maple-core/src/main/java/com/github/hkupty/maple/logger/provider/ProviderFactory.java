package com.github.hkupty.maple.logger.provider;

import com.github.hkupty.maple.logger.MapleLogger;
import com.github.hkupty.maple.models.LogField;
import org.slf4j.Logger;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.function.Function;

public class ProviderFactory {

    private static final EnumMap<LogField, Function<MapleLogger, DataFrameProvider<?>>> providerMap;
    private static final DataFrameProvider<?>[] reference = new DataFrameProvider[]{};

    static {
        providerMap = new EnumMap<>(LogField.class);
        providerMap.put(LogField.LoggerName, LoggerNameProvider::new);
        providerMap.put(LogField.ThreadName, logger -> new ThreadNameProvider());
        providerMap.put(LogField.Timestamp, logger -> new TimestampProvider());
        providerMap.put(LogField.Level, logger -> new LevelProvider());
        providerMap.put(LogField.Message, logger -> new MessageProvider());
        providerMap.put(LogField.Markers, logger -> new MarkerProvider());
        providerMap.put(LogField.MDC, logger -> new MDCProvider());
        providerMap.put(LogField.KeyValuePairs, logger -> new KeyValueProvider());
        providerMap.put(LogField.Throwable, logger -> new ThrowableProvider());
    }

    public static DataFrameProvider<?>[] getProviders(MapleLogger logger, LogField[] fields) {
        var providers = Arrays.copyOf(reference, fields.length);
        for(int i = 0; i < fields.length; i++) {
            providers[i] = providerMap.get(fields[i]).apply(logger);
        }

        return providers;
    }

}
