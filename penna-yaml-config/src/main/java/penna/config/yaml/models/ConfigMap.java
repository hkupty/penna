package penna.config.yaml.models;

import java.util.Map;

public record ConfigMap(Map<String, ConfigNode> loggers, Boolean watch) {}
