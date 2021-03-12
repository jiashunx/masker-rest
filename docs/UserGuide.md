
- 启动server样例

```text
# 未指定默认context-path为"/"，默认端口为8080，默认serverName为mrest-server.
new MRestServer()
    .listenPort(21700)
    .serverName("demo-21700")
    .connectionKeepAlive(false)
    .workerThreadNum(NettyRuntime.availableProcessors() * 2)
    .bossThreadNum(1)
    .start();

# 自行指定context(同一server可指定多个context-path，也可对不同context-path指定不同的url映射处理)
new MRestServer()
        .context("/demo")
    .getRestServer()
        .context("/demo2")
    .getRestServer()
    .start();

# 可指定默认的classpath静态资源扫描目录(优先级从高到底："META-INF/resources/", "resources/", "static/", "public/")
# 可指定自定义的classpath静态资源扫描目录(支持多个)
# 可指定自定义的磁盘文件扫描目录(支持多个)
new MRestServer()
        // 默认context：context-path为"/"
        .context()
        .addDefaultClasspathResource()
        .addClasspathResource("re/01/")
        .addClasspathResource(new String[] { "re/02/", "re/03/" })
        .addDiskResource("/root/html/")
        .addDiskResource(new String[] { "/root/html/", "/root/html1/"})
    .getRestServer()
    .start();
```

- get请求样例

```text
restServer
    .context("/demo")
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
    .getRestServer()
    .start();
```

- post请求样例

```text
restServer
    .context("/demo")
    .post(("/post-form"), request -> {
        logger.info("post, form data: {}", request.parseBodyToObj(Vo.class));
        return new HashMap<String, Object>();
    })
    .getRestServer()
    .start();

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
```

- forward请求样例

```text
restServer
    .context("/demo")
    .post("/post-forward", (request, response) -> {
        logger.info("post forward, forward to /post-forward-target");
        request.setAttribute("hk-01", "hk-01-value");
        response.forward("/post-forward-target", request);
    })
    .post("/post-forward-target", (request, response) -> {
        logger.info("/post forward target, receive attribute: hk-01={}", request.getAttribute("hk-01"));
        response.write(HttpResponseStatus.OK);
    })
    .getRestServer()
    .start();
```

- redirect请求样例

```text
restServer
    .context("/demo")
    .get("/get-redirect.html", (request, response) -> {
        logger.info("get redirect, redirect to /get-redirect-target.html");
        response.redirect("/get-redirect-target.html");
    })
    .get("/get-redirect-target.html", (request) -> {
        return "<html><body>this is an redirected html page !</body></html>";
    }, MRestHeaderBuilder.Build("Content-Type", "text/html"))
    .getRestServer()
    .start();
```

- filter样例

```text
restServer
    .context("/demo")
    .filter("/filter-test/*", new Filter0(), new Filter1())
    .get("/filter-test/get0", request -> {
        // do nothing.
        logger.info("get, /filter-test/get0");
    })
    .getRestServer()
    .start();


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
```

- servlet样例
```text
restServer
    .context("/demo")
    .servlet("/servlet/t", (request, response) -> {
        response.writeString("/servlet/t -> =_=");
    })
    .servlet("/servlet/*", (request, response) -> {
        if ("/servlet/test".equals(request.getUrl())) {
            return;
        }
        response.writeString("/servlet/* -> =_=");
    })
    .servlet("/servlet/test", (request, response) -> {
        response.writeString("/servlet/test -> =_=");
    })
    .servlet("/servlet/m/{abc}", (request, response) -> {
        response.writeString("/servlet/m/{abc}");
    })
    .servlet("/servlet/m/xxx", (request, response) -> {
        response.writeString("/servlet/m/sss");
    })
    .servlet("/servlet/n/{xx}", (request, response) -> {
        response.writeString("/servlet/n/{xxx}");
    })
    .servlet(new Servlet0(), new Servlet1())
    .getRestServer()
    .start();

@RequestMapping(url = "/servlet0")
public static class Servlet0 extends AbstractRestServlet {
    @RequestMapping(url = "/method0")
    public void method0() {
        System.out.println("method0");
    }
    @GetMapping(url = "/method1")
    public void method1(MRestRequest request) {
        System.out.println("method1");
    }
    @PostMapping(url = "/method2")
    public void method2(MRestRequest request, MRestResponse response) {
        System.out.println("method2");
    }
}

public static class Servlet1 extends AbstractRestServlet {
    @GetMapping(url = "/servlet1/method0")
    public void method() {
        System.out.println("Servlet1.method");
    }
}

@RequestMapping(url = "/servlet2")
public static class Servlet2 extends AbstractRestServlet {
    @GetMapping(url = "/0/{m}/{n}")
    public void method0(MRestRequest request) {
        System.out.println("Servlet2.method0, m=" + request.getPathVariable("m") + ", n=" + request.getPathVariable("n"));
    }
    @GetMapping(url = "/1/{m}")
    public void method1(MRestRequest request, MRestResponse response) {
        System.out.println("Servlet2.method0, m=" + request.getPathVariable("m"));
    }
}
```

- cookie样例

```text
restServer
    .context("/demo")
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
    .getRestServer()
    .start();
```

- jwt会话过滤样例

```text
// /jwt/login请求body: {"username": "admin"} 进行会话登录验证
// /jwt/xxx获取资源
restServer
    .context("/demo")
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
    .getRestServer()
    .start();
```

- 文件上传

```text
<body>
    <div>
        <label>文件上传测试（基于FormData，单选）</label>
        <br>
        <input type="file" id="file-upload0" name="avatar">
        <button type="button" id="btn-file-upload0">保存</button>
    </div>
    <div>
        <label>文件上传测试（基于form表单，多选）</label>
        <br>
        <form id="file-upload-form1">
            <input type="file" id="file" name="file-upload1" multiple="multiple">
        </form>
        <button type="button" id="btn-file-upload1">保存</button>

    </div>
    <script type="text/javascript" src="./webjars/webjar-jquery/3.5.1/dist/jquery.min.js"></script>
    <script>
        $("#btn-file-upload0").click(function () {
            var files = $('#file-upload0').prop('files');
            var formData = new FormData();
            formData.append('avatar', files[0]);
            $.ajax({
                url: './fileupload/test0',
                type: 'POST',
                data: formData,
                cache: false,
                processData: false,
                contentType: false
            });
        });
        $("#btn-file-upload1").click(function () {
            var formData = new FormData($("#file-upload-form1")[0]);
            $.ajax({
                url: './fileupload/test1',
                type: 'POST',
                data: formData,
                cache: false,
                processData: false,
                contentType: false,
                success: function (data) {
                    console.log("upload files success.", data);
                }
            });
        });
    </script>
</body>
```

```text
restServer
    .context("/demo")
    .fileupload("/fileupload/test0", (request, response) -> {
        MRestFileUploadRequest fileUploadRequest = (MRestFileUploadRequest) request;
        MRestFileUpload fileUpload = fileUploadRequest.getFileUploadOnlyOne();
        logger.info("[upload one] upload file: {}", fileUpload.getFilePath());
        String newFilePath = MRestUtils.getUserDirPath() + "logs/" + fileUpload.getFilename();
        File newFile = new File(newFilePath);
        try {
            fileUpload.copyFile(newFile);
            logger.info("[upload one] copy file to path: {}", newFilePath);
        } catch (Throwable throwable) {
            logger.error("[upload one] copy file to path {} failed.", newFilePath, throwable);
        }
    })
    .fileupload("/fileupload/test1", (request, response) -> {
        MRestFileUploadRequest fileUploadRequest = (MRestFileUploadRequest) request;
        List<MRestFileUpload> fileUploadList = fileUploadRequest.getFileUploadList();
        List<String> fileNames = new ArrayList<>();
        for (MRestFileUpload fileUpload: fileUploadList) {
            logger.info("[upload more than one] upload file: {}", fileUpload.getFilePath());
            fileNames.add(fileUpload.getFilename());
        }
        return fileNames;
    })
    .getRestServer()
    .start();
```

- 文件下载

```text
restServer
    .context("/demo")
    .filedownload("/filedownload/test0", (request, response) -> {
        String filePath = MRestUtils.getUserDirPath() + "README.md";
        response.write(new File(filePath));
        // 也可使用jquery+form表单提交post请求来实现文件下载.
    })
    .get("/filedownload/test1", (request, response) -> {
        String filePath = MRestUtils.getUserDirPath() + "README.md";
        response.write(new File(filePath));
    })
    .getRestServer()
    .start();
```

- 启动websocket监听处理

```text
if (window.WebSocket) {
    let _socket = new WebSocket("ws://localhost:21700/demo000");
    _socket.onmessage = function (ev) {
        alert("receive from server: " + ev.data);
    };
    _socket.onopen = function (ev) {
        console.log("connected to server");
    };
    _socket.onclose = function (ev) {
        console.log("disconnected from server");
    };
    $("#btn-websocket-test").click(function () {
        if (_socket.readyState === WebSocket.OPEN) {
            _socket.send("hahahhahahhhhhh");
        } else {
            alert("WebSocket Connection is not ready.");
        }
    });
}

restServer
    .context("/demo")
    .websocketContext("/demo000")
    .bindTextFrameHandler((frame, request, response) -> {
        ChannelId channelId = response.getChannelHandlerContext().channel().id();
        logger.info("receive from client: {}, text: {}", channelId, frame.text());
        response.writeAndFlush(new TextWebSocketFrame("hello."));
    })
    .channelActiveCallback((ChannelHandlerContext ctx, MWebsocketRequest request) -> {
        logger.info("client active: {}", ctx.channel().id().toString());
    })
    .channelInactiveCallback((ChannelHandlerContext ctx) -> {
        logger.info("client inactive: {}", ctx.channel().id().toString());
    })
    .getRestServer()
    .start();
```

- 基于websocket实现聊天室demo

   - 前端实现

      - html
      
      ```text
      <div>
          <label>用户A</label>
          <input id="usernameA" value="用户A" style="width: 100px;" />
          <textarea id="textareaA" style="width: 200px; height: 200px;"></textarea>
          <input id="inputA" style="width: 100px;">
          <button id="submitA" type="button">用户A提交</button>
      </div>
      <div>
          <label>用户B</label>
          <input id="usernameB" value="用户B" style="width: 100px;" />
          <textarea id="textareaB" style="width: 200px; height: 200px;"></textarea>
          <input id="inputB" style="width: 100px;">
          <button id="submitB" type="button">用户B提交</button>
      </div>
      ```

      - javascript

      ```text
      var WebsocketChatRoom = function (username, textarea, input, button) {
          this.$username = $("#" + username);
          this.$textarea = $("#" + textarea);
          this.$input = $("#" + input);
          this.$button = $("#" + button);
          if (!window.WebSocket) {
              throw new Error("browser do not support websocket");
          }
          this.websocktURL = "ws://" + window.location.host + "/demo/chatroom";
          this.$websocket = new window.WebSocket(this.websocktURL);
      }
      WebsocketChatRoom.prototype = {
          bindEvent: function () {
              let _this = this;
              _this.$websocket.onopen = function (ev) {
                  console.log(_this.$username.val(), " connectted to websocket server, url: ", _this.websocktURL);
              };
              _this.$websocket.onclose = function (ev) {
                  console.log(this.$username.val(), " disconnected from websocekt server, url: ", _this.websocktURL);
              };
              _this.$websocket.onmessage = function (ev) {
                  let msg = JSON.parse(ev.data);
                  _this.$textarea.val(_this.$textarea.val() + "\n" + msg.username + ": " + msg.text);
              };
              _this.$button.click(function () {
                  let text = JSON.stringify({
                      username: _this.$username.val(),
                      text: _this.$input.val()
                  });
                  _this.$websocket.send(text);
              });
          },
      };
      new WebsocketChatRoom("usernameA", "textareaA", "inputA", "submitA").bindEvent();
      new WebsocketChatRoom("usernameB", "textareaB", "inputB", "submitB").bindEvent();
      ```

      - java
      
      ```text
      restServer
      .websocketContext("/chatroom")
      .bindTextFrameHandler((frame, request, response) -> {
          String channelId = response.getChannelId();
          String text = frame.text();
          logger.info("WebsocketContext[{}] receive from client: {}, text: {}", request.getContextPath(), channelId, text);
          CHATROOM_CHANNEL_MAP.forEach((key, value) -> {
              value.writeAndFlush(new TextWebSocketFrame(text));
          });
      })
      .channelActiveCallback((ChannelHandlerContext ctx, MWebsocketRequest request) -> {
          String channelId = ctx.channel().id().toString();
          logger.info("WebsocketContext[{}] client active: {}", request.getContextPath(), channelId);
          CHATROOM_CHANNEL_MAP.put(channelId, ctx.channel());
      })
      .channelInactiveCallback((MWebsocketRequest request, MWebsocketResponse response) -> {
          String channelId = response.getChannelId();
          logger.info("WebsocketContext[{}] client inactive: {}", request.getContextPath(), channelId);
          CHATROOM_CHANNEL_MAP.remove(channelId);
      })
      .getRestServer()
      .start();
      ```
