package io.github.jiashunx.masker.rest.framework.util;

import io.github.jiashunx.masker.rest.framework.MRestRequest;
import io.github.jiashunx.masker.rest.framework.MRestResponse;
import io.github.jiashunx.masker.rest.framework.servlet.AbstractRestServlet;
import io.github.jiashunx.masker.rest.framework.servlet.MRestServlet;

/**
 * @author jiashunx
 */
public class TestServlet implements MRestServlet {

    private static class OriginServlet extends AbstractRestServlet {

        public void test0() {

        }
        public void test1(MRestRequest request) {

        }
        public void test2(MRestRequest request, MRestResponse response) {

        }
    }

    private OriginServlet servlet;

    public TestServlet(OriginServlet servlet) {
        this.servlet = servlet;
    }

    @Override
    public void service(MRestRequest restRequest, MRestResponse restResponse) {
        servlet.test0();
    }
}
