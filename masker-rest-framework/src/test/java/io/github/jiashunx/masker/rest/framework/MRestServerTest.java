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
    public void test_sslStartAndGracefulShutdown() throws Exception {
        int port;
        try (ServerSocket socket = new ServerSocket(0)) {
            port = socket.getLocalPort();
        }
        MRestServer server = new MRestServer(port, "ut-ssl-server").sslEnabled(true);
        server.start();
        assertTrue(server.isSslEnabled());
        server.shutdown();
    }

}
