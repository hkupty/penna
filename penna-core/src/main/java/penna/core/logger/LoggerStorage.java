package penna.core.logger;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import penna.api.config.Config;
import penna.api.config.ConfigManager;
import penna.core.internals.StringNavigator;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Stores the logger instances for reuse in a Ternary Search Tree (TST) structure.
 * <br/>
 * Given namespaces naturally hierarchical structure, it is more space-efficient and performant
 * to store each namespace component (section preceded/followed by a dot) as a node in the
 * TST, instead of doing the traditional char-by-char trie/TST.
 * <br/>
 * <br/>
 * This TST can store two kinds of values: {@link PennaLogger} and {@link Config}.
 * The loggers exist on some leaf nodes, as they're likely class names: `* -> io -> app -> controller -> MyController`,
 * with `MyController` being a leaf object pointing to a {@link PennaLogger}.
 * <br/>
 * The {@link Config} references can live in the middle of the TST. For example `* -> io -> app -> controller` can hold
 * a reference to a {@link Config} where the log level is {@link org.slf4j.event.Level#DEBUG} whereas the rest of the
 * Logger references will be under {@link Config#getDefault()}, which is stored in the root of the TST.
 * <br/>
 * <br/>
 * The logger storage exposes three public methods:
 * <br/>
 * {@link LoggerStorage#getOrCreate(String)}: For retrieving an existing logger or creating a new one if none existent
 * for the key;
 * <br/>
 * {@link LoggerStorage#replaceConfig(String, Config)} for replacing the config reference for all update points and
 * loggers below a certain prefix;
 */
public class LoggerStorage {


    /**
     * Stores a section (component) of the full-qualified name for the logger class, so
     * for a `com.company.myapp.controller.SalesController`, five nodes will be stored, one for each section
     * of the name.
     * <br/>
     * This node can also serve as a config point in the hierarchy. If that's the case, all children loggers will
     * inherit the config of the nearest config point.
     */
    private static class Node {
        private Node(String component) {this.component = component;}

        public static Node create(String component, Config config) {
            var node = new Node(component);
            node.configRef = config;
            return node;
        }

        public final String component;
        public final Node[] children = new Node[3];
        public PennaLogger loggerRef;
        public Config configRef;
        public final Lock lock = new ReentrantLock();


        void setConfigAndUpdateRecursively(Config baseConfig, @Nullable ConfigManager.ConfigurationChange updateFn) {
            lock.lock();
            try {
                configRef = baseConfig;

                if (loggerRef != null && baseConfig != null) {
                    loggerRef.updateConfig(baseConfig);
                }
            } finally {
                lock.unlock();
            }
            if (children[1] != null) {children[1].updateRecursively(baseConfig, updateFn);}
        }

        void updateRecursively(Config baseConfig, @Nullable ConfigManager.ConfigurationChange updateFn) {
            Config newConfig = baseConfig;
            lock.lock();
            try {
                if (configRef != null) {
                    if (updateFn != null) {
                        newConfig = updateFn.apply(configRef);
                    }
                    configRef = newConfig;
                }

                if (loggerRef != null && newConfig != null) {
                    loggerRef.updateConfig(newConfig);
                }
            } finally {
                lock.unlock();
            }
            if (children[0] != null) {children[0].updateRecursively(newConfig, updateFn);}
            if (children[1] != null) {children[1].updateRecursively(newConfig, updateFn);}
            if (children[2] != null) {children[2].updateRecursively(newConfig, updateFn);}
        }
    }

    /**
     * The root of the TST. This is a special node that doesn't have any component, therefore all children of this node will
     * live on the left children.
     */
    private final Node root = Node.create("", Config.getDefault());


    public PennaLogger getOrCreate(@NotNull String key) {
        var path = new StringNavigator(key);
        var cursor = root;
        var pathIndex = 0;
        int nodeIndex;
        Config config = root.configRef;
        do {
            nodeIndex = path.indexCompare(pathIndex, cursor.component);
            pathIndex += (nodeIndex & 0x1);
            if (pathIndex < path.target) {
                Node node;
                if ((node = cursor.children[nodeIndex]) != null) {
                    cursor = node;
                    var cfg = node.configRef;
                    if (cfg != null) {
                        config = cfg;
                    }
                } else {
                    break;
                }
            } else break;
        } while (true);

        while (pathIndex < path.target) {
            cursor.lock.lock();
            try {
                if (cursor.children[nodeIndex] == null) {
                    cursor.children[nodeIndex] = new Node(path.chunk(pathIndex++).toString());
                }
            } finally {
                cursor.lock.unlock();
                cursor = cursor.children[nodeIndex];
                nodeIndex = 1;
            }
        }

        PennaLogger logger = cursor.loggerRef;
        if (logger == null) {
            logger = new PennaLogger(key, config);
        }
        cursor.loggerRef = logger;
        return logger;
    }

    public void replaceConfig(@NotNull String prefix,
                              @NotNull Config newConfig) {
        var path = new StringNavigator(prefix);
        var cursor = root;
        var pathIndex = 0;
        int nodeIndex;
        do {
            nodeIndex = path.indexCompare(pathIndex, cursor.component);
            pathIndex += (nodeIndex & 0x1);
            if (pathIndex < path.target) {
                Node node;
                if ((node = cursor.children[nodeIndex]) != null) {
                    cursor = node;
                } else {

                    break;
                }
            } else break;
        } while (true);

        cursor.setConfigAndUpdateRecursively(newConfig, null);
    }

    public void updateConfig(@NotNull String prefix,
                             @NotNull ConfigManager.ConfigurationChange configUpdateFn) {
        var path = new StringNavigator(prefix);
        var cursor = root;
        var pathIndex = 0;
        int nodeIndex;
        Config config = root.configRef;
        do {
            nodeIndex = path.indexCompare(pathIndex, cursor.component);
            pathIndex += (nodeIndex & 0x1);
            if (pathIndex < path.target) {
                Node node;
                if ((node = cursor.children[nodeIndex]) != null) {
                    cursor = node;
                    var cfg = node.configRef;
                    if (cfg != null) {
                        config = cfg;
                    }
                } else {

                    break;
                }
            } else break;
        } while (true);

        cursor.setConfigAndUpdateRecursively(configUpdateFn.apply(config), configUpdateFn);
    }

    public void replaceConfig(@NotNull Config newConfig) {
        root.lock.lock();
        try {
            root.configRef = newConfig;
        } finally {
            root.lock.unlock();
        }
        root.updateRecursively(newConfig, null);
    }

}