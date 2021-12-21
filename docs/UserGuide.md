
- [0、masker-rest框架介绍](#0)
    - [0.1、框架介绍](#0.1)
    - [0.2、框架引用](#0.2)
    - [0.3、框架样例](#0.3)
- [1、使用masker-rest发布Http服务](#1)
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
- [2、使用masker-rest发布WebSocket服务](#2)
    - [2.1、发布WebSocket服务](#2.1)
    - [2.2、WebSocket实现简易聊天室](#2.2)
- [3、masker-rest发布版本记录](#0.3)
    - [V1.0.0(released)](#V1.0.0)
    - [V1.1.0(released)](#V1.1.0)
    - [V1.1.1(released)](#V1.1.1)
    - [V1.2.0(released)](#V1.2.0)
    - [V1.2.1(released)](#V1.2.1)
    - [V1.3.0(released)](#V1.3.0)
    - [V1.4.0(released)](#V1.4.0)
    - [V1.4.1(released)](#V1.4.1)
    - [V1.4.2(released)](#V1.4.2)
    - [V1.4.3(released)](#V1.4.3)
    - [V1.4.4(released)](#V1.4.4)
    - [V1.4.5(released)](#V1.4.5)
    - [V1.4.6(released)](#V1.4.6)
    - [V1.4.7(released)](#V1.4.7)
    - [V1.4.8(released)](#V1.4.8)
    - [V1.4.9(released)](#V1.4.9)
    - [V1.4.10(released)](#V1.4.10)
    - [V1.5.0(released)](#V1.5.0)
    - [V1.6.0(released)](#V1.6.0)
    - [V1.6.1(released)](#V1.6.1)
    - [V1.6.2(released)](#V1.6.2)
    - [V1.6.3(released)](#V1.6.3)
    - [V1.6.4(released)](#V1.6.4)
    - [V1.6.5(released)](#V1.6.5)
    - [V1.6.6(released)](#V1.6.6)
    - [V1.6.7(released)](#V1.6.7)
    - [V1.6.8(released)](#V1.6.8)
    - [V1.7.0(doing)](#V1.7.0)

<br>

<h3 id="0">0、masker-rest框架介绍</h3>

<h4 id="0.1">0.1、框架介绍</h4>

<h4 id="0.2">0.2、框架引用</h4>

<h4 id="0.3">0.3、框架样例</h4>



<h3 id="1">1、使用masker-rest发布Http服务</h3>

<h4 id="1.1">1.1、发布Rest服务</h4>

<h4 id="1.2">1.2、发布静态资源服务</h4>



<h3 id="2">2、使用masker-rest发布WebSocket服务</h3>

<h4 id="2.1">2.1、发布WebSocket服务</h4>

<h4 id="2.2">2.2、WebSocket实现简易聊天室</h4>



<h3 id="3">3、masker-rest发布版本记录</h3>

<h4 id="V1.0.0">V1.0.0(released)</h4>

- feature：支持发布rest接口

- feature：支持filter

- feature：支持重定向与转发

- feature：支持设置响应头

<h4 id="V1.1.0">V1.1.0(released)</h4>

- feature：添加默认JWT服务端实现

- fixbug：设置响应体write操作仅可执行一次

<h4 id="V1.1.1">V1.1.1(released)</h4>

- fixbug：修正启动多个rest server时url映射冲突的错误

<h4 id="V1.2.0">V1.2.0(released)</h4>

- feature：请求及响应支持对cookie的处理

- feature：server支持自定义netty的boss及worker线程数

- feature：server支持自定义连接的keep-alive属性

- optimizing：url映射处理及filter映射处理的操作调整至server启动方式执行时执行

- optimizing：统一netty监听线程的名称

- optimizing：请求响应的header设置处理逻辑重构

- optimizing：对http请求的响应统一添加server框架名称及版本信息

<h4 id="V1.2.1">V1.2.1(released)</h4>

- feature：server支持自定义context-path

<h4 id="V1.3.0">V1.3.0(released)</h4>

- feature: request添加context-path字段

- optimizing：默认请求处理实现类重构

- optimizing: filter的执行顺序调整, 按order从小到大顺序执行

- optimizing: jwt token默认不添加"Bearer: "头

- optimizing: redirect支持重定向至其他server url.

- fixbug: 响应头设置Content-Type报NullPonterException问题解决

<h4 id="V1.4.0">V1.4.0(released)</h4>

- feature: 支持静态资源处理

<h4 id="V1.4.1">V1.4.1(released)</h4>

- optimizing: jwt默认实现调整至独立的masker-rest-jwt工程

- optimizing: 移除冗余maven依赖(commons-codec, commons-lang, commons-io)

<h4 id="V1.4.2">V1.4.2(released)</h4>

- feature: 支持文件上传(单文件or多文件)

- optimizing: 重构MRestHandlerType类, 更易理解.

- fixbug: 修正各类handler的分发处理逻辑

<h4 id="V1.4.3">V1.4.3(released)</h4>

- feature: 支持文件下载

- optimizing: 调整server接收的请求body最大size为50MB

<h4 id="V1.4.4">V1.4.4(released)</h4>

- optimizing: jwt默认实现合并至framework工程

- fixbug: 修正未指定context-path时对url的截取异常缺陷

<h4 id="V1.4.5">V1.4.5(released)</h4>

- optimizing: 添加IOUtils工具类

- optimizing: response补充write方法

<h4 id="V1.4.6">V1.4.6(released)</h4>

- feature: 添加默认的请求异常处理

- feature: 添加FileUtils工具类，提供文件新增/删除、压缩/解压相关工具方法

- optimizing: 序列化依赖从fastjson调整为jackson

- optimizing: jwt处理取消抛出异常

<h4 id="V1.4.7">V1.4.7(released)</h4>

- feature: 下载文件支持回调（文件下载完成时执行）

- fixbug: 修正同一url映射处理对象分别进行映射时报冲突的缺陷

<h4 id="V1.4.8">V1.4.8(released)</h4>

- feature: 对于未指定 "/" 或 "/index.html" 路径映射的服务，输出默认masker-rest主页面

- optimizing: 文件上传优化部分代码

- optimizing: 优化文件操作代码，增加运行时异常类：FileOperateException

<h4 id="V1.4.9">V1.4.9(released)</h4>

- optimizing: 优化序列化相应代码

- optimizing: 补充IOUtils工具类中写文件方法

<h4 id="V1.4.10">V1.4.10(released)</h4>

- optimizing: 优化补充IOUtils工具类中相应方法.

<h4 id="V1.5.0">V1.5.0(released)</h4>

- feature: 单个server支持发布多个context-path的服务

- feature: 支持自定义静态资源classpath扫描路径或磁盘文件扫描路径

- feature: 增加rest server默认配置文件读取

- optimizing: 补充样例代码

<h4 id="V1.6.0">V1.6.0(released)</h4>

- feature: 支持发布WebSocket服务及注册相应回调

- feature: 基于自定义WebSocket服务，实现简易聊天室

<h4 id="V1.6.1">V1.6.1(released)</h4>

- feature: 静态资源访问支持自定义前缀

- feature: 对于非正常http请求状态（如404、500等），返回状态码的同时输出特定页面

- optimizing: 优化默认index.html输出页面

- optimizing: rest server支持设置http content的最大值

<h4 id="V1.6.2">V1.6.2(released)</h4>

- fixbug: 修正文件上传处理代码中的文件拷贝逻辑

<h4 id="V1.6.3">V1.6.3(released)</h4>

- feature: 静态资源Content-Type根据文件名与Content-Type的映射表进行取值及返回

- feature: MRestContext支持指定自定义的序列化处理ObjectMapper对象

- feature: MRestContext支持设置devMode（定时更新静态资源）

- feature: rest server添加shutdown方法及部分属性getter方法

- optimizing: 默认响应的状态页面调整

- optimizing: 使用VoidFunc替换Runnable

- optimizing: server及context日志格式化输出

- fixbug: IOUtils中提供的数据流拷贝方法在执行完成执行数据流关闭操作（修正文件上传时无法删除临时文件的缺陷）

<h4 id="V1.6.4">V1.6.4(released)</h4>

- feature: 实现自定义Servlet的注册、分发及处理

- feature: JavaScript实现websocket客户端：[websocket.js][1]，并使用此客户端实现简易聊天室：[chatroom.html][2]

- optimizing: 原有的基于Filter的请求分发处理调整为使用Servlet实现（底层仍然使用Filter进行链式调用）

<h4 id="V1.6.5">V1.6.5(released)</h4>

- feature: context支持指定默认"/"请求重定向地址

- feature: context对于servlet映射处理进行唯一性约束(一个url仅能找到唯一的servlet进行处理)

- feature: server增加启动标识、启动时间（写到响应header及cookie中）

- feature：添加 [AbstractRestServlet][3]，子类继承此类并结合 [RequestMapping][4] 及 [GetMapping][5]、[PostMapping][6]注解实现servlet注册及分发处理

- feature：使用asm生成字节码（取代反射调用，提高执行效率，参见[ServletHandlerClassGenerator][7]）配合 [AbstractRestServlet][3] 实现servlet分发处理

<h4 id="V1.6.6">V1.6.6(released)</h4>

- feature: servlet映射处理url支持占位符匹配与解析处理，可从MRestRquest对象中获取占位符对应path参数

- feature: 添加 [MRestServletAdapter][8] 类用于servlet请求处理分发

<h4 id="V1.6.7">V1.6.7(released)</h4>

- fixbug: 动态Servlet实例缓存实现代码修正

- fixbug: 修正默认index页面静态资源页面重定向缺陷

- feature: websocket实现调整至MRestContext（MRestServer:MRestContext对应关系：1:n，MRestContext:MWebsocketContext对应关系：1:n）

- optimizing: 优化对请求url的解析与匹配处理逻辑（全路径匹配，仅检查url合法性，不对url进行截取修正）

- optimizing: 路径匹配与精确匹配（带占位符）情况兼容处理（根据url匹配度进行映射优先级选择）

<h4 id="V1.6.8">V1.6.8(released)</h4>

- fixbug: 文件压缩时对文件夹下文件压缩处理修正(仅关闭Entry输出流不关闭整个zip文件输出流)

<h4 id="V1.7.0">V1.7.0(doing)</h4>

- fixbug: 请求url与context-path相同时应重置请求url为"/"

- feature: jwt工具移至独立依赖工程masker-rest-jjwt

- feature: 移除冗余第三方包依赖, 如spring-core等, 大大缩减工程构建时fatjar的体积, 最小7.xMB

- optimizing: 静态资源扫描处理方式调整, 运行时未匹配到路由处理的get请求作为静态资源进行查找

- optimizing: 优化ASM字节码生成处理逻辑, 移除部分硬编码代码

- optimizing: 文档结构及描述优化, 增加可读性


[1]: ../masker-rest-framework/src/main/resources/masker-rest/static/websocket.js
[2]: ../masker-rest-demo/src/main/resources/static/chatroom.html
[3]: ../masker-rest-framework/src/main/java/io/github/jiashunx/masker/rest/framework/servlet/AbstractRestServlet.java
[4]: ../masker-rest-framework/src/main/java/io/github/jiashunx/masker/rest/framework/servlet/mapping/RequestMapping.java
[5]: ../masker-rest-framework/src/main/java/io/github/jiashunx/masker/rest/framework/servlet/mapping/GetMapping.java
[6]: ../masker-rest-framework/src/main/java/io/github/jiashunx/masker/rest/framework/servlet/mapping/PostMapping.java
[7]: ../masker-rest-framework/src/main/java/io/github/jiashunx/masker/rest/framework/util/ServletHandlerClassGenerator.java
[8]: ../masker-rest-framework/src/main/java/io/github/jiashunx/masker/rest/framework/servlet/AbstractRestServlet.java






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
