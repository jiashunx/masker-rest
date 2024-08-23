package io.github.jiashunx.masker.rest.framework.model;

import java.util.*;

/**
 * @author jiashunx
 */
public class MRestHeaders {

    private final List<MRestHeader> $headers;

    public MRestHeaders() {
        this.$headers = new ArrayList<>();
    }

    public MRestHeaders(MRestHeaders headers) {
        this(headers == null ? new LinkedList<>() : headers.getHeaders());
    }

    public MRestHeaders(Map<String, Object> headers) {
        this(mapToHeaders(headers));
    }

    public MRestHeaders(MRestHeader... headerArr) {
        this(Arrays.asList(headerArr));
    }

    public MRestHeaders(List<MRestHeader> headerList) {
        this();
        addAll(headerList);
    }

    public static List<MRestHeader> mapToHeaders(Map<String, Object> headers) {
        List<MRestHeader> _headers = new ArrayList<>();
        if (headers != null && !headers.isEmpty()) {
            headers.forEach((key, value) -> {
                _headers.add(new MRestHeader(key, value));
            });
        }
        return _headers;
    }

    public List<MRestHeader> getHeaders() {
        return new ArrayList<>($headers);
    }

    public MRestHeaders addAll(MRestHeaders headers) {
        if (headers == null) {
            return this;
        }
        addAll(headers.getHeaders());
        return this;
    }

    public MRestHeaders addAll(List<MRestHeader> headerList) {
        if (headerList == null || headerList.isEmpty()) {
            return this;
        }
        for (MRestHeader header: headerList) {
            if (header == null) {
                continue;
            }
            $headers.add(header);
        }
        return this;
    }

    public MRestHeaders addAll(MRestHeader... headerArr) {
        return addAll(Arrays.asList(headerArr));
    }

    public MRestHeaders add(MRestHeader header) {
        return addAll(header);
    }

    public MRestHeaders add(String key, Object value) {
        return add(new MRestHeader(key, value));
    }

    public MRestHeaders add(Map<String, Object> headers) {
        return addAll(mapToHeaders(headers));
    }

    @Deprecated
    public Object get(String key) {
        List<Object> values = getAll(key);
        if (values == null || values.isEmpty()) {
            return null;
        }
        if (values.size() > 1) {
            throw new RuntimeException("expected one value, but has " + values.size());
        }
        return values.get(0);
    }

    public List<Object> getAll(String key) {
        List<Object> valueList = new LinkedList<>();
        for (MRestHeader header: $headers) {
            if (header.getKey().equals(key)) {
                valueList.add(header.getValue());
            }
        }
        return valueList;
    }

    public void remove(String key) {
        $headers.removeIf(header -> header.getKey().equals(key));
    }

    public MRestHeaders copy() {
        MRestHeaders newHeadersObj = new MRestHeaders();
        List<MRestHeader> headerList = this.getHeaders();
        if (headerList != null && !headerList.isEmpty()) {
            for (MRestHeader headerObj: headerList) {
                if (headerObj != null) {
                    newHeadersObj.add(headerObj.copy());
                }
            }
        }
        return newHeadersObj;
    }

}
