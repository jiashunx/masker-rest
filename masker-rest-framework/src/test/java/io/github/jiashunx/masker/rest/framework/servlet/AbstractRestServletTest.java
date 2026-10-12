package io.github.jiashunx.masker.rest.framework.servlet;

import static org.junit.Assert.*;
import org.junit.Test;

import io.github.jiashunx.masker.rest.framework.servlet.mapping.GetMapping;
import io.github.jiashunx.masker.rest.framework.servlet.mapping.RequestMapping;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class AbstractRestServletTest {

    @RequestMapping(url = "/demo")
    public static class DemoServlet extends AbstractRestServlet {
        @GetMapping(url = "/hello")
        public void hello() {}
    }

    @Test
    public void test_init_idempotent() {
        DemoServlet servlet = new DemoServlet();
        servlet.init();
        servlet.init();
        List<String> urls = servlet.getMappingUrlList();
        assertTrue(urls.contains("/demo/hello"));
    }

    @Test
    public void test_init_concurrent() throws Exception {
        final DemoServlet servlet = new DemoServlet();
        int threadCount = 16;
        ExecutorService pool = Executors.newFixedThreadPool(threadCount);
        try {
            @SuppressWarnings("unchecked")
            Future<List<String>>[] futures = new Future[threadCount];
            for (int i = 0; i < threadCount; i++) {
                Callable<List<String>> task = () -> {
                    servlet.init();
                    return servlet.getMappingUrlList();
                };
                futures[i] = pool.submit(task);
            }
            for (Future<List<String>> future: futures) {
                List<String> urls = future.get();
                assertTrue(urls.contains("/demo/hello"));
            }
        } finally {
            pool.shutdownNow();
        }
    }

}
