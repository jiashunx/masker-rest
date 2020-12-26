package io.github.jiashunx.masker.rest.framework;

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

    private MRestContext restContext;
    private HttpRequest httpRequest;

    private Map<String, String> attributes = new HashMap<>();

    private String protocolName;
    private String protocolVersion;
    private String clientAddress;
    private int clientPort;
    private String remoteAddress;
    private int remotePort;

    private HttpMethod method;
    private String contextPath;
    /**
     * 带context-path的url.
     */
    private String originUrl;
    /**
     * 不带context-path的url.
     */
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

    public MRestRequest(MRestRequest restRequest) {
        this.restContext = restRequest.restContext;
        this.httpRequest = restRequest.httpRequest;
        this.attributes = restRequest.attributes;
        this.protocolName = restRequest.protocolName;
        this.protocolVersion = restRequest.protocolVersion;
        this.clientAddress = restRequest.clientAddress;
        this.clientPort = restRequest.clientPort;
        this.remoteAddress = restRequest.remoteAddress;
        this.remotePort = restRequest.remotePort;
        this.method = restRequest.method;
        this.contextPath = restRequest.contextPath;
        this.originUrl = restRequest.originUrl;
        this.url = restRequest.url;
        this.urlQuery = restRequest.urlQuery;
        this.parameters = restRequest.parameters;
        this.originParameters = restRequest.originParameters;
        this.headers = restRequest.headers;
        this.headerKeys = restRequest.headerKeys;
        this.cookies = restRequest.cookies;
        this.cookieMap = restRequest.cookieMap;
        this.bodyBytes = restRequest.bodyBytes;
    }

    @Override
    protected MRestRequest clone() throws CloneNotSupportedException {
        return (MRestRequest) super.clone();
    }

    public void release() {

    }

    public MRestContext getRestContext() {
        return restContext;
    }

    public void setRestContext(MRestContext restContext) {
        this.restContext = restContext;
    }

    public String bodyToString() {
        return new String(bodyBytes, StandardCharsets.UTF_8);
    }

    public <T> List<T> parseBodyToObjList(Class<T> klass) {
        return MRestSerializer.jsonToList(bodyBytes, klass);
    }

    public <T> T parseBodyToObj(Class<T> klass) {
        return MRestSerializer.jsonToObj(bodyBytes, klass);
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

    public String getProtocolName() {
        return protocolName;
    }

    public String getProtocolNameLowerCase() {
        return getProtocolName().toLowerCase();
    }

    public void setProtocolName(String protocolName) {
        this.protocolName = protocolName;
    }

    public String getProtocolVersion() {
        return protocolVersion;
    }

    public String getProtocolVersionLowerCase() {
        return getProtocolVersion().toLowerCase();
    }

    public void setProtocolVersion(String protocolVersion) {
        this.protocolVersion = protocolVersion;
    }

    public String getClientAddress() {
        return clientAddress;
    }

    public void setClientAddress(String clientAddress) {
        this.clientAddress = clientAddress;
    }

    public int getClientPort() {
        return clientPort;
    }

    public void setClientPort(int clientPort) {
        this.clientPort = clientPort;
    }

    public String getRemoteAddress() {
        return remoteAddress;
    }

    public void setRemoteAddress(String remoteAddress) {
        this.remoteAddress = remoteAddress;
    }

    public int getRemotePort() {
        return remotePort;
    }

    public void setRemotePort(int remotePort) {
        this.remotePort = remotePort;
    }

    public HttpMethod getMethod() {
        return method;
    }

    public void setMethod(HttpMethod method) {
        this.method = method;
    }

    public String getContextPath() {
        return contextPath;
    }

    public void setContextPath(String contextPath) {
        this.contextPath = contextPath;
    }

    public String getOriginUrl() {
        return originUrl;
    }

    public void setOriginUrl(String originUrl) {
        this.originUrl = originUrl;
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

    public boolean isUploadFile() {
        String contentType = getHeader(Constants.HTTP_HEADER_CONTENT_TYPE);
        if (contentType != null) {
            int idx = contentType.indexOf(";");
            if (idx > 0) {
                contentType = contentType.substring(0, idx);
            }
            contentType = contentType.trim();
        }
        return Constants.CONTENT_TYPE_MULTIPART_FORMDATA.equals(contentType);
    }

    /**
     * get header of "Accept".
     * @return Accept
     */
    public String getAccept() {
        return getHeader(Constants.HTTP_HEADER_ACCEPT);
    }

    /**
     * get first "Accept" from header.
     * @return Accept
     */
    public String getAcceptFirst() {
        String acceptFirst = getAccept();
        if (acceptFirst != null) {
            int idx = acceptFirst.indexOf(",");
            if (idx > 0) {
                acceptFirst = acceptFirst.substring(0, idx);
            }
        }
        if ("*/*".equals(acceptFirst)) {
            acceptFirst = null;
        }
        return acceptFirst;
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
