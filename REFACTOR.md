# masker-rest 缺陷与优化方向分析（REFACTOR）

> 本文基于对 `1.8.0-SNAPSHOT`（JDK8）分支的文档与源码评审，并经资深评审（@oracle）逐条反汇编核验。
> 目的：梳理现存缺陷、优化方向与可执行待办，作为后续重构/修复的输入。**本文不含任何代码改动。**

---

## 0. 背景与总体结论

- **分支现状**：`1.8.0-SNAPSHOT` 与 `2.4.0-SNAPSHOT` 的差异**仅在 `pom.xml`**（版本号 + `jdk.compiler.version`），源码完全一致。
  - 1.8.0-SNAPSHOT：JDK8 构建；2.4.0-SNAPSHOT：JDK11 构建。
  - 合并方向 `1.8.0-SNAPSHOT -> 2.4.0-SNAPSHOT` 实质是**构建配置/版本对齐**。
  - **策略含义**：所有代码缺陷应**先在 1.8.0 修复，再前向并入 2.4.0**，一次修复两分支受益；两分支应仅差 `jdk.compiler.version`（版本统一放父 pom 属性）。
- **总体评估**：框架整体结构清晰，但存在
  1) **真实可利用的路径穿越**（静态资源）；
  2) 一批**生命周期/并发/HTTP 语义**缺陷；
  3) **依赖版本普遍过旧**（Netty 4.1.73 落后约 4.5 年，且含与文件上传直接相关的 CVE）。
- **核验说明**：下文每条均标注位置与严重级别；标注了重建（recon）中被**修正/降级**的结论，避免误判优先级。

严重级别定义：**Critical** 可直接导致任意文件读取/写入等安全后果；**High** 影响可用性/数据正确性/明显安全风险；**Medium** 特定场景下出错或性能问题；**Low** 代码质量/健壮性/性能微优化。

---

## 1. 缺陷清单（已核验）

### 1.1 Critical

| ID | 问题 | 位置 | 说明与修复 |
|----|------|------|-----------|
| A | **磁盘静态资源路径穿越** | `masker-rest-framework/.../servlet/StaticResourceFinder.java:149-152` | 用 `getAbsolutePath()+startsWith` 做包含校验，**不解析 `..`**。`/prefix/../../etc/passwd` 能通过校验，OS 在打开时解析 `..` 逃逸出配置根；根 context 会对 URL **百分号解码**，`%2e%2e/` 同样可绕过。已端到端验证可达（需配置 diskpath 资源）。**修复**：改用 `File.getCanonicalPath()`/`Path.normalize()` 做包含校验，并加分隔符边界（`child.startsWith(root + File.separator)`）；拒绝/归一化 `..`。 |
| D | **非根 context 的 URL 解析错误（严重）** | `.../handler/MRestServerChannelHandler.java:275,283-308` | `originUrl = queryStringDecoder.toString()`（= 含 query 的**未解码**原始 URI）。非根 context 时 `url = originUrl.substring(contextPath.length())` 会**带上 `?a=1`**，且未解码 → filter/servlet/静态资源匹配全部失败；根 context 分支用的是**已解码** `path()`，两者解码行为不一致。context 选择用 `startsWith` 无边界校验（`/abc` 命中 `/a`，得到 `bc`），且对 `ConcurrentHashMap.keySet()` 的"最后匹配胜出"**不确定**。**修复**：统一以已解码的 `path()` 做 context 匹配与 URL 派生；最长前缀 + 边界匹配；注册时按长度倒序确定顺序；保留独立的 rawQuery。 |

### 1.2 High

| ID | 问题 | 位置 | 说明与修复 |
|----|------|------|-----------|
| E | **EventLoopGroup 从不关闭 + 刷新线程不可停** | `.../MRestServer.java:328-329,285-302`；`.../util/MRestThreadFactory.java:27-29`；`.../MRestContext.java:94-104` | `bossGroup/workerGroup` 是 `start()` **局部变量**，`shutdown()` 只关 `serverChannel`；`shutdownGracefully()` 全仓库未出现。线程工厂强制 `setDaemon(false)` → **JVM 无法退出、线程池泄漏**（嵌入式/测试 start-stop 反复泄漏）。静态资源刷新线程为 `while(true)` 非守护且无引用（**修正**：仅当缓存+自动刷新同时开启才存在，默认均为 false）。**修复**：group 字段化；`shutdown()` 中关闭 serverChannel → 关闭子通道（`ChannelGroup`）→ `workerGroup/bossGroup.shutdownGracefully()`；刷新改用受生命周期管理的 `ScheduledExecutorService`；删除死代码 `syncThread`。 |
| G/H | **文件下载：TLS 不兼容 + 句柄泄漏窗口 + 状态码被忽略** | `.../util/MResponseHelper.java:159-187,163`；`.../MRestResponse.java:262-269,371-385` | `DefaultFileRegion` 无法被 `SslHandler` 加密 → **HTTPS 下载输出乱码/挂起**（`ChunkedWriteHandler` 已在管道中，可低代价改用 `ChunkedFile`）。`RandomAccessFile` 在 open 与 listener 注册之间的抛异常的窄窗口内会泄漏（**修正**：正常/失败路径在 progress listener 中确有 close）。`writeFile(status,...)` 的 status 只写入属性，`MResponseHelper` 硬编码 `HttpResponseStatus.OK` → 永远 200，206/404 文件响应不可能。**修复**：加密时用 `ChunkedFile`；status 透传到写响应；`RandomAccessFile` 设置用 try/finally；补充/明确 Range/206 支持。 |
| K | **multipart 属性 use-after-destroy + 解码器失败未销毁** | `.../handler/MRestServerChannelHandler.java:316-327`；`.../MRestFileUploadRequest.java` | 直接存 Netty `Attribute` 引用，随后 `decoder.destroy()` 释放后台缓冲 → 表单字段读到**空串**（内存态）或抛 `IllegalReferenceCountException`/IO 失败（磁盘态）。`destroy()` 不在 `finally`，畸形 multipart 抛出 `ErrorDataDecoderException` 会跳过清理 → 每次坏请求泄漏临时文件/缓冲（廉价 DoS）。`HttpPostRequestDecoder` 为旧 API。**修复**：解析时快照为 String 名值对；`try/finally` 确保 `destroy()`；迁移到 `HttpPostMultipartRequestDecoder`；显式配置 `DefaultHttpDataFactory` 阈值/临时目录。 |
| F | **`AbstractRestServlet.servletHandlerMap` 非线程安全** | `.../servlet/AbstractRestServlet.java:98,107-124` | 公共可变字段为 `WeakHashMap`，多 worker 线程下无锁 `get()` 与加锁 `put()` 数据竞争（`WeakHashMap.get()` 自身会修改表结构）→ 表损坏/潜在死循环。weak key 设计无意义（value 强引用 key）。`init()` 为 `synchronized` 使每次 `service()` 都取锁（热路径竞争）。**修复**：`private final ConcurrentHashMap<Class<?>, MRestServlet>` + `computeIfAbsent`；`init()` 改一次性 volatile 检查。 |
| — | **依赖 CVE（两分支通用）** | `pom.xml` | 见 §3。Netty 4.1.73 含 **CVE-2022-24823**（multipart 磁盘临时文件权限，直击本项目上传路径）、**CVE-2022-41915**（头值校验/响应拆分，`setHeader` 相关）及多项请求走私/HTTP2 修复；jackson-databind 2.11.3、gson 2.8.5（CVE-2022-25647）、logback 1.2.3（CVE-2023-6378）、jsoup 1.11.3、bouncycastle 1.56、jjwt 0.9.1（弃用，且拖入旧 jackson）。 |

### 1.3 Medium

| ID | 问题 | 位置 | 说明与修复 |
|----|------|------|-----------|
| B | **`FileUtils.unzip` Zip Slip** | `.../util/FileUtils.java:131-143`（尤其 `:134`） | 条目名直接拼接写入，可越界写文件（经典 Zip Slip），另有无界条目/大小（zip 炸弹）。**修正**：仓库内**无调用方**，属公共工具 API 的潜在陷阱（对下游库使用者为高危）。**修复**：条目落盘前做 canonical 包含校验；跳过含 `..`/绝对名条目；加条目数与总大小上限。 |
| C | **classpath 静态资源前缀逃逸** | `.../servlet/StaticResourceFinder.java:100-105` | 未过滤 `..`。**修正**：标准 JDK 加载器会阻止**逃出 classpath 根**（fat-jar 免疫）；真实风险是**目录部署**下逃逸到同根其他资源（前缀逃逸），自定义加载器可能更弱。**修复**：复用 §1.1-A 的统一 `..` 拒绝；校验解析结果仍在前缀内。 |
| J | **响应头重复（setHeader 走 add 语义）** | `.../MRestResponse.java:284-292,379-380`；`.../model/MRestHeaders.java:55-66,76-78,90-92`；`.../util/MResponseHelper.java:189-198` | 重复 `setHeader` 累加，全局+每次写入头合并再 `addAll`，可产生重复 `Content-Type`/`Location`/`Content-Length`；框架内已有触发点（handler 依据 Accept 追加 Content-Type）。`getHeader` 在多值时会抛异常。**修复**：为单值头引入真正的 `set`(替换) 语义，保留 `add` 用于 `Set-Cookie`；合并时去重单值头。 |
| — | **静态资源缓存无上限 + 全局锁 IO + 无 HTTP 缓存** | `.../servlet/StaticResourceFinder.java:36,67-89`；`.../servlet/StaticResourceServlet.java` | 未命中 URL 被永久缓存（唯一 URL 洪泛 = 内存 DoS）；失败加载持有**全局写锁**做磁盘 IO；静态文件整体读入 `byte[]` 常驻堆、不流式；无 ETag/Last-Modified/Cache-Control/304。**修复**：LRU + 上限（含负缓存 TTL）、按键加载、流式（`ChunkedFile`）、补齐 HTTP 缓存语义。 |
| — | **WebSocket 生命周期/安全** | `.../handler/MRestServerChannelHandler.java:88-90,114-127,155-171` | 异常断开（TCP reset / `exceptionCaught`）不触发 `inactiveCallback` 与清理（仅 `CloseWebSocketFrame` 触发）；handshaker 缓存为普通 `HashMap` + 不安全 DCL。**修复**：清理/回调移入 `channelInactive`（只触发一次）；`ConcurrentHashMap.computeIfAbsent`；增加可选 Origin 校验（CSWSH）与空闲超时。 |
| — | **Server 加固缺失** | `.../handler/MRestServerChannelHandler.java:235-239`；`.../handler/MRestServerChannelInitializer.java` | 无 `IdleStateHandler`/读超时（slowloris）；框架版本指纹头与标识 cookie 不可关闭、cookie 缺 HttpOnly/Secure/SameSite；`sslEnabled(true)` 静默使用自签证书（不安全默认）。**修复**：加空闲/读超时；加固并可选关闭指纹头/cookie；自签时显式告警或要求显式 `SslContext`。 |
| — | **请求体全量入堆无总量/单文件/数量限制** | `.../handler/MRestServerChannelInitializer.java:40`；`.../handler/MRestServerChannelHandler.java:343-351`；`.../model/MRestFileUpload.java:38-63` | `HttpObjectAggregator(50MB)` 全量聚合，multipart 再解码复制到临时文件，无总量/单文件/数量上限 → OOM/磁盘耗尽（DoS）；另有一次多余拷贝。**修复**：下调/可配置聚合上限，流式处理 multipart，限制文件数/总量。 |

### 1.4 Low（质量/健壮性/性能微优化）

| ID | 问题 | 位置 |
|----|------|------|
| — | filter 正则**每请求每 filter 重编译**（`String.matches`），且 URL 文字段未转义（正则注入/ReDoS） | `.../MRestContext.java:581-593` |
| — | servlet 每请求线性扫描并构造匹配模型（`UrlMatchModel`/`AtomicReference`/列表） | `.../MRestContext.java:444-531`；`.../servlet/AbstractRestServlet.java:127-148` |
| — | `MRestDispatchServlet` 405 分支不可达（按 method 查表，未命中直接落到 404） | `.../servlet/MRestDispatchServlet.java:40-44` |
| — | `application/x-www-form-urlencoded` body 未做 percent/`+` 解码，空值 `a=` 解析错误 | `.../handler/MRestServerChannelHandler.java:364-380` |
| — | `writeJSON(String)` 用平台默认字符集且未加 JSON Content-Type | `.../util/MResponseHelper.java:54-56` |
| — | `IOUtils.copy` 循环条件 `>= 0`（应 `!= -1`/`> 0`）；缓冲区仅 1KB | `.../util/IOUtils.java:405-411` |
| — | `MimetypesFileTypeMap` 每次查找都 new（重载 MIME 配置） | `.../util/MimetypeUtils.java:43`；`.../util/IOUtils.java:199` |
| — | `MRestRequest` 的 `toLowerCase()` 未用 `Locale.ROOT`（土耳其 I 问题） | `.../MRestRequest.java:139,151` |
| — | `UrlPatternModel.getRegularCount()` 用 `split("*")` 会抛 `PatternSyntaxException`（当前无调用，潜在） | `.../model/UrlPatternModel.java:62-67` |
| — | `FileUtils.newFile` 对无父目录路径 NPE；`newFile/newDirectory` 存在 TOCTOU | `.../util/FileUtils.java:45-59` |
| — | 前缀资源扫描用 `HashMap`，顺序不确定（注释声称"配置顺序"） | `.../MRestContext.java:430,434` |
| — | `AbstractRestServlet` 生成的字节码对 `static` 注解方法用 `INVOKEVIRTUAL`（`VerifyError`）；`getDeclaredMethods()` 忽略继承方法 | `.../enhance/ServletHandlerClassGenerator.java:88-109`；`.../servlet/AbstractRestServlet.java:155` |
| — | `remoteAddress` 实为 `localAddress()`（语义误导） | `.../handler/MRestServerChannelHandler.java:266-271` |
| — | `getCallbackAfterStartups()` 返回内部可变列表；`getDefaultServerConfig()` 返回共享可变单例 | `.../MRestServer.java:386-388`；`.../util/MRestUtils.java:82-95` |
| — | `$flushed` 非 volatile 却在锁外读；ThreadLocal 上下文在 handler 卸载线程后为 null（NPE） | `.../MRestResponse.java:36,275-282,347`；`.../global/SharedObjects.java:17` |

### 1.5 已核验"非缺陷"（避免误改）

- `fileUpload.get()` 在 Netty 4.1.73 返回 `byte[]`，`IOUtils.write(fileUpload.get(), tmpFile)` 绑定正确（非序列化 bug）。
- 每连接创建的 `webSocketServerHandshakerMap`/`handshakerFactoryMap` 非跨线程共享（约 155-156 行的无锁 `get` 非并发 bug，但无有效缓存）。
- `MRestFilterChainOfDefault.doFilter` 的 `synchronized` 多余但无害（链为每请求）。
- `urlMappingHandler`/`filterMap` 在事件循环启动前安全发布，普通 `HashMap` 读取安全。

---

## 2. 优化方向与推荐顺序

1. **Phase 0 · 1.8.0 安全与正确性批次**（纯 JDK8 代码，可前向合并）
   - 在 `parseHttpRequest` 建立**单一 URL 归一化/解码入口**，从根因上同时解决 A / C / D。
   - 修复 B（Zip Slip）、K（multipart）、F（servletMap 并发）、J（响应头语义）、H（下载状态码）。
2. **Phase 1 · 1.8.0 生命周期**
   - EventLoopGroup 字段化 + 优雅关闭 + `ChannelGroup` 管理子通道 + 可关闭的静态资源刷新线程。
   - 这是**可测试性与嵌入式使用的前提**（当前每次 start/stop 都泄漏线程池）。
3. **Phase 2 · 两分支 pom（版本统一放父 pom 属性）**
   - 依赖现代化：Netty 至少升到 4.1.139（或规划 4.2）、Jackson/gson/logback 升级、`netty-all` 拆分为按需模块、jjwt 0.9.1 → 0.11.x（api/impl/jackson）。
   - **升级 jjwt 是 JDK11 可移植性最大收益点**：可移除仅为兼容而加的 `jaxb-api/jaxb-impl/jaxb-core/activation` shim。
4. **Phase 3 · 2.4.0**
   - SSL 策略：`bcpkix-jdk18on` 一份产物兼容 JDK8/11+ 作为可选框架依赖；`SelfSignedCertificate` 构建后 `delete()` 临时文件；`maven-compiler` 改用 `<release>`；规划移除 `SecurityManager`。
5. **Phase 4 · 2.4.0 及之后（性能架构）**
   - 预编译路由/过滤器表（Pattern 注册期编译）、servlet 分发 trie、静态资源 LRU + HTTP 缓存 + 流式、可选业务线程池（当前磁盘 IO/Jackson/上传拷贝均跑在 Netty worker 上）。

### JDK8 与 JDK11 相关性小结

- **JDK 敏感**：`SelfSignedCertificate` 直连 `sun.security.x509`（JDK11 仅告警可用，**JDK16+ 硬失败**，且框架 pom 未带 bcpkix）；`jaxb/activation` shim（JDK11 专属，升级 jjwt 后可删）；`MRestThreadFactory` 的 `SecurityManager`（JDK17+ 弃用）；`<release>` vs `<source>/<target>`。
- **JDK 无关**：A–H、J、K 及 Netty/Jackson 等 CVE —— 两分支须一致修复，建议在 1.8.0 一次修好再前向合并。

---

## 3. 依赖升级清单（CVE / 维护状态）

| 依赖 | 当前 | 风险要点 | 建议 |
|------|------|----------|------|
| io.netty:netty-all | 4.1.73.Final | CVE-2022-24823（multipart 临时文件）、CVE-2022-41915（头校验/响应拆分）、CVE-2023-34462、CVE-2023-44487、CVE-2024-29025、CVE-2024-47535、CVE-2025-24970 等；4.1 线 2027-07-01 EOL | 至少 **4.1.139.Final**；中期规划 4.2.x；`netty-bom` + 按需模块替代 `netty-all` |
| io.jsonwebtoken:jjwt | 0.9.1 | 已弃用（<0.10.0 有 CVE-2024-31033，维护者曾申辩），且拖入旧 Jackson | **0.12.7 / 0.13.0**（api/impl/jackson 拆分） |
| com.google.code.gson:gson | 2.8.5 | CVE-2022-25647（DoS） | 2.8.9+，建议 2.13.x |
| jackson-databind | 2.11.3 | CVE-2020-36518、CVE-2022-42003/42004、CVE-2026-54512/54513 等 | 2.18.x 最新补丁（或 2.19/2.21） |
| logback-core/classic | 1.2.3 | CVE-2021-42550、CVE-2023-6378、CVE-2023-6481；1.2/1.3/1.4 均 EOL | 1.5.x（最低 1.2.13） |
| org.slf4j:slf4j-api | 1.7.25 | 1.7.x EOL（api 本身低风险） | 2.0.x（注意类加载变化） |
| org.bouncycastle:bcpkix-jdk15on | 1.56 | jdk15on 自 1.71 EOL；CVE-2025-8916 | **bcpkix-jdk18on 1.86**（或 jdk15to18） |
| org.jsoup:jsoup | 1.11.3 | CVE-2021-37714、CVE-2022-36033、CVE-2026-75140/71497 | 1.23.x |
| org.ow2.asm | 7.0 | 无 CVE，但仅支持到 Java 11 class 文件 | 9.8+ |
| jaxb-api/impl/core + activation | 2.3.0 / 1.1.1 | 无 CVE；`javax` 命名空间已被 Jakarta 9+ 取代 | 用 `jakarta.*` 或 glassfish `jaxb-runtime`；升级 jjwt 后或可移除 |
| junit | 4.13.1 | 4.13.1 已修 CVE-2020-15250；JUnit4 维护模式 | 迁移 JUnit 5 |
| webjar-jquery | 3.5.1 | 已含 CVE-2020-11022/11023 修复，无高危 | 可选升 3.7.1 |

---

## 4. 待办事项（TODO）

> 交付顺序遵循"先在 1.8.0 修复 → 前向并入 2.4.0"。每项建议附带回归测试。

### Phase 0 · 安全与正确性（1.8.0，纯 JDK8 代码）

- [ ] **P0-1（A/D/C）** 在 `MRestServerChannelHandler.parseHttpRequest` 建立唯一的 URL 解码+归一化入口（消除重复编码/解码差异）
- [ ] **P0-2（A）** `StaticResourceFinder.loadResourceFromDiskpath` 改用 canonical path 包含校验 + 分隔符边界；拒绝 `..`
- [ ] **P0-3（C）** classpath 资源路径统一 `..` 拒绝，并校验解析结果仍在配置前缀内
- [ ] **P0-4（D）** context 匹配改用已解码 `path()`，最长前缀 + 边界匹配，注册期排序保证确定性；URL 与 rawQuery 分离
- [ ] **P0-5（B）** `FileUtils.unzip` 增加 canonical 包含校验、跳过危险条目、条目数/总量上限；若确认无调用方考虑废弃 API
- [ ] **P0-6（K）** multipart 属性快照为 String 名值对；`try/finally` 确保 `decoder.destroy()`；迁移到 `HttpPostMultipartRequestDecoder`
- [ ] **P0-7（F）** `servletHandlerMap` 改 `ConcurrentHashMap` + `computeIfAbsent`；`init()` 改一次性 volatile 检查
- [ ] **P0-8（J）** 引入真正的 `set`(替换) 与 `add` 语义区分（`Set-Cookie` 保留 add）；合并头时去重单值头
- [ ] **P0-9（H）** `writeFile(status,...)` 状态码透传到 `MResponseHelper.write`
- [ ] **P0-10** 为以上各项补充回归测试（路径穿越、非根 context 路由、multipart 字段、并发 servlet 分发、响应头）

### Phase 1 · 生命周期（1.8.0）

- [ ] **P1-1（E）** `MRestServer` 持有 `bossGroup/workerGroup` 字段；`shutdown()` 优雅关闭并 `sync()`
- [ ] **P1-2（E）** 使用 `ChannelGroup` 跟踪并关闭子通道（含 WebSocket）
- [ ] **P1-3（E）** 静态资源刷新改用受生命周期管理的守护 `ScheduledExecutorService`，可取消；删除死代码 `syncThread`
- [ ] **P1-4** 增加 start/stop 往返的测试，验证无线程池/文件句柄泄漏

### Phase 2 · 依赖现代化（两分支 pom，父 pom 属性统一）

- [ ] **P2-1** Netty 升级至 ≥ 4.1.139；拆分 `netty-all` 为按需模块 + `netty-bom`
- [ ] **P2-2** jackson-databind、gson、logback、slf4j、jsoup、asm 升级到受支持版本
- [ ] **P2-3** jjwt 0.9.1 → 0.12.x/0.13.x（api/impl/jackson）；同步改 `masker-rest-jjwt`
- [ ] **P2-4** 升级 jjwt 后移除 `jaxb-api/jaxb-impl/jaxb-core/activation` shim（若不再被依赖）
- [ ] **P2-5** 校验版本属性集中在父 pom，确保两分支仅差 `jdk.compiler.version`

### Phase 3 · 2.4.0 / JDK11 可移植性

- [ ] **P3-1** `SelfSignedCertificate` 构建后 `delete()` 临时文件
- [ ] **P3-2** 引入 `bcpkix-jdk18on` 为可选框架依赖（兼容 8/11+），或对 sslEnabled 无显式 `SslContext` 时 `fail fast` 并给出清晰提示
- [ ] **P3-3** `maven-compiler` 改用 `<release>`
- [ ] **P3-4** 规划 `MRestThreadFactory` 中 `SecurityManager` 的移除（JDK17+ 弃用）

### Phase 4 · 性能架构（2.4.0 及之后）

- [ ] **P4-1** 注册期预编译 filter/URL Pattern，缓存不可变过滤链（消除每请求重编译）
- [ ] **P4-2** servlet 分发改 trie/索引结构，消除每请求线性扫描与模型构造
- [ ] **P4-3** 静态资源：有界 LRU（含负缓存 TTL）、按键加载（去掉全局写锁 IO）、`ChunkedFile` 流式、ETag/Last-Modified/Cache-Control/304
- [ ] **P4-4** 引入可选业务线程池，避免阻塞 IO/CPU 占用 Netty event loop
- [ ] **P4-5** 请求体限制：可配置聚合上限、multipart 文件数/总量上限

### Phase 5 · 健壮性与安全加固（可穿插）

- [ ] **P5-1** WebSocket：`channelInactive` 中触发 `inactiveCallback` 与清理；handshaker 缓存改并发安全；可选 Origin 校验；空闲超时
- [ ] **P5-2** 增加 `IdleStateHandler`/读超时；指纹头/cookie 可选关闭并加固（HttpOnly/Secure/SameSite）
- [ ] **P5-3** 修 `MRestDispatchServlet` 405 分支；`application/x-www-form-urlencoded` body 正确解码
- [ ] **P5-4** 修 `Low` 级项：`IOUtils.copy` 循环条件与缓冲区、`MimetypesFileTypeMap` 缓存、`Locale.ROOT`、`UrlPatternModel.getRegularCount`、`FileUtils` NPE/TOCTOU、前缀扫描顺序、ASM `static` 方法处理、`remoteAddress` 语义、可变返回值、`$flushed` volatile / ThreadLocal 上下文传播

---

## 附录：核验修正记录（避免误判）

- **B**：仓库内无调用方，属公共工具 API 的潜在陷阱（非直接服务端可利用）。
- **C**：标准 JDK 加载器可阻止逃出 classpath 根（fat-jar 免疫）；真实范围是目录部署的前缀逃逸。
- **G**：`RandomAccessFile` 在正常/失败路径有 close，泄漏仅存在于 open 与 listener 注册之间的窄窗口。
- **I**：临时文件有 `deleteOnExit` 注册；JDK11 默认可用，硬失败点在 JDK16+；框架 pom 确实缺少 bcpkix。
- **E**：静态资源刷新线程为可选（两个开关默认 false），开启后才不可停。
