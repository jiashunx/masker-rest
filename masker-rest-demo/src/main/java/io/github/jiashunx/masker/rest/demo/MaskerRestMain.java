package io.github.jiashunx.masker.rest.demo;

import io.github.jiashunx.masker.rest.framework.MRestRequest;
import io.github.jiashunx.masker.rest.framework.MRestResponse;
import io.github.jiashunx.masker.rest.framework.MRestServer;
import io.github.jiashunx.masker.rest.framework.exception.MRestJWTException;
import io.github.jiashunx.masker.rest.framework.filter.Filter;
import io.github.jiashunx.masker.rest.framework.filter.MRestFilter;
import io.github.jiashunx.masker.rest.framework.filter.MRestFilterChain;
import io.github.jiashunx.masker.rest.framework.model.MRestServerThreadModel;
import io.github.jiashunx.masker.rest.framework.util.MRestHeaderBuilder;
import io.github.jiashunx.masker.rest.framework.util.MRestJWTHelper;
import io.github.jiashunx.masker.rest.framework.util.SharedObjects;
import io.github.jiashunx.masker.rest.framework.util.StringUtils;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.cookie.Cookie;
import io.netty.handler.codec.http.cookie.DefaultCookie;
import io.netty.util.NettyRuntime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;

/**
 * @author jiashunx
 */
public class MaskerRestMain {

    private static final Logger logger = LoggerFactory.getLogger(MaskerRestMain.class);

    private static final MRestJWTHelper JWT_HELPER = new MRestJWTHelper("qwerasdfzxcv09876543231");

    public static void main(String[] args) {
        new MRestServer()
                .listenPort(21700)
                .contextPath("/demo")
                .serverName("demo-21700")
                .connectionKeepAlive(false)
                .workerThreadNum(NettyRuntime.availableProcessors() * 2)
                .bossThreadNum(1)

                // get请求样例
                .get("/get-NoParam-NoRet", () -> {
                    MRestServerThreadModel threadModel = SharedObjects.getServerThreadModel();
                    MRestRequest request = threadModel.getRestRequest();
                    logger.info("get, origin-url: {}", request.getOriginUrl());
                })
                .get("/get-ParamReq-NoRet", request -> {
                    logger.info("get, param-req, no ret");
                })
                .get("/get-html", request -> {
                    logger.info("get, param-req, return html");
                    return "<html><body>this is a html page !</body></html>";
                }, MRestHeaderBuilder.Build("Content-Type", "text/html"))
                .get("/get-text", request -> {
                    logger.info("get, param-req, return text");
                    return "text.......";
                }, MRestHeaderBuilder.Build("Content-Type", "text/plain"))
                .get("/get-ParamReqResp-NoRet", (request, response) -> {
                    logger.info("get, param-req-resp, no ret");
                })

                // post请求样例
                .post(("/post-form"), request -> {
                    logger.info("post, form data: {}", request.parseBodyToObj(Vo.class));
                    return new HashMap<String, Object>();
                })

                // forward请求样例, 服务端转发
                .post("/post-forward", (request, response) -> {
                    logger.info("post forward, forward to /post-forward-target");
                    request.setAttribute("hk-01", "hk-01-value");
                    response.forward("/post-forward-target", request);
                })
                .post("/post-forward-target", (request, response) -> {
                    logger.info("/post forward target, receive attribute: hk-01={}", request.getAttribute("hk-01"));
                    response.write(HttpResponseStatus.OK);
                })

                // redirect请求样例, 客户端重定向
                .get("/get-redirect.html", (request, response) -> {
                    logger.info("get redirect, redirect to /get-redirect-target.html");
                    response.redirect("/get-redirect-target.html");
                })
                .get("/get-redirect-target.html", (request) -> {
                    return "<html><body>this is an redirected html page !</body></html>";
                }, MRestHeaderBuilder.Build("Content-Type", "text/html"))

                // 过滤器样例(过滤器执行顺序按照order值从小到大顺序执行)
                .filter("/filter-test/*", new Filter0(), new Filter1())
                .get("/filter-test/get0", request -> {
                    // do nothing.
                    logger.info("get, /filter-test/get0");
                })

                // cookie样例
                .get("/cookie/set-cookie", (request, response) -> {
                    String cookieVal = "xxxxxxxxxxxxxxxxxx";
                    logger.info("set-cookie, hello=\"{}\"", cookieVal);
                    response.setCookie("hello", cookieVal);
                    Cookie nCookie = new DefaultCookie("hello0", "hhhhhh");
                    response.setCookie(nCookie);
                })
                .get("/cookie/get-cookie", request -> {
                    logger.info("get-cookie, map: {}", request.getCookieMap());
                    logger.info("get-cookie, list: {}", request.getCookies());
                })

                // jwt会话过滤样例
                // /jwt/login请求body: {"username": "admin"} 进行会话登录验证
                // /jwt/xxx获取资源
                .post("/jwt/xxx", request -> {})
                .filter("/jwt/*", (request, response, filterChain) -> {
                    String requestURL = request.getUrl();
                    if ("/jwt/login".equals(requestURL) && request.getMethod() == HttpMethod.POST) {
                        Vo vo = request.parseBodyToObj(Vo.class);
                        if (vo.username.equals("admin")) {
                            String jwtToken = JWT_HELPER.newToken();
                            Cookie jwtCookie = new DefaultCookie("mmm-jwt-token", jwtToken);
                            jwtCookie.setMaxAge(10*60*1000L);
                            response.setCookie(jwtCookie);
                            response.setHeader("MMM-JWT-TOKEN", jwtToken);
                            response.write(HttpResponseStatus.OK);
                        } else {
                            response.write(HttpResponseStatus.UNAUTHORIZED);
                        }
                    } else {
                        Cookie cookie = request.getCookie("mmm-jwt-token");
                        String jwtToken = cookie == null ? null : cookie.value();
                        if (StringUtils.isBlank(jwtToken)) {
                            response.write(HttpResponseStatus.UNAUTHORIZED);
                            return;
                        }
                        try {
                            if (!JWT_HELPER.isTokenTimeout(jwtToken) && JWT_HELPER.isTokenValid(jwtToken)) {
                                filterChain.doFilter(request, response);
                                String newToken = JWT_HELPER.updateToken(jwtToken);
                                Cookie jwtCookie = new DefaultCookie("mmm-jwt-token", newToken);
                                jwtCookie.setMaxAge(10*60*1000L);
                                response.setCookie(jwtCookie);
                                response.setHeader("MMM-JWT-TOKEN", newToken);
                                return;
                            }
                        } catch (MRestJWTException e) {
                            logger.error("", e);
                        }
                        response.write(HttpResponseStatus.UNAUTHORIZED);
                    }
                })

                .start();
    }

    @Filter(order = 123)
    private static class Filter0 implements MRestFilter {
        @Override
        public void doFilter(MRestRequest restRequest, MRestResponse restResponse, MRestFilterChain filterChain) {
            logger.info("filter0 second(order=123) -->> " + restRequest.getUrl());
            filterChain.doFilter(restRequest, restResponse);
        }
    }

    @Filter(order = -123)
    private static class Filter1 implements MRestFilter {
        @Override
        public void doFilter(MRestRequest restRequest, MRestResponse restResponse, MRestFilterChain filterChain) {
            logger.info("filter1 first(order=-123) -->> " + restRequest.getUrl());
            filterChain.doFilter(restRequest, restResponse);
        }
    }

    private static class Vo {
        String username;

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        @Override
        public String toString() {
            return "Vo{" +
                    "username='" + username + '\'' +
                    '}';
        }
    }

}
