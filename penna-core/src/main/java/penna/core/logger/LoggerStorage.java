package penna.core.logger;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.VisibleForTesting;
import penna.api.models.Config;
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
    @VisibleForTesting
    static class Node {
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


        void setConfigAndUpdateRecursively(@NotNull Config baseConfig) {
            lock.lock();
            try {
                configRef = baseConfig;

                if (loggerRef != null) {
                    loggerRef.updateConfig(baseConfig);
                }
            } finally {
                lock.unlock();
            }
            if (children[1] != null) {children[1].updateRecursively(baseConfig);}
        }

        void updateRecursively(@NotNull Config baseConfig) {
            lock.lock();
            try {
                if (configRef != null) {
                    // TODO: This might be a little problematic. Needs to be investigated further
                    //  A Configuration object might need a stamp so we can differentiate
                    //  a valid replacement (i.e. a newer version is updating old data) from an
                    //  invalid one.
                    configRef = baseConfig;
                }

                if (loggerRef != null) {
                    loggerRef.updateConfig(baseConfig);
                }
            } finally {
                lock.unlock();
            }
            if (children[0] != null) {children[0].updateRecursively(baseConfig);}
            if (children[1] != null) {children[1].updateRecursively(baseConfig);}
            if (children[2] != null) {children[2].updateRecursively(baseConfig);}
        }
    }

    /**
     * The root of the TST. This is a special node that doesn't have any component, therefore all children of this node will
     * live on the left children.
     */
    @VisibleForTesting
    final Node root = Node.create("", Config.getDefault());

    /**
     * Recursively creates nodes in the tree until it reaches the leaf as presented by the supplied key.
     * Creates a logger if missing and returns it.
     *
     * @param key a string containing a fully qualified class name.
     * @return a {@link PennaLogger}
     */
    public PennaLogger getOrCreate(@NotNull String key) {
        StringNavigator path = new StringNavigator(key);
        Node cursor = root;
        Config config = root.configRef;
        int nodeIndex = 2; // Anything will be greater than ""

        while (path.hasNext()) {
            StringNavigator.StringView view = path.next();
            do {
                var next = cursor.children[nodeIndex];
                if (next == null) {
                    cursor.lock.lock();
                    try {
                        cursor.children[nodeIndex] = new Node(view.toString());
                        next = cursor.children[nodeIndex];
                    } finally {
                        cursor.lock.unlock();
                    }
                }
                cursor = next;
            } while ((nodeIndex = view.indexCompare(cursor.component)) != 1);
            if (cursor.configRef != null) {
                config = cursor.configRef;
            }
        }

        if (cursor.loggerRef == null) {
            cursor.loggerRef = new PennaLogger(key, config);
        }

        return cursor.loggerRef;
    }

    public void replaceConfig(@NotNull String prefix,
                              @NotNull Config newConfig) {
        StringNavigator path = new StringNavigator(prefix);
        Node cursor = root;
        int nodeIndex = 2; // Anything will be greater than ""

        while (path.hasNext()) {
            StringNavigator.StringView view = path.next();
            do {
                var next = cursor.children[nodeIndex];
                if (next == null) {
                    cursor.lock.lock();
                    try {
                        cursor.children[nodeIndex] = new Node(view.toString());
                        next = cursor.children[nodeIndex];
                    } finally {
                        cursor.lock.unlock();
                    }
                }
                cursor = next;
            } while ((nodeIndex = view.indexCompare(cursor.component)) != 1);
        }

        cursor.setConfigAndUpdateRecursively(newConfig);
    }

    /**
     * Returns the configuration that is applied to the requested prefix, if not directly associated, the one
     * applied to the nearest ancestor.
     * @param prefix the path to the logger
     * @return The configuration instance that is applied to it
     */
    public @NotNull Config getConfig(@NotNull String prefix) {
        StringNavigator path = new StringNavigator(prefix);
        Node cursor = root;
        Config configRef = root.configRef;
        int nodeIndex = 2; // given root is "", any child is located at index 2, so first hop is "free"
        while (cursor != null && path.hasNext()) {
            StringNavigator.StringView view = path.next();
            do {
                cursor = cursor.children[nodeIndex];
            } while (cursor != null && (nodeIndex = view.indexCompare(cursor.component)) != 1);

            configRef = switch (cursor) {
                case null -> configRef;
                case Node c when c.configRef == null -> configRef;
                default -> cursor.configRef;
            };
        }
        return configRef;
    }

    public void replaceConfig(@NotNull Config newConfig) {
        root.updateRecursively(newConfig);
    }
}