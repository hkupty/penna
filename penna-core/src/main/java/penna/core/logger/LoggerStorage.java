package penna.core.logger;

import org.jetbrains.annotations.NotNull;
import penna.api.config.Config;
import penna.api.config.ConfigManager;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import java.util.concurrent.atomic.AtomicReference;
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
 * {@link LoggerStorage#updateConfig(String, ConfigManager.ConfigurationChange)} for updating existing config with
 * a lambda for all update points and loggers below a certain prefix;
 * <br/>
 * {@link LoggerStorage#replaceConfig(String, Config)} for replacing the config reference for all update points and
 * loggers below a certain prefix;
 */
public class LoggerStorage {

    private static class Cursor {
        public ComponentNode node;
        public Config config;
        public int index;
        public int nextIndex;
        public boolean isMatch;
        public boolean earlyFinish;
        public char[][] path;
        private final int target;

        @SuppressWarnings("PMD.ArrayIsStoredDirectly")
        public Cursor(ComponentNode node, char[]... path) {
            this.path = path;
            this.index = 0;
            this.target = path.length;
            this.isMatch = false;
            this.earlyFinish = false;
            setNode(node);
        }

        private void setNode(ComponentNode node) {
            if (node == null) {
                this.earlyFinish = true;
                return;
            }

            Config cfg;
            if ((cfg = node.configRef.get()) != null) {
                this.config = cfg;
            }
            this.node = node;

        }

        /**
         * Ensures the values are within the range of [-1, 1], then makes them an array-accessor by incrementing
         *
         * @param offset Diff between two characters
         * @return a number in the range of [0, 2]
         */
        private int normalize(int offset) {
            return ((-offset >>> 31) - (offset >>> 31)) + 1;
        }

        public boolean next() {
            var key = path[index];
            nextIndex = normalize(Arrays.compare(key, node.component));
            index += (nextIndex & 0x1);
            isMatch = nextIndex == 1;
            if (index < target) setNode(node.children[nextIndex]);
            return index < target && !earlyFinish;
        }

        public boolean exactMatch() {
            return index >= target && isMatch && !earlyFinish;
        }

        public void createRemaining() {
            for (; index < target; index++) {
                node.lock.lock();
                try {
                    if (node.children[nextIndex] == null) {
                        node.children[nextIndex] = node.createChild(path[index]);
                    }
                } finally {
                    node.lock.unlock();
                    setNode(node.children[nextIndex]);
                    this.nextIndex = 1;
                }
            }
        }
    }

    /**
     * Stores a section (component) of the full-qualified name for the logger class, so
     * for a `com.company.myapp.controller.SalesController`, five nodes will be stored, one for each section
     * of the name, storing the values as a char[].
     * <br/>
     * This node can also serve as a config point in the hierarchy. If that's the case, all children loggers will
     * inherit the config of the nearest config point.
     *
     * @param component The char[] representing the component/section of the name.
     * @param children  The three subordinate nodes: Left, Bottom and Right.
     * @param loggerRef A potential reference to the {@link PennaLogger}
     * @param configRef A potential reference to a {@link Config}
     * @param lock      A lock for manipulating the children and/or the references.
     */
    private record ComponentNode(
            char[] component,
            ComponentNode[] children,
            AtomicReference<PennaLogger> loggerRef,
            AtomicReference<Config> configRef,
            Lock lock

    ) {

        /**
         * Static factory for creating a node with the correct values.
         *
         * @param component the char-array representing that node
         * @param config    the configuration for that segment
         * @return a new {@link ComponentNode} instance
         */
        static ComponentNode create(char[] component, Config config) {
            return new ComponentNode(
                    component,
                    new ComponentNode[3],
                    new AtomicReference<>(),
                    new AtomicReference<>(config),
                    new ReentrantLock(true)
            );
        }

        /**
         * This is a convenience factory method for creating a child node.
         *
         * @param component The char-array representing that node.
         * @return a new {@link ComponentNode} instance.
         */
        ComponentNode createChild(char... component) {
            return new ComponentNode(
                    component,
                    new ComponentNode[3],
                    new AtomicReference<>(),
                    new AtomicReference<>(),
                    new ReentrantLock(true)
            );
        }

        /**
         * Updates a configuration value that is stored in a {@link ComponentNode#configRef}.
         *
         * @param configurationChange a {@link ConfigManager.ConfigurationChange} lambda.
         * @return the updated configuration value.
         */
        Config updateConfigReference(ConfigManager.ConfigurationChange configurationChange) {
            var cfg = configRef.getAcquire();
            var updated = configurationChange.apply(cfg);
            configRef.setRelease(cfg);
            return updated;
        }

        /**
         * If this node contains a logger reference, updates its config.
         *
         * @param config the new configuration to be applied to that logger reference.
         */
        void updateLoggerConfig(Config config) {
            PennaLogger logger;
            if ((logger = loggerRef.get()) != null) {
                logger.updateConfig(config);
            }
        }

        /**
         * Set (or replace if existing) the configuration reference for this node.
         *
         * @param config the new configuration to be held by this node.
         */
        void replaceConfigReference(Config config) {
            configRef.set(config);
        }

        /**
         * Removes any configuration reference associated with this node.
         */
        void unsetConfig() {
            configRef.set(null);
        }

    }

    /**
     * The root of the TST. This is a special node that doesn't have any component, therefore all children of this node will
     * live on the left children.
     */
    private final ComponentNode root = ComponentNode.create(new char[]{}, Config.getDefault());

    /**
     * Transforms a FQ string into an array of components, being each component an array of chars.
     * For example, the string `io.app.controller.MyController` becomes
     * `[[i,o],[a,p,p],[c,o,n,t,r,o,l,l,e,r],[M,y,C,o,n,t,r,o,l,l,e,r]]`.
     *
     * @param key a FQ string, like the logger name.
     * @return an array of char arrays containing the components of the name.
     */
    private char[][] componentsForLoggerName(String key) {
        char[] keyChars = key.toCharArray();
        char[][] components = new char[16][];
        int base = 0;
        int componentIndex = 0;
        for (int i = 0; i < keyChars.length; i++) {
            if (keyChars[i] == '.') {
                if (componentIndex >= components.length) {
                    components = Arrays.copyOf(components, components.length * 2);
                }
                components[componentIndex++] = Arrays.copyOfRange(keyChars, base, i);
                base = i + 1;
            }
        }
        components[componentIndex++] = Arrays.copyOfRange(keyChars, base, keyChars.length);

        return Arrays.copyOfRange(components, 0, componentIndex);

    }

    private Cursor find(char[]... key) {
        Cursor cursor = new Cursor(root, key);
        while (cursor.next()) {}
        return cursor;
    }


    public PennaLogger getOrCreate(@NotNull String key) {
        var ret = find(componentsForLoggerName(key));

        if (!ret.exactMatch()) {
            ret.createRemaining();
        }

        PennaLogger logger = ret.node.loggerRef.getAcquire();
        if (logger == null) {
            logger = new PennaLogger(key, ret.config);
        }
        ret.node.loggerRef.setRelease(logger);

        return logger;
    }

    private void traverse(LoggerStorage.ComponentNode node, Config config) {
        Deque<ComponentNode> nodes = new ArrayDeque<>();
        ComponentNode next;
        for (ComponentNode child : node.children) {
            if (child != null) {
                nodes.push(child);
            }
        }

        while ((next = nodes.poll()) != null) {
            if (next.configRef.get() != null) {
                continue;
            }
            next.updateLoggerConfig(config);
            for (ComponentNode child : next.children) {
                if (child != null) {
                    nodes.push(child);
                }
            }
        }
    }


    public void updateConfig(
            @NotNull String prefix,
            @NotNull ConfigManager.ConfigurationChange configurationChange) {
        Cursor updatePoint = find(componentsForLoggerName(prefix));

        if (!updatePoint.exactMatch()) {
            // TODO handle correctly
            return;
        }

        Config newConfig = updatePoint.node.updateConfigReference(configurationChange);
        updatePoint.node.updateLoggerConfig(newConfig);
        var child = updatePoint.node.children[1]; // Only the middle child of the matched prefix should be traversed
        if (child != null) {
            child.updateLoggerConfig(newConfig);
            traverse(child, newConfig);
        }
    }

    public void replaceConfig(@NotNull String prefix,
                              @NotNull Config newConfig) {
        Cursor cursor = find(componentsForLoggerName(prefix));

        if (!cursor.exactMatch()) {
            // TODO handle correctly
            return;
        }

        ComponentNode updatePoint = cursor.node;

        updatePoint.replaceConfigReference(newConfig);
        updatePoint.updateLoggerConfig(newConfig);
        var child = updatePoint.children[1]; // Only the middle child of the matched prefix should be traversed
        if (child != null) {
            child.updateLoggerConfig(newConfig);
            traverse(child, newConfig);
        }
    }

    public void replaceConfig(@NotNull Config newConfig) {
        root.replaceConfigReference(newConfig);
        traverse(root, newConfig);
    }


    public void unsetConfigPoint(@NotNull String prefix) {
        ComponentNode updatePoint = find(componentsForLoggerName(prefix)).node;
        updatePoint.unsetConfig();
    }
}