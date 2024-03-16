package penna.config.yaml.models;

import org.slf4j.event.Level;
import penna.api.config.Config;
import penna.api.config.ExceptionHandling;
import penna.api.models.LogField;

import java.util.List;

public sealed interface ConfigNode {
    Config toConfig();


    sealed interface SetsLevel {
        String level();
    }

    sealed interface SetsFields {
        List<String> fields();
    }

    sealed interface SetsException {
        ExceptionHandling exception();
    }

    record OnlyLevel(String level) implements ConfigNode, SetsLevel {
        @Override
        public Config toConfig() {
            return Config.getDefault().replaceLevel(Level.valueOf(level.toUpperCase()));
        }
    }

    record OnlyFields(List<String> fields) implements ConfigNode, SetsFields {
        @Override
        public Config toConfig() {
            return Config
                    .withFields(fields.stream().map(LogField::fromFieldName).toArray(LogField[]::new));
        }
    }

    record OnlyExceptions(ExceptionHandling exception) implements ConfigNode, SetsException {
        @Override
        public Config toConfig() {
            return Config.getDefault().replaceExceptionHandling(exception);
        }
    }

    record LevelAndFields(String level, List<String> fields) implements ConfigNode, SetsLevel, SetsFields {
        @Override
        public Config toConfig() {
            return Config.withFields(
                    Level.valueOf(level.toUpperCase()),
                    fields.stream().map(LogField::fromFieldName).toArray(LogField[]::new)
            );
        }
    }

    record LevelAndExceptions(String level,
                              ExceptionHandling exception) implements ConfigNode, SetsLevel, SetsException {
        @Override
        public Config toConfig() {
            return Config
                    .getDefault()
                    .replaceLevel(Level.valueOf(level.toUpperCase()))
                    .replaceExceptionHandling(exception);
        }
    }

    record FieldsAndException(List<String> fields,
                              ExceptionHandling exception) implements ConfigNode, SetsFields, SetsException {
        @Override
        public Config toConfig() {
            return Config
                    .withFields(fields.stream().map(LogField::fromFieldName).toArray(LogField[]::new))
                    .replaceExceptionHandling(exception);
        }
    }

    record CompleteConfig(String level, List<String> fields,
                          ExceptionHandling exception) implements ConfigNode, SetsLevel, SetsFields, SetsException {
        @Override
        public Config toConfig() {
            return new Config(
                    Level.valueOf(level.toUpperCase()),
                    fields.stream().map(LogField::fromFieldName).toArray(LogField[]::new),
                    exception
            );
        }
    }
}
