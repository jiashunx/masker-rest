package io.github.jiashunx.masker.rest.demo;

//import org.springframework.boot.SpringApplication;
//import org.springframework.boot.autoconfigure.SpringBootApplication;

import io.github.jiashunx.masker.rest.framework.MRestServer;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author jiashunx
 */
//@SpringBootApplication
public class MaskerRestMain {

    private static final Logger logger = LoggerFactory.getLogger(MaskerRestMain.class);

    public static void main(String[] args) {
//        SpringApplication.run(MaskerRestMain.class, args);
        new MRestServer()
                .mapping("/post0", (request, response) -> {
                    System.out.println(request.bodyToString());
                    response.write(HttpResponseStatus.OK);
                }, HttpMethod.POST)
                .mapping("/post1", (request, response) -> {
                    System.out.println(request.bodyToString());
                    response.write(HttpResponseStatus.OK);
                }, HttpMethod.POST)
                .mapping("/get", (request, response) -> {
                    System.out.println(request.bodyToString());
                    System.out.println("receive username: " + request.getParameter("username"));
                    System.out.println("receive password: " + request.getParameter("password"));
                    response.write(HttpResponseStatus.OK);
                }, HttpMethod.GET)
                .filter("/*", (restRequest, restResponse, filterChain) -> {
                    if (logger.isInfoEnabled()) {
                        logger.info("test filter, receive request, url: {}", restRequest.getUrl());
                    }
                    filterChain.doFilter(restRequest, restResponse);
                })

                .start();
    }

}
