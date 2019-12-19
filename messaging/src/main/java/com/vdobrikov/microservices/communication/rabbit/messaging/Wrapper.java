package com.vdobrikov.microservices.communication.rabbit.messaging;

import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.HashMap;
import java.util.Map;

@ToString
@EqualsAndHashCode
public class Wrapper {
    public final static String OBJECT = "object";
    public final static String OBJECT_CLASS = "objectClass";
    public final static String FORWARD_TOPIC = "forwardTo";
    private Map<String, Object> properties = new HashMap<>();

    public Map<String, Object> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, Object> properties) {
        this.properties = properties;
    }

    public Wrapper withObject(Object object) {
        properties.put(OBJECT, object);
        properties.put(OBJECT_CLASS, object.getClass().getCanonicalName());
        return this;
    }

    public Wrapper withForwardTopic(String fwTopic) {
        properties.put(FORWARD_TOPIC, fwTopic);
        return this;
    }

    public Wrapper with(String name, Object value) {
        properties.put(name, value);
        return this;
    }

    public Object get(String property) {
        return properties.get(property);
    }
}
