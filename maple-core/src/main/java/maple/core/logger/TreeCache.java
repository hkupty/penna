package maple.core.logger;


import maple.api.config.Config;
import maple.api.config.ConfigManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * The tree cache is a data structure that allows us to reuse loggers based on their namespace as well as
 * update their configuration live, recursively.
 * <br />
 * By having this data structure here we can offload the responsibility of managing the loggers
 * from {@link maple.core.slf4j.MapleLoggerFactory}, at the cost of a small memory footprint, while we have an
 * easily traversable storage of the loggers.
 */
public class TreeCache {

    static class EntryData{
        MapleLogger logger;
        Config config;

        EntryData(MapleLogger logger, Config config) {
            this.logger = logger;
            this.config = config;
        }

        static EntryData empty(Config config) { return new EntryData(null, config);}

        private EntryData initialize(String[] identifier) {
            this.logger = new MapleLogger(String.join(".", identifier), this.config);
            return this;
        }

        void updateConfig(Config config) {
            this.config = config;
            if (this.logger != null) {
                logger.updateConfig(config);
            }
        }
    }

    private record Entry(String[] identifier, EntryData data, ArrayList<Entry> children){
        static Entry create(String[] identifier, Config config) {
            return new Entry(identifier, EntryData.empty(config), new ArrayList<>());
        }

        void initialize() {
            if (data.logger == null) {
                data.initialize(this.identifier);
            }
        }

        MapleLogger logger() {
            return data.logger;
        }
    }

    transient final Entry ROOT;

    public TreeCache(Config config) {
        ROOT = Entry.create(new String[]{}, config);
    }

    private Entry search(Entry cursor, String key, int index){
        for(var childEntry : cursor.children) {
            if (childEntry.identifier.length > index) {
                if (childEntry.identifier[index].equals(key)){
                    return childEntry;
                }
            }
        }
        return null;
    }

     Entry getOrCreate(String[] identifier){
        var cursor = ROOT;
        for(int index = 0; index < identifier.length; index++) {
            var child = search(cursor, identifier[index], index);
            if (Objects.isNull(child)) {
                var slicedIdentifier = Arrays.copyOfRange(identifier, 0, index + 1);
                child = Entry.create(slicedIdentifier, cursor.data.config);
                cursor.children.add(child);
            }
            cursor = child;
        }

        return cursor;
    }

    public MapleLogger getLoggerAt(String[] identifier) {
        Entry entry = getOrCreate(identifier);
        entry.initialize();
        return entry.logger();
    }

    // Default visibility, for testing
    void traverse(Entry base, Consumer<EntryData> update) {
        update.accept(base.data);
        for (int index = 0; index < base.children.size(); index++){
            traverse(base.children.get(index), update);
        }
    }

    public void updateConfig(String[] hierarchyIdentifier, Config config) {
        updateConfig(hierarchyIdentifier, old -> config);
    }

    public void updateConfig(String[] hierarchyIdentifier, ConfigManager.ConfigurationChange configUpdateFn) {
        Entry base;
        if (hierarchyIdentifier.length == 0) {
            base = ROOT;
        } else {
            base = getOrCreate(hierarchyIdentifier);
        }
        traverse(base, data -> data.updateConfig(configUpdateFn.applyUpdate(data.config)));
    }
}
