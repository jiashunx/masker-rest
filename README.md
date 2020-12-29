
### masker-rest

- 项目简介：基于Netty实现Http Server，极简API发布Rest服务及Websocket服务

- 主要功能：
   - Http服务器
      - rest请求处理注册与分发（同一端口支持发布多个context-path服务）
      - filter注册与处理
      - 静态资源文件访问（classpath中静态资源文件及磁盘文件）
      - 文件上传、下载、压缩解压等
      - 简易的jwt实现
   - Websocket服务器
      - 复用http服务器端口发布websocket服务（可发布多个）
      - websocket请求处理回调（建立连接、销毁连接）

- 工程介绍：

   - masker-rest-framework：http server实现framework包，引入到工程后即可使用相应API创建http server

   ```text
   <dependency>
     <groupId>io.github.jiashunx</groupId>
     <artifactId>masker-rest-framework</artifactId>
     <version>${mrf.version}</version>
   </dependency>
   ```

   - masker-rest-demo：基于masker-rest-framework实现的http server样例代码

- 使用文档：

   - [UserGuide](./docs/UserGuide.md)

- 版本清单（最新版本：<b>1.6.1</b>）：

   - version 1.0.0 (released)
      - feature：支持发布rest接口
      - feature：支持filter
      - feature：支持重定向与转发
      - feature：支持设置响应头
   - version 1.1.0 (released)
      - feature：添加默认JWT服务端实现
      - bugfix：设置响应体write操作仅可执行一次
   - version 1.1.1 (released)
      - bugfix：修正启动多个rest server时url映射冲突的错误
   - version 1.2.0 (released)
      - feature：请求及响应支持对cookie的处理
      - feature：server支持自定义netty的boss及worker线程数
      - feature：server支持自定义连接的keep-alive属性
      - refactor：url映射处理及filter映射处理的操作调整至server启动方式执行时执行
      - refactor：统一netty监听线程的名称
      - refactor：请求响应的header设置处理逻辑重构
      - refactor：对http请求的响应统一添加server框架名称及版本信息
   - version 1.2.1 (released)
      - feature：server支持自定义context-path
   - version 1.3.0 (released)
      - feature: request添加context-path字段
      - refactor：默认请求处理实现类重构
      - refactor: filter的执行顺序调整, 按order从小到大顺序执行
      - refactor: jwt token默认不添加"Bearer: "头
      - refactor: redirect支持重定向至其他server url.
      - bugfix: 响应头设置Content-Type报NullPonterException问题解决
   - version 1.4.0 (released)
      - feature: 支持静态资源处理
   - version 1.4.1 (released)
      - refactor: jwt默认实现调整至独立的masker-rest-jwt工程
      - refactor: 移除冗余maven依赖(commons-codec, commons-lang, commons-io)
   - version 1.4.2 (released)
      - feature: 支持文件上传(单文件or多文件)
      - refactor: 重构MRestHandlerType类, 更易理解.
      - bugfix: 修正各类handler的分发处理逻辑
   - version 1.4.3 (released)
      - feature: 支持文件下载
      - refactor: 调整server接收的请求body最大size为50MB
   - version 1.4.4 (released)
      - refactor: jwt默认实现合并至framework工程
      - bugfix: 修正未指定context-path时对url的截取异常缺陷
   - version 1.4.5 (released)
      - refactor: 添加IOUtils工具类
      - refactor: response补充write方法
   - version 1.4.6 (released)
      - feature: 添加默认的请求异常处理
      - feature: 添加FileUtils工具类，提供文件新增/删除、压缩/解压相关工具方法
      - refactor: 序列化依赖从fastjson调整为jackson
      - refactor: jwt处理取消抛出异常
   - version 1.4.7 (released)
      - feature: 下载文件支持回调（文件下载完成时执行）
      - bugfix: 修正同一url映射处理对象分别进行映射时报冲突的缺陷
   - version 1.4.8 (released)
      - feature: 对于未指定 "/" 或 "/index.html" 路径映射的服务，输出默认masker-rest主页面
      - refactor: 文件上传优化部分代码
      - refactor: 优化文件操作代码，增加运行时异常类：FileOperateException
   - version 1.4.9 (released)
      - refactor: 优化序列化相应代码
      - refactor: 补充IOUtils工具类中写文件方法
   - verion 1.4.10 (released)
      - refactor: 优化补充IOUtils工具类中相应方法.
   - version 1.5.0 (released)
      - feature: 单个server支持发布多个context-path的服务
      - feature: 支持自定义静态资源classpath扫描路径或磁盘文件扫描路径
      - feature: 增加rest server默认配置文件读取
      - refactor: 补充样例代码
   - version 1.6.0 (released)
      - feature: 支持发布WebSocket服务及注册相应回调
      - feature: 基于自定义WebSocket服务，实现简易聊天室
   - version 1.6.1 (released)
      - feature: 静态资源访问支持自定义前缀
      - feature: 对于非正常http请求状态（如404、500等），返回状态码的同时输出特定页面
      - refactor: 优化默认index.html输出页面
      - refactor: rest server支持设置http content的最大值
   - TODO
      - 移除spring-core依赖（参考spring-core实现jar包资源扫描）
