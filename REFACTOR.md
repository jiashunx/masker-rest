# masker-rest 缺陷与优化方向分析（REFACTOR）

> 本文基于对 `1.8.0-SNAPSHOT`（JDK8）分支的文档与源码评审，并经资深评审（@oracle）逐条反汇编核验。
> 目的：梳理现存缺陷、优化方向与可执行待办。
>
> **更新（已实施）**：第 1~3 节的分析结论已按第 4 节逐项落地修复，每个修复均经 `mvn -o test` 验证通过并单独提交推送至 `origin/1.8.0-SNAPSHOT`。
> 最终测试：framework 39 + jjwt 1，BUILD SUCCESS。完成情况与提交见 §4，遗留/差异见 §5。

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

> 说明：本节为**评审时的原始推荐顺序**。实际实施中，安全的代码级项已在 `1.8.0-SNAPSHOT` 分支完成（含原计划在 Phase 4 的性能项），以便前向并入 `2.4.0`。完成状态见 §4。

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

> **实际升级结果**：Netty **4.1.139.Final**；jackson-databind **2.17.2**；gson **2.10.1**；logback **1.2.13**；slf4j-api **1.7.36**；jsoup **1.15.4**；asm/asm-commons/asm-util **9.7**；jjwt **0.12.6**（api/impl/jackson）；bcpkix **jdk18on 1.80**；移除 jaxb-api/impl/core（`activation` 保留）。junit 4 与 webjar-jquery 未升级。

---

## 4. 待办事项与完成状态（TODO）

> 交付顺序遵循"先在 1.8.0 修复 → 前向并入 2.4.0"。每项均已附带回归测试并通过 `mvn -o test`。
> 状态：`[x]` 已完成；`[~]` 部分完成/有差异（见 §5）。

### Phase 0 · 安全与正确性（1.8.0，纯 JDK8 代码）

- [x] **P0-1（A/D/C）** URL 解码+归一化入口 —— `202490f`（改用已解码 `path()`）+ `637ca67`（集中拒绝 `..`）
- [x] **P0-2（A）** 磁盘资源 canonical 包含校验 + 分隔符边界 —— `a80381a`
- [x] **P0-3（C）** classpath 资源 `..` 拒绝 —— `637ca67`
- [x] **P0-4（D）** context 最长前缀+边界匹配、确定性选择、URL/rawQuery 分离 —— `202490f`
- [x] **P0-5（B）** `FileUtils.unzip` canonical 包含校验 + 条目数/总量上限 —— `476b348`
- [x] **P0-6（K）** multipart 属性快照为 String 名值对 + `finally` 销毁解码器 —— `17a9076`（保留 `HttpPostRequestDecoder`，未迁移 `HttpPostMultipartRequestDecoder`）
- [x] **P0-7（F）** `servletHandlerMap` → `ConcurrentHashMap` + `computeIfAbsent`；`init()` 快速路径 —— `bc1d123`
- [x] **P0-8（J）** `set`(替换)/`add` 语义区分 + 合并去重（`Set-Cookie` 保留 add）—— `42e6102`
- [x] **P0-9（H）** 下载状态码透传 + 异常路径句柄释放 —— `4b7119f`
- [x] **P0-10** 回归测试 —— 已随各修复提交（UrlUtils/FileUtils/IOUtils/MRestHeaders/MRestFileUploadRequest/AbstractRestServlet 等）

### Phase 1 · 生命周期（1.8.0）

- [x] **P1-1（E）** `bossGroup/workerGroup` 字段化 + `shutdownGracefully().sync()` —— `3d0d4ee`
- [x] **P1-2（E）** `ChannelGroup` 跟踪并关闭子通道（含 WebSocket）—— `3d0d4ee`
- [x] **P1-3（E）** 静态资源刷新改为可停的守护线程（volatile 标志 + interrupt），删除死代码 `syncThread` —— `3d0d4ee`
- [x] **P1-4** start/stop 往返测试 —— `MRestServerTest`（`3d0d4ee`）

### Phase 2 · 依赖现代化（两分支 pom）

- [~] **P2-1** Netty 升级至 4.1.139.Final —— `5a1a722`（**未拆分** `netty-all`）
- [x] **P2-2** jackson-databind 2.17.2 / gson 2.10.1 / logback 1.2.13 / slf4j 1.7.36 / jsoup 1.15.4 / asm 9.7 —— `ecfd693`
- [x] **P2-3** jjwt → 0.12.6（api/impl/jackson）—— `2b4da1f`
- [x] **P2-4** 移除 jaxb-api/impl/core（`activation` 保留，`MimetypesFileTypeMap` 需要）—— `a13a529`
- [~] **P2-5** 版本仍集中于父 pom `dependencyManagement`（未改用 `<properties>` 占位）

### Phase 3 · 2.4.0 / JDK11 可移植性

- [x] **P3-1** `SelfSignedCertificate` 构建后 `delete()` 临时文件 —— `54c4ab2`
- [x] **P3-2** `bcpkix-jdk18on` 作为可选框架依赖 + 失败时清晰提示 —— `54c4ab2`
- [~] **P3-3** `<release>` 编译 —— **未在 1.8.0 改动**（JDK8 构建；属 2.4.0/JDK11 分支）
- [x] **P3-4** 移除 `MRestThreadFactory` 对 `SecurityManager` 的依赖 —— `54c4ab2`

### Phase 4 · 性能架构

- [x] **P4-1** filter URL Pattern 注册期预编译 + 字面量转义 —— `4457132`
- [x] **P4-2** servlet 路由复用预计算模型 + 命中结果有界 LRU 缓存 —— `160b16c` + `a34b486`
- [x] **P4-3** 静态资源：不缓存未命中 + 有界 LRU + ETag/304 + 磁盘文件流式输出 —— `f9a8902`/`a20217a`/`d5e7f10`/`9d321f3`
- [x] **P4-4** 可选独立业务线程池（`businessThreadNum`，默认关闭）—— `5951dad`
- [x] **P4-5** multipart 文件数量上限 + 畸形请求 400 —— `3fb4020`

### Phase 5 · 健壮性与安全加固

- [x] **P5-1** WebSocket `channelInactive` 清理 + handshaker 缓存并发安全 —— `a626920`（Origin 校验未做）
- [~] **P5-2** 可选空闲超时 + 指纹头开关 —— `43b4cec`（超时默认关闭；cookie 未加 HttpOnly/Secure/SameSite）
- [x] **P5-3** 表单体 percent/`+` 解码 + 405 分支 —— `a626920` + `fe61592`
- [x] **P5-4** Low 级项：`IOUtils.copy`、Mimetype 缓存、`Locale.ROOT`、`getRegularCount`、`FileUtils` NPE、前缀扫描顺序、ASM 静态方法校验、可变返回值副本、`$flushed` volatile、forward 递归保护、ThreadLocal 显式传播 —— `5bc65dd`/`af347f8`/`6402eea`/`0f16184`/`54dc3f5`

### 其他（评审补充项）

- [x] **G** HTTPS 下载改用 `ChunkedFile` + `Content-Disposition` 文件名转义 —— `4a84780`

---

## 5. 遗留与差异说明

以下项为**有意保留/延后**或与原文建议存在差异，需后续评估：

- **依赖拆分**：Netty 已升至 4.1.139.Final，但仍使用 `netty-all` 聚合依赖，未拆分为按需模块 + `netty-bom`。
- **`<release>` 编译**：仅适用于 2.4.0/JDK11 分支；1.8.0 保持 `source/target=8`（JDK8 构建），未改动。
- **P2-5**：依赖版本仍集中在父 pom `dependencyManagement`（未改用 `<properties>` 占位）。
- **WebSocket Origin 校验**（CSWSH）未实现。
- **空闲超时**：`httpIdleTimeoutSeconds` 默认 `0`（关闭），需显式配置启用；指纹标识 cookie 未加 `HttpOnly/Secure/SameSite`。
- **ASM**：仅增加 `public && !static` 映射方法校验；未扫描继承的注解方法。
- **`remoteAddress` 语义**：保持原行为（未交换 `local/remote`），以免破坏既有调用方语义。
- **Low 项未做**：`MResponseHelper.writeJSON(String)` 仍用平台默认字符集且未补 JSON Content-Type；`FileUtils.newFile/newDirectory` 的 TOCTOU 未处理（仅修 NPE）。
- **multipart**：保留 `HttpPostRequestDecoder`（未迁移 `HttpPostMultipartRequestDecoder`），仅修复 use-after-destroy 与销毁时机。

**兼容性注意**：
- **jjwt 0.12.6 迁移破坏了令牌兼容** —— HMAC 密钥改为对原始密钥做 SHA-256 派生（32 字节），旧版 0.9.1 签发的 token 不再通过校验，升级前需评估存量 token 过渡方案。
- 新增配置默认保持向后兼容：`httpIdleTimeoutSeconds=0`、`businessThreadNum=0`、`fingerprintHeadersEnabled=true`、`multipartMaxFileCount=1000`。

**验证环境**：本机仅 JDK11；`source/target=8` 由 JDK11 编译验证通过（`mvn -o test`）。

---

## 附录：核验修正记录（避免误判）

- **B**：仓库内无调用方，属公共工具 API 的潜在陷阱（非直接服务端可利用）。
- **C**：标准 JDK 加载器可阻止逃出 classpath 根（fat-jar 免疫）；真实范围是目录部署的前缀逃逸。
- **G**：`RandomAccessFile` 在正常/失败路径有 close，泄漏仅存在于 open 与 listener 注册之间的窄窗口。
- **I**：临时文件有 `deleteOnExit` 注册；JDK11 默认可用，硬失败点在 JDK16+；框架 pom 确实缺少 bcpkix。
- **E**：静态资源刷新线程为可选（两个开关默认 false），开启后才不可停。
