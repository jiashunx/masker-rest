package io.github.jiashunx.masker.rest.framework.wrapper;

import io.github.jiashunx.masker.rest.framework.MRestRequest;

import java.util.Map;
import java.util.Objects;

/**
 * @author jiashunx
 */
public class MRestRequestWrapper extends MRestRequest {

    public MRestRequestWrapper(MRestRequest request) {
        super(request);
    }

    public void addParameter(String key, String value) {
        getParameters().put(key, value);
    }

    public void addParameters(Map<String, String> params) {
        getParameters().putAll(params);
    }

    public void setBodyBytes(byte[] bodyBytes) {
        super.bodyBytes = Objects.requireNonNull(bodyBytes);
    }

}
