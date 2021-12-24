package io.github.jiashunx.masker.rest.framework.model;

import java.util.Objects;

/**
 * @author jiashunx
 */
public class MRestHeader {

    private final String key;
    private final Object value;

    public MRestHeader(String key, Object value) {
        this.key = String.valueOf(key);
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public Object getValue() {
        return value;
    }

    public MRestHeader copy() {
        return new MRestHeader(this.key, this.value);
    }

}
