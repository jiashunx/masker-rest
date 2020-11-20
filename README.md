
### masker-rest

- 项目简介：基于Netty实现Http Server，极简API发布Rest服务

- 工程介绍：

   - masker-rest-framework：http server实现framework包，引入到工程后即可使用相应API创建http server
   - masker-rest-demo：基于masker-rest-framework实现的http server demo

- 环境依赖：

   - JDK8+

- 版本清单：

   - 1.0.0版本
      - feature：支持发布rest接口
      - feature：支持重定向与转发
      - feature：支持设置响应头
   - 1.1.0版本
      - feature：添加默认JWT服务端实现
      - bugfix：设置响应体write操作仅可执行一次
   - 1.1.1版本
      - bugfix：修正启动多个rest server时url映射冲突的错误
   - 1.2.0版本
      - feature：请求及响应支持对cookie的处理
      - feature：server支持自定义netty的boss及worker线程数
      - feature：server支持自定义连接的keep-alive属性
      - refactor：url映射处理及filter映射处理的操作调整至server启动方式执行时执行
      - refactor：统一netty监听线程的名称
      - refactor：请求响应的header设置处理逻辑重构
      - refactor：对http请求的响应统一添加server框架名称及版本信息
   - 1.2.1版本
      - feature：server支持自定义context-path

   - TODO
      - 默认静态资源处理
      - 支持文件的上传下载

- 部署使用：

   - 1、引入masker-rest-framework依赖

   ```text
   <dependency>
     <groupId>io.github.jiashunx</groupId>
     <artifactId>masker-rest-framework</artifactId>
     <version>1.2.1</version>
   </dependency>
   ```

   - 2、根据api创建http server并启动

   ```text
   new MRestServer(21700, "mrest-demo")
       .listenPort(port).serverName(serverName)
       .bossThreadNum(1).workerThreadNum(NettyRuntime.availableProcessors() * 2)
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
   ```

   - 3、使用masker-rest-framework提供的jwt实现来实现会话控制

   ```text
   new MRestServer(21700, "mrest-demo")
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
               String jwtToken = restRequest.getHeaderToStr("Authorization");
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
   ```
