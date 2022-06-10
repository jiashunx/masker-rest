
<h3 id="V">masker-rest版本清单</h3>

- [V2.0.0(released)](#2.0.0)
- [V1.7.2(released)](#V1.7.2)
- [V1.7.1.1(released)](#V1.7.1.1)
- [V1.7.1(released)](#V1.7.1)
- [V1.7.0(released)](#V1.7.0)
- [V1.6.8(released)](#V1.6.8)
- [V1.6.7(released)](#V1.6.7)
- [V1.6.6(released)](#V1.6.6)
- [V1.6.5(released)](#V1.6.5)
- [V1.6.4(released)](#V1.6.4)
- [V1.6.3(released)](#V1.6.3)
- [V1.6.2(released)](#V1.6.2)
- [V1.6.1(released)](#V1.6.1)
- [V1.6.0(released)](#V1.6.0)
- [V1.5.0(released)](#V1.5.0)
- [V1.4.10(released)](#V1.4.10)
- [V1.4.9(released)](#V1.4.9)
- [V1.4.8(released)](#V1.4.8)
- [V1.4.7(released)](#V1.4.7)
- [V1.4.6(released)](#V1.4.6)
- [V1.4.5(released)](#V1.4.5)
- [V1.4.4(released)](#V1.4.4)
- [V1.4.3(released)](#V1.4.3)
- [V1.4.2(released)](#V1.4.2)
- [V1.4.1(released)](#V1.4.1)
- [V1.4.0(released)](#V1.4.0)
- [V1.3.0(released)](#V1.3.0)
- [V1.2.1(released)](#V1.2.1)
- [V1.2.0(released)](#V1.2.0)
- [V1.1.1(released)](#V1.1.1)
- [V1.1.0(released)](#V1.1.0)
- [V1.0.0(released)](#V1.0.0)

<h4 id="V2.0.0">V2.0.0(released)</h4>

- feature: 编译构建环境由JDK8升级至JDK11

- optimizing: 增加MRestFilterChain接口

- optimizing: 增加MRestFilterChainAdapter用于过滤器调用链自定义处理

<h4 id="V1.7.2">V1.7.2(released)</h4>

- fixbug: 修正解压文件后未关闭输入流导致文件占用的缺陷

- fixbug: json序列化为对象列表代码修正

- feature: 增加对PEM格式密钥的解析处理

- optimizing: 优化及补充部分工具类公共代码

<h4 id="V1.7.1.1">V1.7.1.1(released)</h4>

- fixbug: 修正header属性覆盖的缺陷（1.7.0引入）

<h4 id="V1.7.1">V1.7.1(released)</h4>

- fixbug: 修正filter拦截url不能出现空格的缺陷

- feature: 添加masker-rest-rsa工程，包含RSA前后端交互公共方法，用于RSA加解密及签名验证

- feature: 添加jquery、jsencrypt、base64、jsrsasign等前端依赖至classpath静态资源路径

- feature: 添加对webjar中静态资源的自动扫描及匹配处理，无需对webjar资源显式指定映射规则

- optimizing: 优化异常类继承层次结构，剥离出顶级异常基类: MRuntimeException

- optimizing: 优化及补充部分工具类公共代码

<h4 id="V1.7.0">V1.7.0(released)</h4>

- fixbug: 请求url与context-path相同时应重置请求url为"/"

- feature: jwt工具移至独立依赖工程masker-rest-jjwt

- feature: 移除冗余第三方包依赖, 如spring-core等, 大大缩减工程构建时fatjar的体积, 最小7.xMB

- feature: 移除对jdk ASM依赖, 添加ASM7.0作为第三方依赖用于字节码生成

- optimizing: 静态资源扫描处理方式调整, 运行时未匹配到路由处理的get请求作为静态资源进行查找

- optimizing: 优化ASM字节码生成处理逻辑, 移除部分硬编码代码

- optimizing: 优化写响应输出流时的Header处理

- optimizing: 文档结构及描述优化, 增加可读性

<h4 id="V1.6.8">V1.6.8(released)</h4>

- fixbug: 文件压缩时对文件夹下文件压缩处理修正(仅关闭Entry输出流不关闭整个zip文件输出流)

<h4 id="V1.6.7">V1.6.7(released)</h4>

- fixbug: 动态Servlet实例缓存实现代码修正

- fixbug: 修正默认index页面静态资源页面重定向缺陷

- feature: websocket实现调整至MRestContext（MRestServer:MRestContext对应关系：1:n，MRestContext:MWebsocketContext对应关系：1:n）

- optimizing: 优化对请求url的解析与匹配处理逻辑（全路径匹配，仅检查url合法性，不对url进行截取修正）

- optimizing: 路径匹配与精确匹配（带占位符）情况兼容处理（根据url匹配度进行映射优先级选择）

<h4 id="V1.6.6">V1.6.6(released)</h4>

- feature: servlet映射处理url支持占位符匹配与解析处理，可从MRestRquest对象中获取占位符对应path参数

- feature: 添加 [MRestServletAdapter][8] 类用于servlet请求处理分发

<h4 id="V1.6.5">V1.6.5(released)</h4>

- feature: context支持指定默认"/"请求重定向地址

- feature: context对于servlet映射处理进行唯一性约束(一个url仅能找到唯一的servlet进行处理)

- feature: server增加启动标识、启动时间（写到响应header及cookie中）

- feature：添加 [AbstractRestServlet][3]，子类继承此类并结合 [RequestMapping][4] 及 [GetMapping][5]、[PostMapping][6]注解实现servlet注册及分发处理

- feature：使用asm生成字节码（取代反射调用，提高执行效率，参见[ServletHandlerClassGenerator][7]）配合 [AbstractRestServlet][3] 实现servlet分发处理

<h4 id="V1.6.4">V1.6.4(released)</h4>

- feature: 实现自定义Servlet的注册、分发及处理

- feature: JavaScript实现websocket客户端：[websocket.js][1]，并使用此客户端实现简易聊天室：[chatroom.html][2]

- optimizing: 原有的基于Filter的请求分发处理调整为使用Servlet实现（底层仍然使用Filter进行链式调用）

<h4 id="V1.6.3">V1.6.3(released)</h4>

- feature: 静态资源Content-Type根据文件名与Content-Type的映射表进行取值及返回

- feature: MRestContext支持指定自定义的序列化处理ObjectMapper对象

- feature: MRestContext支持设置devMode（定时更新静态资源）

- feature: rest server添加shutdown方法及部分属性getter方法

- optimizing: 默认响应的状态页面调整

- optimizing: 使用VoidFunc替换Runnable

- optimizing: server及context日志格式化输出

- fixbug: IOUtils中提供的数据流拷贝方法在执行完成执行数据流关闭操作（修正文件上传时无法删除临时文件的缺陷）

<h4 id="V1.6.2">V1.6.2(released)</h4>

- fixbug: 修正文件上传处理代码中的文件拷贝逻辑

<h4 id="V1.6.1">V1.6.1(released)</h4>

- feature: 静态资源访问支持自定义前缀

- feature: 对于非正常http请求状态（如404、500等），返回状态码的同时输出特定页面

- optimizing: 优化默认index.html输出页面

- optimizing: rest server支持设置http content的最大值

<h4 id="V1.6.0">V1.6.0(released)</h4>

- feature: 支持发布WebSocket服务及注册相应回调

- feature: 基于自定义WebSocket服务，实现简易聊天室

<h4 id="V1.5.0">V1.5.0(released)</h4>

- feature: 单个server支持发布多个context-path的服务

- feature: 支持自定义静态资源classpath扫描路径或磁盘文件扫描路径

- feature: 增加rest server默认配置文件读取

- optimizing: 补充样例代码

<h4 id="V1.4.10">V1.4.10(released)</h4>

- optimizing: 优化补充IOUtils工具类中相应方法.

<h4 id="V1.4.9">V1.4.9(released)</h4>

- optimizing: 优化序列化相应代码

- optimizing: 补充IOUtils工具类中写文件方法

<h4 id="V1.4.8">V1.4.8(released)</h4>

- feature: 对于未指定 "/" 或 "/index.html" 路径映射的服务，输出默认masker-rest主页面

- optimizing: 文件上传优化部分代码

- optimizing: 优化文件操作代码，增加运行时异常类：FileOperateException

<h4 id="V1.4.7">V1.4.7(released)</h4>

- feature: 下载文件支持回调（文件下载完成时执行）

- fixbug: 修正同一url映射处理对象分别进行映射时报冲突的缺陷

<h4 id="V1.4.6">V1.4.6(released)</h4>

- feature: 添加默认的请求异常处理

- feature: 添加FileUtils工具类，提供文件新增/删除、压缩/解压相关工具方法

- optimizing: 序列化依赖从fastjson调整为jackson

- optimizing: jwt处理取消抛出异常

<h4 id="V1.4.5">V1.4.5(released)</h4>

- optimizing: 添加IOUtils工具类

- optimizing: response补充write方法

<h4 id="V1.4.4">V1.4.4(released)</h4>

- optimizing: jwt默认实现合并至framework工程

- fixbug: 修正未指定context-path时对url的截取异常缺陷

<h4 id="V1.4.3">V1.4.3(released)</h4>

- feature: 支持文件下载

- optimizing: 调整server接收的请求body最大size为50MB

<h4 id="V1.4.2">V1.4.2(released)</h4>

- feature: 支持文件上传(单文件or多文件)

- optimizing: 重构MRestHandlerType类, 更易理解.

- fixbug: 修正各类handler的分发处理逻辑

<h4 id="V1.4.1">V1.4.1(released)</h4>

- optimizing: jwt默认实现调整至独立的masker-rest-jwt工程

- optimizing: 移除冗余maven依赖(commons-codec, commons-lang, commons-io)

<h4 id="V1.4.0">V1.4.0(released)</h4>

- feature: 支持静态资源处理

<h4 id="V1.3.0">V1.3.0(released)</h4>

- feature: request添加context-path字段

- optimizing：默认请求处理实现类重构

- optimizing: filter的执行顺序调整, 按order从小到大顺序执行

- optimizing: jwt token默认不添加"Bearer: "头

- optimizing: redirect支持重定向至其他server url.

- fixbug: 响应头设置Content-Type报NullPonterException问题解决

<h4 id="V1.2.1">V1.2.1(released)</h4>

- feature：server支持自定义context-path

<h4 id="V1.2.0">V1.2.0(released)</h4>

- feature：请求及响应支持对cookie的处理

- feature：server支持自定义netty的boss及worker线程数

- feature：server支持自定义连接的keep-alive属性

- optimizing：url映射处理及filter映射处理的操作调整至server启动方式执行时执行

- optimizing：统一netty监听线程的名称

- optimizing：请求响应的header设置处理逻辑重构

- optimizing：对http请求的响应统一添加server框架名称及版本信息

<h4 id="V1.1.1">V1.1.1(released)</h4>

- fixbug：修正启动多个rest server时url映射冲突的错误

<h4 id="V1.1.0">V1.1.0(released)</h4>

- feature：添加默认JWT服务端实现

- fixbug：设置响应体write操作仅可执行一次

<h4 id="V1.0.0">V1.0.0(released)</h4>

- feature：支持发布rest接口

- feature：支持filter

- feature：支持重定向与转发

- feature：支持设置响应头


[1]: ../masker-rest-framework/src/main/resources/masker-rest/static/websocket.js
[2]: ../masker-rest-demo/src/main/resources/static/chatroom.html
[3]: ../masker-rest-framework/src/main/java/io/github/jiashunx/masker/rest/framework/servlet/AbstractRestServlet.java
[4]: ../masker-rest-framework/src/main/java/io/github/jiashunx/masker/rest/framework/servlet/mapping/RequestMapping.java
[5]: ../masker-rest-framework/src/main/java/io/github/jiashunx/masker/rest/framework/servlet/mapping/GetMapping.java
[6]: ../masker-rest-framework/src/main/java/io/github/jiashunx/masker/rest/framework/servlet/mapping/PostMapping.java
[7]: ../masker-rest-framework/src/main/java/io/github/jiashunx/masker/rest/framework/util/ServletHandlerClassGenerator.java
[8]: ../masker-rest-framework/src/main/java/io/github/jiashunx/masker/rest/framework/servlet/AbstractRestServlet.java

