package penna.config.yaml;

class NodeTest {

//    @Test
//    void CanReadSimpleYaml() throws JsonProcessingException {
//        var provider = new YamlConfigProvider();
//
//        var loggers = """
//                ---
//                loggers:
//                    penna:
//                        level: debug
//                """;
//
//        var tree = provider.mapper.readTree(loggers);
//        var result = provider.readConfig(tree);
//
//        var reference = new ConfigMap(Map.of("penna", new ConfigNode.OnlyLevel("debug")));
//        Assertions.assertEquals(reference, result);
//    }
//
//    @Test
//    void CanReadMultipleFields() throws JsonProcessingException {
//        var provider = new YamlConfigProvider();
//
//        var loggers = """
//                ---
//                loggers:
//                    penna:
//                        level: debug
//                        fields:
//                            - timestamp
//                            - message
//                            - thread
//                """;
//
//        var tree = provider.mapper.readTree(loggers);
//        var result = provider.readConfig(tree);
//
//        var reference = new ConfigMap(Map.of("penna", new ConfigNode.LevelAndFields("debug", List.of(
//                "timestamp",
//                "message",
//                "thread"
//        ))));
//        Assertions.assertEquals(reference, result);
//    }
//
//
//    @Test
//    void CanReadMultipleFields2() throws JsonProcessingException {
//        var provider = new YamlConfigProvider();
//        var loggers = """
//                ---
//                loggers:
//                    "": { level: error }
//                    penna: { level: debug }
//                    penna.loggers.yaml: { level: trace, fields: [message] }
//                    com.other: { level: warn }
//                """;
//
//        var tree = provider.mapper.readTree(loggers);
//        var result = provider.readConfig(tree);
//
//        var reference = new ConfigMap(
//                Map.of("", new ConfigNode.OnlyLevel("error"),
//                        "penna", new ConfigNode.OnlyLevel("debug"),
//                        "penna.loggers.yaml", new ConfigNode.LevelAndFields("trace", List.of("message")),
//                        "com.other", new ConfigNode.OnlyLevel("warn")
//                ));
//
//        Assertions.assertEquals(reference, result);
//    }
//
//    @Test
//    void CanReadExceptionConfigs() throws JsonProcessingException {
//        var provider = new YamlConfigProvider();
//        var loggers = """
//                ---
//                loggers:
//                    penna:
//                        level: debug
//                        exception:
//                            deduplication: true
//                            maxDepth: 128
//                            traverseDepth: 3
//                """;
//
//        var tree = provider.mapper.readTree(loggers);
//        var result = provider.readConfig(tree);
//
//        var reference = new ConfigMap(
//                Map.of("penna",
//                        new ConfigNode.LevelAndExceptions(
//                                "debug",
//                                new ExceptionHandling(128, 3, true)
//                        )
//                ));
//
//        Assertions.assertEquals(reference, result);
//    }
//
//    class DummyStorage implements Storage {
//        HashMap<String, Config> simpleStorage = new HashMap<>();
//
//        @Override
//        public void apply(ConfigToLogger... configs) {
//            for (var loggers : configs) {
//                if (loggers instanceof ConfigToLogger.RootLoggerConfigItem rootLoggerConfigItem) {
//                    simpleStorage.put("", rootLoggerConfigItem.loggers());
//                } else if (loggers instanceof ConfigToLogger.NamedLoggerConfigItem namedLoggerConfigItem) {
//                    simpleStorage.put(namedLoggerConfigItem.logger(), namedLoggerConfigItem.loggers());
//                }
//            }
//        }
//
//        @Override
//        public Config get(String logger) {
//            return simpleStorage.get(logger);
//        }
//    }
//
//    @Test
//    void fullConfigTest() {
//        var dummy = new DummyStorage();
//        var impl = Manager.Factory.initialize(dummy);
//
//        System.out.println(impl);
//    }
//
}