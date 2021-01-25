package io.github.jiashunx.masker.rest.framework.model;

import io.github.jiashunx.masker.rest.framework.servlet.AbstractRestServlet;
import io.github.jiashunx.masker.rest.framework.servlet.mapping.HttpMethod;
import io.github.jiashunx.masker.rest.framework.util.MRestUtils;

import java.lang.reflect.Method;
import java.util.*;

/**
 * @author jiashunx
 */
public class ServletMappingClass {

    private final Class<? extends AbstractRestServlet> klass;
    private final String klassName;
    private final Map<String, ServletMappingHandler> handleMapping;

    public ServletMappingClass(Class<? extends AbstractRestServlet> klass) {
        this.klass = Objects.requireNonNull(klass);
        this.klassName = this.klass.getName();
        this.handleMapping = new HashMap<>();
    }

    public Class<? extends AbstractRestServlet> getKlass() {
        return klass;
    }

    public String getKlassName() {
        return klassName;
    }

    public ServletMappingHandler getMappingHandler(String requestUrl) {
        return handleMapping.get(requestUrl);
    }

    public void putMappingHandler(String requestUrl, ServletMappingHandler mappingHandler) {
        ServletMappingHandler _mappingHandler = getMappingHandler(requestUrl);
        if (_mappingHandler != null) {

        }
    }

}
