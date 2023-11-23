package penna.core.models;

public record KeyValuePair(String key, Object value) {
    public org.slf4j.event.KeyValuePair toSlf4j() {
        return new org.slf4j.event.KeyValuePair(key, value);
    }
}
