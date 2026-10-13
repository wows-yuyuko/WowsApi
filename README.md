# WowsApi — 战舰世界 public / vortex-api 接口开发包

> **JDK 21+** · **Jackson 3** · 零 HTTP 第三方依赖（纯 JDK `HttpClient`） ·
> 支持服务器：国服 / 亚服 / 欧服 / 美服 / 莱服（korabli.su）

本库封装《战舰世界》两条数据通道，全部只提供**同步方法**（异常以 `BasicException` 抛出）：

| 通道 | 主机 | 覆盖 |
|---|---|---|
| vortex | `vortex.*` / `clans.*` | 玩家档案、战舰战绩、军团详情/成员/排行、箱子与资源 |
| 官方开发者API | `api.*`（WG 服 `/wows/`、莱服 `/mk/`） | 账号、战舰统计、军团、词典（**国服无此通道**） |

> **3.0 重写进行中**：请求层已整体重写（见下节架构与教程），account 域（玩家）已全量迁入；
> 其余端点在 Phase 2 逐步迁入，迁移期**旧写法仍可用**（5 个旧 Tools 类已标记 `@Deprecated`）——
> 旧版用法、2.x 版本历史与「旧 → 新」映射表见 [`docs/3.0之前-用法说明.md`](docs/3.0之前-用法说明.md)。

---

## 3.0 架构（一分钟了解）

- **实例即服务器**：`WowsApi.of(server)` 创建实例，凭证表随实例走——跨服串用结构上不可能；
- **端点注册表 + 唯一管线**：所有请求经 `HttpPipeline` 统一出口（兼容性检查 → 凭证解析 → 主机策略 → 发送 → 解码 → 解析），
  URL / 参数 / 认证槽位 / 解析器 / 监控键全部来自 `Endpoint` 定义，无散落的 `URI.create`；
- **凭证一等公民**：`ApplicationId`（服务器级）/ `AccessToken`、`ShareToken`（账号级，按 accountId 精确匹配）/ 预留 `CookieCredential`；
  缺失必填凭证**发送前 fast-fail**（`MissingCredentialException`，零网络请求）；
- **视图按通道分区**：`api.vortex()` / `api.developers()`——**1 调用 = 1 请求**；多请求编排显式走 `aggregate` 层；
- **服务器差异数据化**：`ServerProfile` 承载主机、`/mk/` 前缀（按域）、`extra` 串、CN 无 dev API 等事实，端点定义零分支。

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

## 使用教程

### 1. 创建实例

```java
import com.shinoaki.wows.api.WowsApi;
import com.shinoaki.wows.api.type.WowsServer;

WowsApi api = WowsApi.of(WowsServer.ASIA);
```

一个实例对应一个服务器；需要查询多个服务器时创建多个实例（凭证互不可见）。

### 2. 配置凭证

```java
import com.shinoaki.wows.api.auth.AccessToken;
import com.shinoaki.wows.api.auth.ShareToken;

long accountId = 2022515210L;

WowsApi api = WowsApi.of(WowsServer.ASIA)
        .credentials(c -> c
                .applicationId("...")              // 官方API 令牌（服务器级，dev 端点必填）
                .accessToken(accountId, "...")     // OAuth 用户态令牌（账号级，解锁 private.* 私有数据，可选）
                .shareToken(accountId, "..."));    // vortex 分享凭证（账号级，只对该账号有效，可选）
```

规则速记：

- **按账号精确命中**：账号级凭证只在「请求账号 == 凭证绑定账号」时注入；请求不带账号的端点（搜索、军团详情等）不注入；
- **不可变**：`credentials(...)` 返回**新实例**，原实例不受影响；同 kind + 同账号重复配置 = 覆盖；
- **调用级覆盖（逃生门）**：单次调用可直接传凭证对象，优先于实例配置——
  `api.vortex().user(accountId, ShareToken.of(accountId, "..."))`。

### 3. 查询玩家（vortex 通道）

```java
import com.shinoaki.wows.api.endpoint.RateLimitPolicy;
import com.shinoaki.wows.api.vortex.account.VortexSearchUser;
import com.shinoaki.wows.api.vortex.account.VortexUserInfo;

VortexUserInfo user = api.vortex().user(accountId);          // 玩家详情（1 请求）
List<VortexSearchUser> users = api.vortex().search("JustOn"); // 玩家搜索（1 请求）

// 国服搜索接口对高频请求返回 503——默认 STRICT 抛异常；需要容错时显式传 TOLERANT：
List<VortexSearchUser> tolerant = api.vortex().search("西行寺", RateLimitPolicy.TOLERANT);
```

### 4. 查询玩家与军团（官方开发者API 通道）

```java
import com.shinoaki.wows.api.developers.account.DevelopersSearchUser;
import com.shinoaki.wows.api.developers.account.DevelopersUserInfo;
import com.shinoaki.wows.api.developers.clan.DevelopersClanInfo;
import com.shinoaki.wows.api.developers.clan.DevelopersSearchUserClan;

DevelopersUserInfo info = api.developers().user(accountId);       // 玩家详情（1 请求）
List<DevelopersSearchUser> found = api.developers().search("JustOn");
DevelopersSearchUserClan clanRef = api.developers().accountClan(accountId);  // 玩家所属军团
DevelopersClanInfo clanInfo = api.developers().clanInfo(clanRef.clan_id());  // 军团详情（含成员列表）
```

- 国服调用 dev 通道会直接抛 `UnsupportedOnServerException`（发送前，零网络请求）；
- 莱服的 `/mk/` 前缀由 `ServerProfile` 自动注入（account / ships / glossary 走 `/mk/`，clans 系列仍在 `/wows/`，无需手工拼接）；
- `extra` 串按服自动选择（莱服没有 `statistics.clan`）。

### 5. 多请求聚合（显式 opt-in）

视图层保持「1 调用 = 1 请求」；需要「玩家详情 + 所属军团 + 军团详情」三请求编排时：

```java
import com.shinoaki.wows.api.aggregate.AccountAssembler;
import com.shinoaki.wows.api.data.AccountInfo;

AccountInfo account = AccountAssembler.of(api).info(accountId);
```

语义：任一步失败整体失败（不吞异常、不降级）；玩家无军团时自动省略第 3 个请求。

### 6. 错误处理

```java
import com.shinoaki.wows.api.error.*;

try {
    api.vortex().user(accountId);
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

### 7. 超时与代理

```java
import java.net.InetSocketAddress;
import java.net.ProxySelector;

WowsApi api = WowsApi.of(WowsServer.RU)
        .timeout(20)                                                    // 请求超时（秒）
        .proxy(ProxySelector.of(new InetSocketAddress("127.0.0.1", 7890))) // 代理；null = 直连
        .credentials(c -> c.applicationId("..."));
```

两者作用于**进程级共享的传输配置**（`HttpClientProvider` 持有的共享 `HttpClient`），
在任意实例上设置一次即可，对之后的所有请求生效。

### 8. 完整示例

```java
import com.shinoaki.wows.api.WowsApi;
import com.shinoaki.wows.api.aggregate.AccountAssembler;
import com.shinoaki.wows.api.data.AccountInfo;
import com.shinoaki.wows.api.type.WowsServer;

public class Demo {
    public static void main(String[] args) throws Exception {
        long accountId = 2022515210L;
        WowsApi api = WowsApi.of(WowsServer.ASIA)
                .timeout(20)
                .credentials(c -> c
                        .applicationId(System.getenv("WOWS_APP_ID"))
                        .shareToken(accountId, System.getenv("WOWS_VORTEX_TOKEN")));

        System.out.println("vortex 详情   = " + api.vortex().user(accountId).name());
        System.out.println("vortex 搜索   = " + api.vortex().search("JustOn").size() + " 条");
        System.out.println("dev 玩家详情  = " + api.developers().user(accountId).nickname());
        AccountInfo info = AccountAssembler.of(api).info(accountId);
        System.out.println("聚合档案      = " + info.nickname() + " / 军团=" + info.clan().tag());
    }
}
```

### 9. 当前覆盖范围与迁移状态

| 域 | 3.0 状态 |
|---|---|
| 玩家：vortex 搜索 / 详情；dev 搜索 / 详情 / 所属军团 / 军团详情 + 聚合 | ✅ 已迁入（上例可直接跑） |
| 战舰、军团 vortex、箱子/资源/词典、军团排行 | Phase 2 迁入中——暂用旧写法，见 [`docs/3.0之前-用法说明.md`](docs/3.0之前-用法说明.md) |

---

## 请求通道与传输行为

3.0 中一切请求经 `HttpPipeline` 路由到三个通道（`Channel`：vortex / clans / dev），主机由 `ServerProfile` 注入：

- **Cookie 管理 + 重定向跟随按主机开启**，默认仅注册 `api.korabli.su`（莱服官方API，存在 `307 + Set-Cookie: bp_chl=...` 反爬握手）：
  无 cookie 首次请求自动带上下发的 cookie 重放（最多 2 次），cookie 进程内共享、按域名隔离、线程安全；
- **并发冷启动单飞**：同一主机无有效 cookie 时只允许一个线程发起握手，其余线程复用其 cookie，避免并发 307 风暴；
- **429 限流自动退避**：按 `Retry-After` / 指数退避等待后重试（最多 3 次）；
- 其它主机走普通快速通道，单次发送直接返回，**零额外开销**；
- 未来若其它服务器出现同样防护，一行开启：`HostPolicy.enableCookieRedirect("主机")`；
  不需要默认莱服处理时：`HostPolicy.disableCookieRedirect("api.korabli.su")`。

共享 `HttpClient` 由 `HttpClientProvider` 统一持有（不跟随重定向、不挂 CookieHandler——重定向与会话 cookie 由上表机制按主机自行处理），
超时 / 代理集中管理，调用方无需自建 client。

## 序列化兼容性

数据模型大多是 record，Java 原生序列化对 record 的处理与普通类不同（已实测）：

| 变更 | 结果 |
| --- | --- |
| record **加**字段 | ✅ 旧数据可读，新字段取默认值（null/0） |
| record **删**字段 | ✅ 旧数据可读，多余字段被跳过 |
| record **改字段类型** | ❌ `InvalidClassException: incompatible types` |
| 改包名/类名、record↔普通类互转 | ❌ ClassNotFoundException / 不兼容 |

- record 的默认 `serialVersionUID` 恒为 `0L`（不是普通类那种结构哈希）→ **record 不要手写 serialVersionUID**；
- 普通类一旦 `implements Serializable` **必须**手写 serialVersionUID，否则加字段就会让旧数据读不出来；
- `ShipInfo.source` 在早期数据里不存在时统一归一为 `ShipInfoSource.UNKNOWN`（最保守：`hasXxx()` 全为 false），不会 NPE；
- 长期缓存/跨进程传输建议用 JSON（`JsonUtils`，按字段名匹配、缺字段走默认值），Java 原生序列化只当"同版本内短期缓存"；
- 回归测试：`ShipInfoSerializationCompatTest`。

## 关于异步

本SDK**只提供同步方法**（直接返回结果，异常以 `BasicException` 抛出），不提供 `xxxAsync` 异步方法。
需要异步时请在调用方自行包装（线程池或 JDK21 虚拟线程）：

```java
// 线程池
ExecutorService executor = Executors.newFixedThreadPool(8);
CompletableFuture<VortexUserInfo> future = CompletableFuture.supplyAsync(() -> {
    try {
        return api.vortex().user(accountId);
    } catch (BasicException e) {
        throw new RuntimeException(e);
    }
}, executor);

// 虚拟线程（JDK21+）
try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
    var task = executor.submit(() -> api.vortex().user(accountId));
    var data = task.get();
}
```

## 版本与迁移

- **3.0.0（开发中）**：全新请求架构（`WowsApi` 入口 / 端点注册表 / 唯一管线 / 凭证一等公民 / `ServerProfile` 数据化）；
  已完成 account 域迁移；错误语义强化（缺凭证 fast-fail、不支持显式拒绝、限流显式策略）；
  旧入口（5 个 Tools 类）标记 `@Deprecated`，将在断代清理时删除。
- **2.x 版本变更历史**（2.0.3 同步方法 / 2.3.0 Jackson3 / 2.6.0 字段补齐 / 2.7.0 莱服分家 / 2.7.1 箱子字段）
  与旧版用法见 [`docs/3.0之前-用法说明.md`](docs/3.0之前-用法说明.md)。

## 基于此SDK开发的平台接口

`https://v3-api.wows.shinoaki.com:8443/v3/index.html`
