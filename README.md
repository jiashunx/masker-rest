
### masker-rest

- 项目简介：基于Netty实现Http Server，极简API发布Rest服务

- 工程介绍：

   - masker-rest-framework：http server实现framework包，引入到工程后即可使用相应API创建http server
   - masker-rest-demo：基于masker-rest-framework实现的http server demo

- 环境依赖：

   - JDK8+

- 功能列表：

    <table>
        <tr>
            <th>功能名称</th>
            <th>是否完成</th>
        </tr>
        <tr>
            <td>rest请求处理</td>
            <td>DONE</td>
        </tr>
        <tr>
            <td>过滤器实现</td>
            <td>DONE</td>
        </tr>
        <tr>
            <td>重定向、转发</td>
            <td>DONE</td>
        </tr>
        <tr>
            <td>默认JWT实现&会话控制</td>
            <td>DONE</td>
        </tr>
        <tr>
            <td>默认静态资源处理</td>
            <td>TODO</td>
        </tr>
        <tr>
            <td>支持文件的上传下载</td>
            <td>TODO</td>
        </tr>
    </table>

- 部署使用：

   - 1、引入masker-rest-framework依赖

   ```text
   <dependency>
     <groupId>io.github.jiashunx</groupId>
     <artifactId>masker-rest-framework</artifactId>
     <version>1.1.1</version>
   </dependency>
   ```

   - 2、根据api创建http server并启动

   ```text
   new MRestServer(21700, "mrest-demo")
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
