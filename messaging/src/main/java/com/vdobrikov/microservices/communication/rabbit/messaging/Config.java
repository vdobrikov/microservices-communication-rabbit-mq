package com.vdobrikov.microservices.communication.rabbit.messaging;

import java.util.HashMap;
import java.util.Optional;
import java.util.function.Supplier;

public class Config extends HashMap<String, Object> {

    public Config with(String key, Object value) {
        put(key, value);
        return this;
    }

    public String getString(String key) {
        return (String) get(key);
    }

    public String getStringOrDefault(String key, String defaultValue) {
        return (String) getOrDefault(key, defaultValue);
    }

    public <T> T getOrDefaultGet(String key, Supplier<T> defaultSupplier) {
        return (T) Optional.ofNullable(get(key)).orElseGet(defaultSupplier);
    }

    public static Config build() {
        return new Config();
    }
}
