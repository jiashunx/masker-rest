package io.github.jiashunx.masker.rest.demo;

import com.alibaba.fastjson.JSON;
import com.google.gson.Gson;
import io.github.jiashunx.masker.rest.framework.MRestRequest;
import io.github.jiashunx.masker.rest.framework.MRestResponse;
import io.github.jiashunx.masker.rest.framework.MRestServer;
import io.github.jiashunx.masker.rest.framework.exception.MRestJWTException;
import io.github.jiashunx.masker.rest.framework.filter.Filter;
import io.github.jiashunx.masker.rest.framework.filter.MRestFilter;
import io.github.jiashunx.masker.rest.framework.filter.MRestFilterChain;
import io.github.jiashunx.masker.rest.framework.util.MRestHeaderBuilder;
import io.github.jiashunx.masker.rest.framework.util.MRestJWTHelper;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.util.NettyRuntime;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;

/**
 * @author jiashunx
 */
public class MaskerRestMain {

    private static final Logger logger = LoggerFactory.getLogger(MaskerRestMain.class);

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

    @Filter(order = 123)
    private static class Filter0 implements MRestFilter {
        @Override
        public void doFilter(MRestRequest restRequest, MRestResponse restResponse, MRestFilterChain filterChain) {
            logger.info("filter0 -->> " + restRequest.getUrl());
            filterChain.doFilter(restRequest, restResponse);
        }
    }

    @Filter(order = -123)
    private static class Filter1 implements MRestFilter {
        @Override
        public void doFilter(MRestRequest restRequest, MRestResponse restResponse, MRestFilterChain filterChain) {
            logger.info("filter1 -->> " + restRequest.getUrl());
            filterChain.doFilter(restRequest, restResponse);
        }
    }

    private static void startNewServer(int port, String serverName) {
        MRestJWTHelper jwtHelper = new MRestJWTHelper("qwerasdfzxcv09876543231");
        new MRestServer(port, serverName)
                .listenPort(port).serverName(serverName)
                .bossThreadNum(1).workerThreadNum(NettyRuntime.availableProcessors() * 2)
                .filter("/*", (restRequest, restResponse, filterChain) -> {
                    String requestURL = restRequest.getUrl();
                    if ("/login".equals(requestURL)) {
                        Vo vo = restRequest.parseBodyToObj(Vo.class);
                        if (vo.username.equals("admin")) {
                            String jwtToken = jwtHelper.newToken();
                            restResponse.write(HttpResponseStatus.OK, MRestHeaderBuilder.Build("Authorization", jwtToken));
                        } else {
                            restResponse.write(HttpResponseStatus.UNAUTHORIZED);
                        }
                    } else {
                        String jwtToken = restRequest.getHeader("Authorization");
                        if (StringUtils.isBlank(jwtToken)) {
                            restResponse.write(HttpResponseStatus.UNAUTHORIZED);
                            return;
                        }
                        try {
                            if (!jwtHelper.isTokenTimeout(jwtToken) && jwtHelper.isTokenValid(jwtToken)) {
                                filterChain.doFilter(restRequest, restResponse);
                                restResponse.setHeader("Authroization", jwtHelper.updateToken(jwtToken));
                                return;
                            }
                        } catch (MRestJWTException e) {
                            logger.error("", e);
                        }
                        restResponse.write(HttpResponseStatus.UNAUTHORIZED);
                    }
                })

                .get("/get0", request -> {
                    logger.info("get0 -->> username=" + request.getParameter("username"));
                })
                .get("/get1", (request, response) -> {
                    logger.info("get1 -->> body=" + request.bodyToString());
                    response.write(HttpResponseStatus.OK);
                })
                .post("/post0", request -> {
                    logger.info("post0 -->> body=" + request.bodyToString());
                })
                .post("/post1", (request, response) -> {
                    logger.info("post1 -->> body=" + request.bodyToString());
                    logger.info("post1 -->> body=" + request.parseBodyToObj(Vo.class));
                    response.write(HttpResponseStatus.OK);
                })
                .post("/post2", (request, response) -> {
                    logger.info("post2(redirect) -->> redirect to /post1");
                    logger.info("post2(redirect) -->> attribute->hell0: " + request.getAttribute("hello"));
                    response.redirect("/post1");
                })
                .post("/post3", (request, response) -> {
                    logger.info("post3(forward) -->> forward to /post2");
                    request.setAttribute("hello", "I'm fine, thank you, and you?");
                    response.forward("/post2", request);
                })
                .filter("/*", (restRequest, restResponse, filterChain) -> {
                    logger.info("filter* -->> " + restRequest.getUrl());
                    filterChain.doFilter(restRequest, restResponse);
                })
                .post("/post-data", request -> {
                    logger.info("post-data -->> " + request.parseBodyToObj(Vo.class));
                    return new HashMap<>();
                })
                .get("/get-html", request -> {
                    logger.info("get-html -->> ");
                    return "<html><body>this is a html page !</body></html>";
                }, MRestHeaderBuilder.Build("Content-Type", "text/html"))
                .get("/get-text", request -> {
                    logger.info("get-text -->> ");
                    return "text.......";
                }, MRestHeaderBuilder.Build("Content-Type", "text/plain"))
                .filter("/post*", new Filter0(), new Filter1())
                .start();
    }

    public static void main(String[] args) {
        startNewServer(21700, "mrest-demo");
        startNewServer(21701, "mrest-demo1");
        startNewServer(21702, "mrest-demo2");
        startNewServer(21703, "mrest-demo3");
        logger.info(JSON.toJSONString("asldfjl"));
        logger.info(new Gson().toJson("asldfjl"));
        logger.info(JSON.toJSONString(new Vo()));
        logger.info(new Gson().toJson(new Vo()));
    }

}
