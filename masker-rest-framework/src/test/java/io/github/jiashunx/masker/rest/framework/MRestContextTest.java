package io.github.jiashunx.masker.rest.framework;

import static org.junit.Assert.*;
import org.junit.Test;

import io.netty.handler.codec.http.HttpMethod;

public class MRestContextTest {

    @Test
    public void test_urlMappingHandlerLookupByMethod() {
        MRestServer server = new MRestServer(8080, "ctx-test-server");
        MRestContext context = server.context("/api");
        context.get("/hello", () -> "ok");
        // 触发映射注册
        context.init();
        assertNotNull("method-specific lookup should find GET handler", context.getUrlMappingHandler("/hello", HttpMethod.GET));
        assertNull("POST should not match GET-only mapping", context.getUrlMappingHandler("/hello", HttpMethod.POST));
        // 方法无关查找应命中(用于405判定)
        assertNotNull("url-level lookup should find any-method handler", context.getUrlMappingHandler("/hello"));
        assertNull(context.getUrlMappingHandler("/not-registered"));
    }

}
