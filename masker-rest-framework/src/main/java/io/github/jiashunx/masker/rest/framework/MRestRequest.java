package io.github.jiashunx.masker.rest.framework;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import io.github.jiashunx.masker.rest.framework.cons.Constants;
import io.github.jiashunx.masker.rest.framework.serialize.MRestSerializer;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.cookie.Cookie;
import io.netty.handler.codec.http.cookie.ServerCookieDecoder;

import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * @author jiashunx
 */
public class MRestRequest {

    private HttpRequest httpRequest;

    private Map<String, String> attributes = new HashMap<>();
    private HttpMethod method;
    private String url;
    private String urlQuery;
    private Map<String, String> parameters;
    private Map<String, List<String>> originParameters;
    private HttpHeaders headers;
    private List<String> headerKeys;
    private List<Cookie> cookies;
    private Map<String, Cookie> cookieMap;
    private byte[] bodyBytes;

    public MRestRequest() {}

    public String bodyToString() {
        return new String(bodyBytes, StandardCharsets.UTF_8);
    }

    public JSONObject bodyToJSONObj() {
        return MRestSerializer.parseToJSONObject(bodyToString());
    }

    public JSONArray bodyToJSONArr() {
        return MRestSerializer.parseToJSONArray(bodyToString());
    }

    public <T> List<T> parseBodyToObjList(Class<T> klass) {
        return JSON.parseArray(bodyToString(), klass);
    }

    public <T> T parseBodyToObj(Class<T> klass) {
        return JSON.parseObject(bodyToString(), klass);
    }

    public HttpRequest getHttpRequest() {
        return httpRequest;
    }

    public void setHttpRequest(HttpRequest httpRequest) {
        this.httpRequest = httpRequest;
    }

    public Map<String, String> getAttributes() {
        return attributes;
    }

    public void setAttributes(Map<String, String> attributes) {
        this.attributes = attributes;
    }

    public void setAttribute(String key, String value) {
        attributes.put(key, value);
    }

    public String getAttribute(String key) {
        return attributes.get(key);
    }

    public HttpMethod getMethod() {
        return method;
    }

    public void setMethod(HttpMethod method) {
        this.method = method;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUrlQuery() {
        return urlQuery;
    }

    public void setUrlQuery(String urlQuery) {
        this.urlQuery = urlQuery;
    }

    public Map<String, String> getParameters() {
        return parameters;
    }

    public String getParameter(String key) {
        return getParameters().get(key);
    }

    public void setParameters(Map<String, String> parameters) {
        this.parameters = parameters;
    }

    public Map<String, List<String>> getOriginParameters() {
        return originParameters;
    }

    public void setOriginParameters(Map<String, List<String>> originParameters) {
        this.originParameters = originParameters;
    }

    public HttpHeaders getHeaders() {
        return headers;
    }

    public String getHeader(String key) {
        return getHeaders().get(key);
    }

    public List<String> getHeaderAll(String key) {
        return getHeaders().getAll(key);
    }

    public List<Map.Entry<String, String>> getHeaderEntries() {
        return getHeaders().entries();
    }

    public List<String> getHeaderKeys() {
        return this.headerKeys;
    }

    private void setHeaderKeys(List<String> headerKeys) {
        this.headerKeys = Objects.requireNonNull(headerKeys);
    }

    public List<Cookie> getCookies() {
        return cookies;
    }

    public void setCookies(List<Cookie> cookies) {
        this.cookies = Objects.requireNonNull(cookies);
    }

    public Cookie getCookie(String key) {
        return getCookieMap().get(key);
    }

    public Map<String, Cookie> getCookieMap() {
        return cookieMap;
    }

    private void setCookieMap(Map<String, Cookie> cookieMap) {
        this.cookieMap = Objects.requireNonNull(cookieMap);
    }

    public void setHeaders(HttpHeaders headers) {
        this.headers = headers;

        // ----------- set headerKeys
        Set<String> keySet = new HashSet<>();
        getHeaderEntries().forEach(entry -> {
            keySet.add(entry.getKey());
        });
        setHeaderKeys(new ArrayList<>(keySet));

        // ----------- set cookies
        List<String> cookieNames = new ArrayList<>();
        getHeaderKeys().forEach(key -> {
            if (key.toLowerCase().equals(Constants.HTTP_HEADER_COOKIE.toLowerCase())
                    || key.toLowerCase().equals(Constants.HTTP_HEADER_SET_COOKIE.toLowerCase())) {
                cookieNames.add(key);
            }
        });
        List<String> cookieStrList = new ArrayList<>(cookieNames.size());
        cookieNames.forEach(name -> {
            cookieStrList.addAll(getHeaderAll(name));
        });
        List<Cookie> _cookies = new ArrayList<>();
        cookieStrList.forEach(string -> {
            _cookies.addAll(ServerCookieDecoder.STRICT.decodeAll(string));
        });
        setCookies(_cookies);

        // ----------- set cookieMap
        Map<String, Cookie> _cookieMap = new HashMap<>();
        getCookies().forEach(cookie -> {
            _cookieMap.put(cookie.name(), cookie);
        });
        setCookieMap(_cookieMap);
    }

    public byte[] getBodyBytes() {
        return bodyBytes;
    }

    public void setBodyBytes(byte[] bodyBytes) {
        this.bodyBytes = bodyBytes;
    }
}
