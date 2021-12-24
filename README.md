
### masker-rest

- 项目简介: 基于Netty实现Http Server Framework, 极简API发布Rest服务及Websocket服务

- 主要功能: 
   - Http Server Framework
      - 支持rest请求处理注册与分发（同一端口支持发布多个context-path服务）, 支持重定向、转发等
      - 支持Filter的注册与拦截处理
      - 支持Servlet的注册与拦截处理（精确匹配（支持占位符）、路径匹配、拓展名匹配）
      - 支持静态资源文件访问（classpath中静态资源文件及磁盘文件, 支持动态刷新）
      - 支持文件上传、下载、压缩解压等
      - 基于ASM实现Servlet字节码生成与处理（简单实现）
   - Websocket Server Framework
      - 复用Http服务器端口发布Websocket服务（websocket服务可根据url进行请求分发）

- 版本清单(最新版本: <b>1.6.8</b>): 

   - 参见: [Version.md][0]

- 工程介绍: 

   - masker-rest-framework: http server实现framework包, 引入到工程后即可使用相应API创建http/websocket server

   - masker-rest-jjwt: 简易jwt实现

   - masker-rest-demo: 基于masker-rest-framework实现的http server样例代码

- 使用文档: 

   - 参见: [UserGuide][1]

[0]: ./docs/Version.md
[1]: ./docs/UserGuide.md
