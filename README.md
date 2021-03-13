
### masker-rest

- 项目简介：基于Netty实现Http Server Framework，极简API发布Rest服务及Websocket服务（端口可复用）

- 主要功能：
   - Http Server Framework
      - 支持rest请求处理注册与分发（同一端口支持发布多个context-path服务），支持重定向、转发等
      - 支持Filter的注册与拦截处理
      - 支持Servlet的注册与拦截处理（精确匹配（支持占位符）、路径匹配、拓展名匹配）
      - 支持静态资源文件访问（classpath中静态资源文件及磁盘文件，支持动态刷新）
      - 支持文件上传、下载、压缩解压等
      - 简易的jwt实现
      - 基于ASM实现Servlet字节码生成与处理（简单实现）
   - Websocket Server Framework
      - 复用Http服务器端口发布websocket服务（可发布多个）
      - websocket请求处理回调（建立连接、销毁连接、接收消息等）

- 工程介绍：

   - masker-rest-framework：http server实现framework包，引入到工程后即可使用相应API创建http/websocket server

   ```text
   <dependency>
     <groupId>io.github.jiashunx</groupId>
     <artifactId>masker-rest-framework</artifactId>
     <version>${mrf.version}</version>
   </dependency>
   ```

   - masker-rest-demo：基于masker-rest-framework实现的http server样例代码

- 使用文档：

   - 使用样例点此链接 [UserGuide](./docs/UserGuide.md)

- 版本清单（最新版本：<b>1.6.6</b>）：

   - version 1.0.0 (released)
      - feature：支持发布rest接口
      - feature：支持filter
      - feature：支持重定向与转发
      - feature：支持设置响应头
   - version 1.1.0 (released)
      - feature：添加默认JWT服务端实现
      - fixbug：设置响应体write操作仅可执行一次
   - version 1.1.1 (released)
      - fixbug：修正启动多个rest server时url映射冲突的错误
   - version 1.2.0 (released)
      - feature：请求及响应支持对cookie的处理
      - feature：server支持自定义netty的boss及worker线程数
      - feature：server支持自定义连接的keep-alive属性
      - optimizing：url映射处理及filter映射处理的操作调整至server启动方式执行时执行
      - optimizing：统一netty监听线程的名称
      - optimizing：请求响应的header设置处理逻辑重构
      - optimizing：对http请求的响应统一添加server框架名称及版本信息
   - version 1.2.1 (released)
      - feature：server支持自定义context-path
   - version 1.3.0 (released)
      - feature: request添加context-path字段
      - optimizing：默认请求处理实现类重构
      - optimizing: filter的执行顺序调整, 按order从小到大顺序执行
      - optimizing: jwt token默认不添加"Bearer: "头
      - optimizing: redirect支持重定向至其他server url.
      - fixbug: 响应头设置Content-Type报NullPonterException问题解决
   - version 1.4.0 (released)
      - feature: 支持静态资源处理
   - version 1.4.1 (released)
      - optimizing: jwt默认实现调整至独立的masker-rest-jwt工程
      - optimizing: 移除冗余maven依赖(commons-codec, commons-lang, commons-io)
   - version 1.4.2 (released)
      - feature: 支持文件上传(单文件or多文件)
      - optimizing: 重构MRestHandlerType类, 更易理解.
      - fixbug: 修正各类handler的分发处理逻辑
   - version 1.4.3 (released)
      - feature: 支持文件下载
      - optimizing: 调整server接收的请求body最大size为50MB
   - version 1.4.4 (released)
      - optimizing: jwt默认实现合并至framework工程
      - fixbug: 修正未指定context-path时对url的截取异常缺陷
   - version 1.4.5 (released)
      - optimizing: 添加IOUtils工具类
      - optimizing: response补充write方法
   - version 1.4.6 (released)
      - feature: 添加默认的请求异常处理
      - feature: 添加FileUtils工具类，提供文件新增/删除、压缩/解压相关工具方法
      - optimizing: 序列化依赖从fastjson调整为jackson
      - optimizing: jwt处理取消抛出异常
   - version 1.4.7 (released)
      - feature: 下载文件支持回调（文件下载完成时执行）
      - fixbug: 修正同一url映射处理对象分别进行映射时报冲突的缺陷
   - version 1.4.8 (released)
      - feature: 对于未指定 "/" 或 "/index.html" 路径映射的服务，输出默认masker-rest主页面
      - optimizing: 文件上传优化部分代码
      - optimizing: 优化文件操作代码，增加运行时异常类：FileOperateException
   - version 1.4.9 (released)
      - optimizing: 优化序列化相应代码
      - optimizing: 补充IOUtils工具类中写文件方法
   - verion 1.4.10 (released)
      - optimizing: 优化补充IOUtils工具类中相应方法.
   - version 1.5.0 (released)
      - feature: 单个server支持发布多个context-path的服务
      - feature: 支持自定义静态资源classpath扫描路径或磁盘文件扫描路径
      - feature: 增加rest server默认配置文件读取
      - optimizing: 补充样例代码
   - version 1.6.0 (released)
      - feature: 支持发布WebSocket服务及注册相应回调
      - feature: 基于自定义WebSocket服务，实现简易聊天室
   - version 1.6.1 (released)
      - feature: 静态资源访问支持自定义前缀
      - feature: 对于非正常http请求状态（如404、500等），返回状态码的同时输出特定页面
      - optimizing: 优化默认index.html输出页面
      - optimizing: rest server支持设置http content的最大值
   - version 1.6.2 (released)
      - fixbug: 修正文件上传处理代码中的文件拷贝逻辑
   - version 1.6.3 (released)
      - feature: 静态资源Content-Type根据文件名与Content-Type的映射表进行取值及返回
      - feature: MRestContext支持指定自定义的序列化处理ObjectMapper对象
      - feature: MRestContext支持设置devMode（定时更新静态资源）
      - feature: rest server添加shutdown方法及部分属性getter方法
      - optimizing: 默认响应的状态页面调整
      - optimizing: 使用VoidFunc替换Runnable
      - optimizing: server及context日志格式化输出
      - fixbug: IOUtils中提供的数据流拷贝方法在执行完成执行数据流关闭操作（修正文件上传时无法删除临时文件的缺陷）
   - version 1.6.4 (released)
      - feature: 实现自定义Servlet的注册、分发及处理
      - feature: JavaScript实现websocket客户端：[websocket.js][1]，并使用此客户端实现简易聊天室：[chatroom.html][2]
      - optimizing: 原有的基于Filter的请求分发处理调整为使用Servlet实现（底层仍然使用Filter进行链式调用）
   - version 1.6.5 (released)
      - feature: context支持指定默认"/"请求重定向地址
      - feature: context对于servlet映射处理进行唯一性约束(一个url仅能找到唯一的servlet进行处理)
      - feature: server增加启动标识、启动时间（写到响应header及cookie中）
      - feature：添加 [AbstractRestServlet][3]，子类继承此类并结合 [RequestMapping][4] 及 [GetMapping][5]、[PostMapping][6]注解实现servlet注册及分发处理
      - feature：使用asm生成字节码（取代反射调用，提高执行效率，参见[ServletHandlerClassGenerator][7]）配合 [AbstractRestServlet][3] 实现servlet分发处理
   - version 1.6.6 (released)
      - feature: servlet映射处理url支持占位符匹配与解析处理，可从MRestRquest对象中获取占位符对应path参数
      - feature: 添加 [MRestServletAdapter][8] 类用于servlet请求处理分发
   - version 1.6.7 (doing)
      - fixbug: 动态Servlet实例缓存实现代码修正
      - fixbug: 修正默认index页面静态资源页面重定向缺陷
      - feature: websocket实现调整至MRestContext（MRestServer:MRestContext对应关系：1:n，MRestContext:MWebsocketContext对应关系：1:n）
      - optimizing: 优化对请求url的解析与匹配处理逻辑（全路径匹配，仅检查url合法性，不对url进行截取修正）
      - TODO 参考spring-core实现classpath资源扫描，移除spring-core依赖

[1]: masker-rest-framework/src/main/resources/masker-rest/static/websocket.js
[2]: masker-rest-demo/src/main/resources/static/chatroom.html
[3]: masker-rest-framework/src/main/java/io/github/jiashunx/masker/rest/framework/servlet/AbstractRestServlet.java
[4]: masker-rest-framework/src/main/java/io/github/jiashunx/masker/rest/framework/servlet/mapping/RequestMapping.java
[5]: masker-rest-framework/src/main/java/io/github/jiashunx/masker/rest/framework/servlet/mapping/GetMapping.java
[6]: masker-rest-framework/src/main/java/io/github/jiashunx/masker/rest/framework/servlet/mapping/PostMapping.java
[7]: masker-rest-framework/src/main/java/io/github/jiashunx/masker/rest/framework/util/ServletHandlerClassGenerator.java
[8]: masker-rest-framework/src/main/java/io/github/jiashunx/masker/rest/framework/servlet/AbstractRestServlet.java
