package penna.core.logger;

import org.jetbrains.annotations.NotNull;
import penna.api.config.Config;
import penna.api.config.ConfigManager;

import java.util.Arrays;
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

    private record NodeAndConfig(
            ComponentNode node,
            Config config
    ) {
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

        static ComponentNode create(char[] component, Config config) {
            return new ComponentNode(
                    component,
                    new ComponentNode[3],
                    new AtomicReference<>(),
                    new AtomicReference<>(config),
                    new ReentrantLock(true)
            );
        }

        ComponentNode createChild(char[] component) {
            return new ComponentNode(
                    component,
                    new ComponentNode[3],
                    new AtomicReference<>(),
                    new AtomicReference<>(),
                    new ReentrantLock(true)
            );
        }

        Config updateConfig(ConfigManager.ConfigurationChange configurationChange) {
            var cfg = configRef.getAcquire();
            var updated = configurationChange.apply(cfg);
            configRef.setRelease(cfg);
            return updated;
        }

        void replaceConfig(Config config) {
            configRef.set(config);
        }

        void unsetConfig() {
            configRef.set(null);
        }

    }

    private final ComponentNode root = ComponentNode.create(new char[]{}, Config.getDefault());

    /**
     * Ensures the values are within the range of [-1, 1]
     *
     * @param offset Diff between two characters
     * @return a number in the range of [-1, 1]
     */
    private int normalize(int offset) {
        return (-offset >>> 31) - (offset >>> 31);
    }

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


    private NodeAndConfig find(ComponentNode node,
                               char[][] key) {
        int target = key.length - 1;

        int nodeIx;
        int index = 0;
        ComponentNode cursor = node;
        Config cfg = null;
        char[] chr;

        do {
            chr = key[index];
            Config tmpCfg;
            if ((tmpCfg = cursor.configRef.get()) != null) {
                cfg = tmpCfg;
            }

            nodeIx = normalize(Arrays.compare(chr, cursor.component)) + 1;
            index = index + (nodeIx & 0x1);
            if (cursor.children[nodeIx] == null) {
                cursor.lock.lock();
                try {
                    if (cursor.children[nodeIx] == null) {
                        cursor.children[nodeIx] = cursor.createChild(key[index]);
                    }
                } finally {
                    cursor.lock.unlock();
                }
            }
            cursor = cursor.children[nodeIx];
        } while (index != target);
        return new NodeAndConfig(cursor, cfg);
    }


    public PennaLogger getOrCreate(@NotNull String key) {
        var ret = find(root, componentsForLoggerName(key));

        PennaLogger logger = ret.node.loggerRef.getAcquire();
        if (logger == null) {
            logger = new PennaLogger(key, ret.config);
        }
        ret.node.loggerRef.setRelease(logger);

        return logger;
    }

    private void traverse(LoggerStorage.ComponentNode node, Config config) {
        for (ComponentNode child : node.children) {
            if (child != null) {

                if (child.configRef.get() != null) {
                    return;
                }

                PennaLogger logger;
                if ((logger = child.loggerRef.get()) != null) {
                    logger.updateConfig(config);
                }

                traverse(child, config);
            }
        }
    }

    public void updateConfig(
            @NotNull String prefix,
            @NotNull ConfigManager.ConfigurationChange configurationChange) {
        ComponentNode updatePoint = find(root, componentsForLoggerName(prefix)).node;
        Config newConfig = updatePoint.updateConfig(configurationChange);
        traverse(updatePoint, newConfig);
    }

    public void replaceConfig(@NotNull String prefix,
                              @NotNull Config newConfig) {
        ComponentNode updatePoint = find(root, componentsForLoggerName(prefix)).node;
        updatePoint.replaceConfig(newConfig);
        traverse(updatePoint, newConfig);
    }


    public void unsetConfigPoint(@NotNull String prefix) {
        ComponentNode updatePoint = find(root, componentsForLoggerName(prefix)).node;
        updatePoint.unsetConfig();
    }
}