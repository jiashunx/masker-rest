package io.github.jiashunx.masker.rest.framework.model;

import io.github.jiashunx.masker.rest.framework.exception.MRestMappingException;
import io.github.jiashunx.masker.rest.framework.servlet.AbstractRestServlet;

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

    public List<String> getMappingUrls() {
        return new ArrayList<>(handleMapping.keySet());
    }

    public synchronized void putMappingHandler(String requestUrl, ServletMappingHandler mappingHandler) {
        ServletMappingHandler _mappingHandler = getMappingHandler(requestUrl);
        if (_mappingHandler != null) {
            throw new MRestMappingException(String.format("url: %s mapping conflict.", requestUrl));
        }
        handleMapping.put(requestUrl, mappingHandler);
    }

}
