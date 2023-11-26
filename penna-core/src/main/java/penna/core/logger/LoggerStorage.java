package penna.core.logger;

import org.jetbrains.annotations.NotNull;
import penna.api.config.Config;
import penna.api.config.ConfigManager;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

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

    }

    private static final Consumer<ComponentNode> DO_NOTHING = componentNode -> {
    };
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

    private ComponentNode find(ComponentNode node, String key) {
        return find(node, key, DO_NOTHING);
    }

    private ComponentNode find(ComponentNode node, String key, Consumer<ComponentNode> processNode) {
        if (key == null || key.isEmpty()) {
            return node;
        }

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

        return find(node, Arrays.copyOfRange(components, 0, componentIndex), 0, processNode);
    }


    private ComponentNode find(ComponentNode node,
                               char[][] key,
                               int index,
                               Consumer<ComponentNode> processNode
    ) {
        processNode.accept(node);
        char[] chr = key[index];
        int nodeIx = normalize(Arrays.compare(chr, node.component)) + 1;
        if (nodeIx == 1 && key.length == index + 1) {
            return node;
        } else {
            // We only advance the index if we're hitting the middle node.
            int nextIx = index + (nodeIx & 0x1);
            if (node.children[nodeIx] == null) {
                node.lock.lock();
                try {
                    if (node.children[nodeIx] == null) {
                        node.children[nodeIx] = node.createChild(key[nextIx]);
                    }
                } finally {
                    node.lock.unlock();
                }
            }
            ComponentNode next = node.children[nodeIx];
            return find(next, key, nextIx, processNode);
        }
    }


    public PennaLogger getOrCreate(@NotNull String key) {
        AtomicReference<Config> closestConfig = new AtomicReference<>();
        ComponentNode node = find(root, key, componentNode -> {
            Config nodesConfig;
            if ((nodesConfig = componentNode.configRef.get()) != null) {
                closestConfig.set(nodesConfig);
            }
        });
        PennaLogger logger = node.loggerRef.getAcquire();
        if (logger == null) {
            logger = new PennaLogger(key, closestConfig.get());
        }
        node.loggerRef.setRelease(logger);

        return logger;
    }


    private void traverse(LoggerStorage.ComponentNode node,
                          Config config,
                          ConfigManager.ConfigurationChange configurationChange
    ) {
        for (ComponentNode child : node.children) {
            if (child != null) {
                Config innerCfg = config;
                if (child.configRef.get() != null) {
                    innerCfg = child.updateConfig(configurationChange);
                }

                PennaLogger logger;
                if ((logger = child.loggerRef.get()) != null) {
                    logger.updateConfig(innerCfg);
                }
                traverse(child, innerCfg, configurationChange);
            }
        }
    }

    private void traverse(LoggerStorage.ComponentNode node, Config config) {
        for (ComponentNode child : node.children) {
            if (child != null) {
                if (child.configRef.get() != null) {
                    child.replaceConfig(config);
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
        ComponentNode updatePoint = find(root, prefix);
        Config newConfig = updatePoint.updateConfig(configurationChange);
        traverse(updatePoint, newConfig, configurationChange);
    }

    public void replaceConfig(@NotNull String prefix,
                              @NotNull Config newConfig) {
        ComponentNode updatePoint = find(root, prefix);
        updatePoint.replaceConfig(newConfig);
        traverse(updatePoint, newConfig);
    }
}