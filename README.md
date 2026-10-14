# WowsApi — 战舰世界 public 接口开发包

> **JDK 21+** · **Jackson 3** · 零 HTTP 第三方依赖（纯 JDK `HttpClient`）
>
> 支持服务器：国服 / 亚服 / 欧服 / 美服 / 莱服（korabli.su）

---

## 3.0 

- **实例即服务器**：`WowsApi.of(server)` 创建实例
- **端点注册表 + 唯一管线**：所有请求经 `HttpPipeline` 统一出口（兼容性检查 → 凭证解析 → 主机策略 → 发送 → 解码 → 解析），
  URL / 参数 / 认证槽位 / 解析器 / 监控键全部来自 `Endpoint` 定义，无散落的 `URI.create`；
- **凭证**：`ApplicationId`（服务器级）/ `AccessToken`、`ShareToken`（账号级，按 accountId 精确匹配）/ 预留 `CookieCredential`；
  缺失必填凭证**发送前 fast-fail**（`MissingCredentialException`）；
- **视图按通道分区**：`api.clans()` / `api.developers()`——**1 调用 = 1 请求**；
  多请求编排显式走 `aggregate` 层（`AccountAssembler`）与显式标注的方法（`ladder` 内部分页）；
- **官方开发者API 全量覆盖（29 个接口）**：`api.developers()` 覆盖官方 5 个分区 ——
  Account（详情/成就）、Encyclopedia（舰船/参数/模块/指挥官/成就/收藏/地图等 14 个）、Warships（统计/徽章）、Seasons（排位赛季/单船/玩家）、Clans（军团/军团战赛季）。
  带文本的接口默认 `language=zh-cn`，均有显式语言重载：

```java
var dev = api.developers();
var yamato = dev.encyclopediaShip(4276041424L);   // 舰船字典条目（含 default_profile 全参数）
var profile = dev.shipProfile(4276041424L);       // 舰船参数明细（装甲/主炮/机动/隐蔽…）
var seasons = dev.rankedSeasons();                // 排位赛季（含联赛/段位）
var ranked = dev.rankedAccountStats(accountId);   // 排位战绩（段位进度 + 各赛季统计）
```

## Maven 坐标

目前只发布在 GitHub 这边，支持 JDK21+：

```xml
<dependency>
    <groupId>com.shinoaki.wows.api</groupId>
    <artifactId>wows-api</artifactId>
    <version>Latest Version</version>
</dependency>
```

---

### 错误处理

```java
import com.shinoaki.wows.api.error.*;

try {
    api.developers().user(accountId);
} catch (HttpStatusException e) {          // 非 200 / 重定向超限；带 statusCode
    System.out.println(e.getStatusCode());
} catch (MissingCredentialException e) {   // 必填凭证缺失（发送前 fast-fail）
    System.out.println(e.getEndpointId() + " 缺少 " + e.getKind());
} catch (UnsupportedOnServerException e) { // 端点/类型在当前服不支持
    System.out.println(e.getMessage());
} catch (BasicException e) {               // 其余（网络/解析/业务状态/HIDDEN…）
    System.out.println(e.getCode() + " " + e.getMessage());
}
```

异常分层（全部继承 `BasicException`，旧写法 `catch (BasicException)` 依然有效）：

| 异常 | 含义 |
|---|---|
| `HttpNetworkException` | 网络传输失败（连接/超时/解压） |
| `HttpStatusException` | HTTP 状态码异常（非 200/重定向超限），带 `getStatusCode()` |
| `ResponseParseException` | JSON 非法 / 字段解析失败（消息带响应摘要） |
| `MissingCredentialException` | 必填凭证缺失（发送前 fast-fail） |
| `UnsupportedOnServerException` | 端点/类型在当前服务器不支持 |
| `HiddenProfileException` | 账号隐藏战绩 |
| `BasicException`（基类） | 业务状态异常（`status≠ok`、数据缺失）等 |

原则：**同种失败 = 同种异常；"空结果"只表示服务器明确返回了空**（如 `TOLERANT` 容错才会返回空表，且记日志）。

### 超时与代理

```java
import java.net.InetSocketAddress;
import java.net.ProxySelector;

WowsApi api = WowsApi.of(WowsServer.RU)
        .timeout(20)                                                       // 请求超时（秒）
        .proxy(ProxySelector.of(new InetSocketAddress("127.0.0.1", 7890))) // 代理；null = 直连
        .credentials(c -> c.applicationId("..."));
```

两者作用于**进程级共享的传输配置**（`HttpClientProvider` 持有的共享 `HttpClient`），在任意实例上设置一次即可，对之后的所有请求生效。


## 关于异步

本SDK**只提供同步方法**（直接返回结果，异常以 `BasicException` 抛出），不提供 `xxxAsync` 异步方法。
需要异步时请在调用方自行包装（线程池或 JDK21 虚拟线程）：

```java
// 线程池
ExecutorService executor = Executors.newFixedThreadPool(8);
CompletableFuture<DevelopersUserInfo> future = CompletableFuture.supplyAsync(() -> {
    try {
        return api.developers().user(accountId);
    } catch (BasicException e) {
        throw new RuntimeException(e);
    }
}, executor);

// 虚拟线程（JDK21+）
try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
    var task = executor.submit(() -> api.developers().user(accountId));
    var data = task.get();
}
```

## 基于此SDK开发的平台接口

`https://v3-api.wows.shinoaki.com/v3/index.html`
