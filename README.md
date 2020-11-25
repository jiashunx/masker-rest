
### masker-rest

- 项目简介：基于Netty实现Http Server，极简API发布Rest服务

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

- 版本清单：

   - version 1.0.0
      - feature：支持发布rest接口
      - feature：支持filter
      - feature：支持重定向与转发
      - feature：支持设置响应头
   - version 1.1.0
      - feature：添加默认JWT服务端实现
      - bugfix：设置响应体write操作仅可执行一次
   - version 1.1.1
      - bugfix：修正启动多个rest server时url映射冲突的错误
   - version 1.2.0
      - feature：请求及响应支持对cookie的处理
      - feature：server支持自定义netty的boss及worker线程数
      - feature：server支持自定义连接的keep-alive属性
      - refactor：url映射处理及filter映射处理的操作调整至server启动方式执行时执行
      - refactor：统一netty监听线程的名称
      - refactor：请求响应的header设置处理逻辑重构
      - refactor：对http请求的响应统一添加server框架名称及版本信息
   - version 1.2.1
      - feature：server支持自定义context-path
   - version 1.3.0
      - feature: request添加context-path字段
      - refactor：默认请求处理实现类重构
      - refactor: filter的执行顺序调整, 按order从小到大顺序执行
      - refactor: jwt token默认不添加"Bearer: "头
      - refactor: redirect支持重定向至其他server url.
      - bugfix: 响应头设置Content-Type报NullPonterException问题解决

   - TODO
      - 支持静态资源处理
      - 支持文件的上传下载
