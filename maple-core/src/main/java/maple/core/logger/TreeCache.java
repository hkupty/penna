package maple.core.logger;


import maple.api.config.Config;
import maple.api.config.ConfigManager;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * The tree cache is a data structure that allows us to reuse loggers based on their namespace as well as
 * update their configuration live, recursively.
 * <br />
 * By having this data structure here we can offload the responsibility of managing the loggers
 * from {@link maple.core.slf4j.MapleLoggerFactory}, at the cost of a small memory footprint, while we have an
 * easily traversable storage of the loggers.
 */
public class TreeCache {
    private record Entry(String[] identifier, MapleLogger logger, ArrayList<Entry> children){
        static Entry create(String[] identifier, MapleLogger logger) {
            return new Entry(identifier, logger, new ArrayList<>());
        }
    }

    private transient final Entry ROOT;

    public TreeCache(MapleLogger root) {
        ROOT = Entry.create(new String[]{}, root);
    }

    private Entry search(Entry cursor, String[] identifier, int index){
       for(var childEntry : cursor.children) {
           if (childEntry.identifier.length > index) {
               if (childEntry.identifier[index].equals(identifier[index])){
                   if (index == identifier.length - 1) {
                       return childEntry;
                   } else {
                       return search(childEntry, identifier, index + 1);
                   }
               }
           }
       }
       return null;
    }

    public Logger find(String[] identifier) {
        if(identifier.length == 0) {
            return ROOT.logger;
        }
        var entry = search(ROOT, identifier, 0);
        if (!Objects.isNull(entry)) {
            return entry.logger;
        }
        return null;
    }

     public Logger createRecursively(String[] identifier, BiFunction<MapleLogger, String[], MapleLogger> filler){
        var cursor = ROOT;
        for(int index = 1; index <= identifier.length; index++) {
            var slicedIdentifier = Arrays.copyOfRange(identifier, 0, index);
            var child = search(cursor, slicedIdentifier, index);
            if (Objects.isNull(child)) {
                child = Entry.create(slicedIdentifier, filler.apply(cursor.logger, slicedIdentifier));
                cursor.children.add(child);
            }
            cursor = child;

        }

        return cursor.logger;
    }

    private void traverse(Entry base, Consumer<MapleLogger> update) {
        update.accept(base.logger);
        for (int index = 0; index < base.children.size(); index++){
            traverse(base.children.get(index), update);
        }
    }

    public void updateConfig(String[] hierarchyIdentifier, Config config) {
        updateConfig(hierarchyIdentifier, old -> config);
    }

    public void updateConfig(String[] hierarchyIdentifier, ConfigManager.UpdateConfigFn configUpdateFn) {
        Entry base;
        if (hierarchyIdentifier.length == 0) {
            base = ROOT;
        } else {
            base = search(ROOT, hierarchyIdentifier, 0);
        }
        if (base != null) {
            traverse(base, logger -> {
                logger.updateConfig(configUpdateFn.applyUpdate(logger.getConfig()));
            });
        }
    }
}
