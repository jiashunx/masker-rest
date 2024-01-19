
- [0、masker-rest框架介绍](#0)
    - [0.1、框架介绍](#0.1)
    - [0.1、版本清单](#0.2)
    - [0.3、框架引用](#0.3)
    - [0.4、框架样例](#0.4)
- [1、masker-rest发布Http服务](#1)
    - [1.1、发布Rest服务](#1.1)
        - [1.1.1、Get请求处理](#1.1.1)
        - [1.1.2、Post请求处理](#1.1.2)
        - [1.1.3、自定义方法的Rest请求](#1.1.3)
        - [1.1.4、客户端重定向(Redirect)](#1.1.4)
        - [1.1.5、服务端转发(Forward)](#1.1.5)
        - [1.1.6、Filter](#1.1.6)
        - [1.1.7、Servlet](#1.1.7)
        - [1.1.8、Cookie](#1.1.8)
        - [1.1.9、JWT](#1.1.9)
        - [1.1.10、文件上传](#1.1.10)
        - [1.1.11、文件下载](#1.1.11)
    - [1.2、发布静态资源服务](#1.2)
        - [1.2.1、静态资源绑定](#1.2.1)
        - [1.2.2、classpath静态资源](#1.2.2)
        - [1.2.3、磁盘静态资源](#1.2.3)
- [2、masker-rest发布WebSocket服务](#2)
    - [2.1、发布WebSocket服务](#2.1)
    - [2.2、WebSocket实现简易聊天室](#2.2)
<br>



<h3 id="0">0、masker-rest框架介绍</h3>

<h4 id="0.1">0.1、框架介绍</h4>

<i>masker-rest</i>是基于Netty实现的一款Java框架，非常简单的几行代码就可发布Http（Rest、静态资源）服务及WebSocket服务，框架依赖包为Netty、Jackson、ASM，构建的fatjar体积7.xMB

<h4 id="0.2">0.2、版本清单</h4>

参见: [Version.md][0]

<h4 id="0.3">0.3、框架引用</h4>

```text
   <dependency>
     <groupId>io.github.jiashunx</groupId>
     <artifactId>masker-rest-framework</artifactId>
     <version>${lastest.version}</version>
   </dependency>
   ```

<h4 id="0.4">0.4、框架样例</h4>

```java
public class Test {
    private static final Logger logger = LoggerFactory.getLogger(Test.class);
    public static void main(String[] args) {
        // 默认端口8080
        new MRestServer()
            .listenPort(8081)
            .serverName("demo")
            .connectionKeepAlive(false)
            .workerThreadNum(NettyRuntime.availableProcessors() * 2)
            .bossThreadNum(1)
            .callbackAfterStartup(() -> {
                System.out.println("Server启动成功后的回调");
            })
            // 默认context：context-path为"/"
            .context()
                // 设置自定义的序列化处理对象
                .setObjectMapperSupplier(() -> {
                    ObjectMapper objectMapper = new ObjectMapper();
                    objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                    return objectMapper;
                })
                // 允许静态资源缓存
                .setStaticResourcesCacheEnabled(true)
                // 缓存静态资源定时刷新
                .setAutoRefreshStaticResources(true)
                // 设置默认首页
                .setIndexUrl("/index.html")
                // "/"扫描classpath: "META-INF/resources/", "resources/", "static/", "public/"
                .addDefaultClasspathResource("/")
                .addClasspathResource("/html/dist", "dist/")
                .addDiskpathResource("/html/dist", "/root/html/")
                .get("/get-html", request -> {
                    return "<html><body>this is a html page !</body></html>";
                }, MRestHeaderBuilder.Build("Content-Type", "text/html"))
                .post(("/post-form"), request -> {
                    logger.info("post, form data: {}", request.parseBodyToObj(Vo.class));
                    return new HashMap<String, Object>();
                })
                .servlet("/servlet/{name}", (request, response) -> {
                    response.writeString("/servlet/{name}" + request.getPathVariable("name"));
                })
            .getRestServer()
                .context("/demo")
                // websocket地址: /demo/test-websocket
                .websocketContext("/test-websocket")
                .bindTextFrameHandler((frame, request, response) -> {
                    String channelId = response.getChannelId();
                    System.out.println("receive from client: " + channelId + ", text: " + frame.text());
                    response.writeAndFlush(new TextWebSocketFrame("hello."));
                })
                .channelActiveCallback((ChannelHandlerContext ctx, MWebsocketRequest request) -> {
                    System.out.println("client active: " + ctx.channel().id().toString());
                })
                .channelInactiveCallback((request, response) -> {
                    System.out.println("client inactive: " + response.getChannelId());
                })
            .getRestServer()
            .start();
    }
}
```

<h3 id="1">1、masker-rest发布Http服务</h3>

<h4 id="1.1">1.1、发布Rest服务</h4>

<h5 id="1.1.1">1.1.1、Get请求处理</h5>

```java
public class Test {
    private static final Logger logger = LoggerFactory.getLogger(Test.class);
    public static void main(String[] args) {
        new MRestServer(10000)
            .context("/demo")
                .get("/get-NoParam-NoRet", () -> {
                    MRestServerThreadModel threadModel = SharedObjects.getServerThreadModel();
                    MRestRequest request = threadModel.getRestRequest();
                    System.out.println("get, origin-url: " + request.getOriginUrl());
                })
                .get("/get-ParamReq-NoRet", request -> {
                    System.out.println("get, param-req, no ret");
                })
                .get("/get-html", request -> {
                    System.out.println("get, param-req, return html");
                    return "<html><body>this is a html page !</body></html>";
                }, MRestHeaderBuilder.Build("Content-Type", "text/html"))
                .get("/get-text", request -> {
                    System.out.println("get, param-req, return text");
                    return "text.......";
                }, MRestHeaderBuilder.Build("Content-Type", "text/plain"))
                .get("/get-ParamReqResp-NoRet", (request, response) -> {
                    System.out.println("get, param-req-resp, no ret");
                })
            .getRestServer()
            .start();
    }
}
```

<h5 id="1.1.2">1.1.2、Post请求处理</h5>

```java
public class Test {
    private static final Logger logger = LoggerFactory.getLogger(Test.class);
    public static void main(String[] args) {
        new MRestServer(10001)
            .context("/demo")
                .post(("/post-form"), request -> {
                    System.out.println("post, form data: " + request.parseBodyToObj(Vo.class));
                    return new HashMap<String, Object>();
                })
            .getRestServer()
            .start();
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
            return "Vo{" + "username='" + username + '\'' + '}';
        }
    }
}
```

<h5 id="1.1.3">1.1.3、自定义方法的Rest请求</h5>

```java
public class Test {
    private static final Logger logger = LoggerFactory.getLogger(Test.class);
    public static void main(String[] args) {
        new MRestServer(10002)
            .context("/demo")
                .mapping("/put", (request, response) -> {
                    response.write(HttpResponseStatus.OK);
                }, HttpMethod.PUT)
                .mapping("/put_delete", (request, response) -> {
                    response.write(new HashMap<>());
                }, HttpMethod.PUT, HttpMethod.DELETE)
                .mapping("/空格测试 1 2", (request, response) -> {
                    response.write("空格测试成功");
                }, HttpMethod.GET)
                .filter("/*", (request, response, filterChain) -> {
                    System.out.println("拦截到url: " + request.getUrl());
                    filterChain.doFilter(request, response);
                })
            .getRestServer()
            .start();
    }
}
```

<h5 id="1.1.4">1.1.4、客户端重定向(Redirect)</h5>

```java
public class Test {
    private static final Logger logger = LoggerFactory.getLogger(Test.class);
    public static void main(String[] args) {
        new MRestServer(10003)
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
    }
}
```

<h5 id="1.1.5">1.1.5、服务端转发(Forward)</h5>

```java
public class Test {
    private static final Logger logger = LoggerFactory.getLogger(Test.class);
    public static void main(String[] args) {
        new MRestServer(10004)
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
    }
}
```

<h5 id="1.1.6">1.1.6、Filter</h5>

```java
public class Test {
    private static final Logger logger = LoggerFactory.getLogger(Test.class);
    public static void main(String[] args) {
        new MRestServer(10005)
            .context("/demo")
                // 过滤器样例(过滤器执行顺序按照order值从小到大顺序执行)
                .filter("/filter-test/*", new Filter0(), new Filter1())
                .get("/filter-test/get0", request -> {
                    // do nothing.
                    logger.info("get, /filter-test/get0");
                })
            .getRestServer()
            .start();
    }
    @MFilter(order = 123)
    private static class Filter0 implements MRestFilter {
        @Override
        public void doFilter(MRestRequest restRequest, MRestResponse restResponse, MRestFilterChain filterChain) {
            logger.info("filter0 second(order=123) -->> " + restRequest.getUrl());
            filterChain.doFilter(restRequest, restResponse);
        }
    }
    @MFilter(order = -123)
    private static class Filter1 implements MRestFilter {
        @Override
        public void doFilter(MRestRequest restRequest, MRestResponse restResponse, MRestFilterChain filterChain) {
            logger.info("filter1 first(order=-123) -->> " + restRequest.getUrl());
            filterChain.doFilter(restRequest, restResponse);
        }
    }
}
```

<h5 id="1.1.7">1.1.7、Servlet</h5>

```java
public class Test {
    private static final Logger logger = LoggerFactory.getLogger(Test.class);
    public static void main(String[] args) {
        new MRestServer(10006)
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
                .servlet("/servlet/m/{abc}", (request, response) -> {
                    response.writeString("/servlet/m/{abc}");
                })
                /**
                 * Exception in thread "main" io.github.jiashunx.masker.rest.framework.exception.MRestServerInitializeException: Server[demo:21700] start failed
                 * 	at io.github.jiashunx.masker.rest.framework.MRestServer.start(MRestServer.java:276)
                 * 	at io.github.jiashunx.masker.rest.demo.MaskerRestMain.main(MaskerRestMain.java:294)
                 * Caused by: io.github.jiashunx.masker.rest.framework.exception.MRestMappingException: Server[demo:21700] Context[/demo] mapping servlet conflict, urlPattern: /servlet/m/{abcd}
                 * 	at io.github.jiashunx.masker.rest.framework.MRestContext.lambda$servlet$6(MRestContext.java:497)
                 * 	at io.github.jiashunx.masker.rest.framework.MRestContext.init(MRestContext.java:70)
                 * 	at io.github.jiashunx.masker.rest.framework.MRestServer.lambda$start$1(MRestServer.java:240)
                 * 	at java.util.concurrent.ConcurrentHashMap.forEach(ConcurrentHashMap.java:1597)
                 * 	at io.github.jiashunx.masker.rest.framework.MRestServer.start(MRestServer.java:239)
                 * 	... 1 more
                 */
                /*.servlet("/servlet/m/{abcd}", (request, response) -> {
                    response.writeString("/servlet/m/{abcd}");
                })*/
                .servlet("/servlet/m/xxx", (request, response) -> {
                    response.writeString("/servlet/m/sss");
                })
                .servlet("/servlet/n/{xx}", (request, response) -> {
                    response.writeString("/servlet/n/{xxx}");
                })
                .servlet("/servlet/test", (request, response) -> {
                    response.writeString("/servlet/test -> =_=");
                })
                .servlet(new Servlet0(), new Servlet1(), new Servlet2())
            .getRestServer()
            .start();
    }
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
}
```

<h5 id="1.1.8">1.1.8、Cookie</h5>

```java
public class Test {
    private static final Logger logger = LoggerFactory.getLogger(Test.class);
    public static void main(String[] args) {
        new MRestServer(10007)
            .context("/demo")
                .get("/cookie/set-cookie", (request, response) -> {
                    String cookieVal = "xxxxxxxxxxxxxxxxxx";
                    System.out.println("set-cookie, hello=" + cookieVal);
                    response.setCookie("hello", cookieVal);
                    Cookie nCookie = new DefaultCookie("hello0", "hhhhhh");
                    response.setCookie(nCookie);
                })
                .get("/cookie/get-cookie", request -> {
                    System.out.println("get-cookie, map: " + request.getCookieMap());
                    System.out.println("get-cookie, list: " + request.getCookies());
                })
            .getRestServer()
            .start();
    }
}
```

<h5 id="1.1.9">1.1.9、JWT</h5>

```java
public class Test {
    private static final Logger logger = LoggerFactory.getLogger(Test.class);
    public static void main(String[] args) {
        MRestJWTHelper jwtHelper = new MRestJWTHelper("kkkdfjdkfjkdjf", 10*60);
        // /jwt/login请求body: {"username": "admin"} 进行会话登录验证
        // /jwt/xxx获取资源
        new MRestServer(10008)
            .context("/demo")
                // jwt会话过滤样例
                // /jwt/login请求body: {"username": "admin"} 进行会话登录验证
                // /jwt/xxx获取资源
                .post("/jwt/xxx", request -> {})
                .filter("/jwt/*", (request, response, filterChain) -> {
                    String requestURL = request.getUrl();
                    if ("/jwt/login".equals(requestURL) && request.getMethod() == HttpMethod.POST) {
                        Vo vo = request.parseBodyToObj(Vo.class);
                        if (vo.username.equals("admin")) {
                            String jwtToken = jwtHelper.newToken();
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
                        if (StringUtils.isEmpty(jwtToken)) {
                            response.write(HttpResponseStatus.UNAUTHORIZED);
                            return;
                        }
                        try {
                            if (!jwtHelper.isTokenTimeout(jwtToken) && jwtHelper.isTokenValid(jwtToken)) {
                                filterChain.doFilter(request, response);
                                String newToken = jwtHelper.updateToken(jwtToken);
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
            return "Vo{" + "username='" + username + '\'' + '}';
        }
    }
}
```

<h5 id="1.1.10">1.1.10、文件上传</h5>

- 新建upload.html文件放至工程资源目录下的static/目录下

```html
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>文件上传测试</title>
</head>
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
<script type="application/javascript" src="./masker-rest/static/lib/jquery.min.js"></script>
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
            contentType: false,
            success: function (data) {
                console.log("upload files success.", data);
                alert("upload file success.");
            }
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
                alert("upload file success.");
            }
        });
    });
</script>
</body>
</html>
```

- 后端代码

```java
public class Test {
    private static final Logger logger = LoggerFactory.getLogger(Test.class);
    public static void main(String[] args) {
        // 配套前端: static/upload.html
        new MRestServer(10009)
            .context("/demo")
                // "/"扫描classpath: "META-INF/resources/", "resources/", "static/", "public/"
                .addDefaultClasspathResource()
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
                        logger.error("[upload one] copy file to path {} failed", newFilePath, throwable);
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
                    // 临时文件未拷贝会被删除
                    return fileNames;
                })
            .getRestServer()
            .start();
    }
}
```

- 访问 http://127.0.0.1:10009/demo/upload.html 进行文件上传测试

<h5 id="1.1.11">1.1.11、文件下载</h5>

- 新建download.html文件放至工程资源目录下的static/目录下

```html
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>文件下载测试</title>
</head>
<body>
<div>
    <label>文件下载测试（下载项目README.md文件）</label>
    <br>
    <button type="button" id="btn-download-test0">下载</button>
</div>
<div>
    <label>文件下载测试（下载项目目录）</label>
    <br>
    <button type="button" id="btn-download-test1">下载</button>
</div>
<script type="application/javascript" src="./masker-rest/static/lib/jquery.min.js"></script>
<script>
    $("#btn-download-test0").click(function () {
        var _win = window.open("about:blank");
        _win.location.href = "./filedownload/test0";
    });
    $("#btn-download-test1").click(() => {
        window.open("about:blank").location.href = "./filedownload/test1";
    });
</script>
</body>
</html>
```

- 后端代码

```java
public class Test {
    private static final Logger logger = LoggerFactory.getLogger(Test.class);
    public static void main(String[] args) {
        // 配套前端: static/download.html
        new MRestServer(10010)
            .context("/demo")
                // "/"扫描classpath: "META-INF/resources/", "resources/", "static/", "public/"
                .addDefaultClasspathResource()
                .filedownload("/filedownload/test0", (request, response) -> {
                    String filePath = MRestUtils.getUserDirPath() + "README.md";
                    response.write(new File(filePath));
                    // 也可使用jquery+form表单提交post请求来实现文件下载.
                })
                .get("/filedownload/test1", (request, response) -> {
                    String targetFilePath = MRestUtils.getSystemTempDirPath() + "SFS" + File.separator + System.currentTimeMillis() + File.separator + System.nanoTime() + ".zip";
                    File targetFile = new File(targetFilePath);
                    FileUtils.zip(new File(MRestUtils.getUserDirPath()).listFiles(), targetFile);
                    response.write(targetFile, f -> {
                        try {
                            File parent = f.getParentFile();
                            f.delete();
                            parent.delete();
                        } catch (Throwable throwable) {
                            logger.error("delete tmp file failed: {}", f, throwable);
                        }
                    });
                })
            .getRestServer()
            .start();
    }
}
```

- 访问 http://127.0.0.1:10010/demo/download.html 进行文件下载测试

<h4 id="1.2">1.2、发布静态资源服务</h4>

<h5 id="1.2.1">1.2.1、静态资源绑定</h5>

masker-rest初始化时对静态资源（classpath静态资源及磁盘静态资源）进行绑定，运行时对绑定的静态资源进行扫描加载。

<h5 id="1.2.2">1.2.2、classpath静态资源</h5>

```java
public class Test {
    private static final Logger logger = LoggerFactory.getLogger(Test.class);
    public static void main(String[] args) {
        new MRestServer(10011)
            .context("/demo")
                // 不允许静态资源缓存
                .setStaticResourcesCacheEnabled(false)
                // "/"扫描classpath: "META-INF/resources/", "resources/", "static/", "public/"
                .addDefaultClasspathResource()
                // "/test"扫描classpath: "META-INF/resources/", "resources/", "static/", "public/"
                .addDefaultClasspathResource("/test")
                // "/"增加扫描classpath: "dist/"
                .addClasspathResource("dist/")
                // "/test"增加扫描: "dist/"
                .addClasspathResource("/test", "dist/")
            .getRestServer()
            .start();
    }
}
```

<h5 id="1.2.3">1.2.3、磁盘静态资源</h5>

```java
public class Test {
    private static final Logger logger = LoggerFactory.getLogger(Test.class);
    public static void main(String[] args) {
        new MRestServer(10012)
            .context("/demo")
                // 不允许静态资源缓存
                .setStaticResourcesCacheEnabled(false)
                // "/"扫描磁盘路径: "/home/html"
                .addDiskpathResource("/home/html")
                // "/test"扫描磁盘路径: "/home/html"
                .addDiskpathResource("/test", "/home/html")
            .getRestServer()
            .start();
    }
}
```



<h3 id="2">2、masker-rest发布WebSocket服务</h3>

<h4 id="2.1">2.1、发布WebSocket服务</h4>

- 新建websocket.html文件放至工程资源目录下的static/目录下

```html
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>websocket测试</title>
</head>
<body>
<div>
    <button type="button" id="btn-websocket-test">WebSocket测试</button>
</div>
<script type="application/javascript" src="./masker-rest/static/lib/jquery.min.js"></script>
<script>
    if (window.WebSocket) {
        let _socket = new WebSocket("ws://" + window.location.host + "/demo/websocket-test");
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
</script>
</body>
</html>
```

- 后端代码

```java
public class Test {
    private static final Logger logger = LoggerFactory.getLogger(Test.class);
    public static void main(String[] args) {
        // 配套前端: static/websocket.html
        new MRestServer(10013)
            .context("/demo")
                // "/"扫描classpath: "META-INF/resources/", "resources/", "static/", "public/"
                .addDefaultClasspathResource()
                // websocket服务地址: ws://127.0.0.1:10012/demo/websocket-test
                .websocketContext("/websocket-test")
                .bindTextFrameHandler((frame, request, response) -> {
                    String channelId = response.getChannelId();
                    logger.info("receive from client: {}, text: {}", channelId, frame.text());
                    response.writeAndFlush(new TextWebSocketFrame("hello."));
                })
                .channelActiveCallback((ChannelHandlerContext ctx, MWebsocketRequest request) -> {
                    logger.info("client active: {}", ctx.channel().id().toString());
                })
                .channelInactiveCallback((request, response) -> {
                    logger.info("client inactive: {}", response.getChannelId());
                })
            .getRestServer()
            .start();
    }
}
```

- 访问 http://127.0.0.1:10013/demo/websocket.html 进行websocket测试

<h4 id="2.2">2.2、WebSocket实现简易聊天室</h4>

- 新建chatroom.html文件放至工程资源目录下的static/目录下

```html
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>websocket实现聊天室</title>
</head>
<body>
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
<!-- 引入框架封装的websocket客户端javascript实现 -->
<script type="application/javascript" src="./masker-rest/static/websocket.js"></script>
<script>
    console.log(WebSocketClient);
</script>
<script type="application/javascript" src="./masker-rest/static/lib/jquery.min.js"></script>
<script>
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
                console.log(_this.$username.val(), " disconnected from websocekt server, url: ", _this.websocktURL);
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
</script>
</body>
</html>
```

- 后端代码

```java
public class Test {
    private static final Logger logger = LoggerFactory.getLogger(Test.class);
    public static void main(String[] args) {
        // 配套前端: static/chatroom.html
        Map<String, Channel> chatRoomChannelMap = new ConcurrentHashMap<>();
        new MRestServer(10014)
            .context("/demo")
                // "/"扫描classpath: "META-INF/resources/", "resources/", "static/", "public/"
                .addDefaultClasspathResource()
                // websocket服务地址: ws://127.0.0.1:10014/demo/chatroom
                .websocketContext("/chatroom")
                .bindTextFrameHandler((frame, request, response) -> {
                    String channelId = response.getChannelId();
                    String text = frame.text();
                    logger.info("WebsocketContext[{}] receive from client: {}, text: {}", request.getContextPath(), channelId, text);
                    chatRoomChannelMap.forEach((key, value) -> {
                        value.writeAndFlush(new TextWebSocketFrame(text));
                    });
                })
                .channelActiveCallback((ChannelHandlerContext ctx, MWebsocketRequest request) -> {
                    String channelId = ctx.channel().id().toString();
                    logger.info("WebsocketContext[{}] client active: {}", request.getContextPath(), channelId);
                    chatRoomChannelMap.put(channelId, ctx.channel());
                })
                .channelInactiveCallback((MWebsocketRequest request, MWebsocketResponse response) -> {
                    String channelId = response.getChannelId();
                    logger.info("WebsocketContext[{}] client inactive: {}", request.getContextPath(), channelId);
                    chatRoomChannelMap.remove(channelId);
                })
            .getRestServer()
            .start();
    }
}
```

- 访问 http://127.0.0.1:10014/demo/chatroom.html 进行测试


[0]: ../docs/Version.md


