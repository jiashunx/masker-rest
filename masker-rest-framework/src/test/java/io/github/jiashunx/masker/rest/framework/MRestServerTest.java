package io.github.jiashunx.masker.rest.framework;

import static org.junit.Assert.*;
import org.junit.Test;

import java.net.ServerSocket;

public class MRestServerTest {

    @Test
    public void test_startAndGracefulShutdown() throws Exception {
        int port;
        try (ServerSocket socket = new ServerSocket(0)) {
            port = socket.getLocalPort();
        }
        MRestServer server = new MRestServer(port, "ut-server");
        server.start();
        server.shutdown();
        // 关闭后重复关闭应抛出异常
        try {
            server.shutdown();
            fail("expected MRestServerCloseException on second shutdown");
        } catch (RuntimeException expected) {
            // expected
        }
    }

    @Test
    public void test_sslStartAndGracefulShutdown() throws Exception {        int port;
        try (ServerSocket socket = new ServerSocket(0)) {
            port = socket.getLocalPort();
        }
        MRestServer server = new MRestServer(port, "ut-ssl-server").sslEnabled(true);
        server.start();
        assertTrue(server.isSslEnabled());
        server.shutdown();
    }

    @Test
    public void test_multipartMaxFileCountConfigurable() {
        MRestServer server = new MRestServer(8081, "cfg-server");
        assertTrue(server.getMultipartMaxFileCount() > 0);
        server.multipartMaxFileCount(10);
        assertEquals(10, server.getMultipartMaxFileCount());
    }

    @Test
    public void test_securityConfigDefaults() {
        MRestServer server = new MRestServer(8082, "sec-server");
        assertFalse("idle timeout disabled by default", server.getHttpIdleTimeoutSeconds() > 0);
        assertTrue("fingerprint headers enabled by default", server.isFingerprintHeadersEnabled());
        assertEquals("business thread pool disabled by default", 0, server.getBusinessThreadNum());
        server.httpIdleTimeoutSeconds(30).fingerprintHeadersEnabled(false);
        assertEquals(30, server.getHttpIdleTimeoutSeconds());
        assertFalse(server.isFingerprintHeadersEnabled());
    }

    @Test
    public void test_businessThreadPoolStartAndShutdown() throws Exception {
        int port;
        try (ServerSocket socket = new ServerSocket(0)) {
            port = socket.getLocalPort();
        }
        MRestServer server = new MRestServer(port, "biz-server").businessThreadNum(2);
        server.start();
        assertNotNull(server.getBusinessGroup());
        server.shutdown();
        assertNull(server.getBusinessGroup());
    }

}
