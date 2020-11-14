package io.github.jiashunx.masker.rest.framework;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import io.github.jiashunx.masker.rest.framework.serialize.MRestSerializer;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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

    public void setHeaders(HttpHeaders headers) {
        this.headers = headers;
    }

    public byte[] getBodyBytes() {
        return bodyBytes;
    }

    public void setBodyBytes(byte[] bodyBytes) {
        this.bodyBytes = bodyBytes;
    }
}
