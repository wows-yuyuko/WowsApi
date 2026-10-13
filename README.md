# WowsApi — 战舰世界 public / vortex-api 接口开发包

> **JDK 21+** · **Jackson 3** · 零 HTTP 第三方依赖（纯 JDK `HttpClient`）
>
> 支持服务器：国服 / 亚服 / 欧服 / 美服 / 莱服（korabli.su）

本库封装《战舰世界》两条数据通道，全部只提供**同步方法**（异常以 `BasicException` 抛出）：

| 通道 | 主机 | 覆盖 |
|---|---|---|
| vortex | `vortex.*` / `clans.*` | 玩家档案、战舰战绩、军团详情/成员/排行、箱子与资源 |
| 官方开发者API | `api.*`（WG 服 `/wows/`、莱服 `/mk/`） | 账号、战舰统计、军团、词典（**国服无此通道**） |

> **3.0 重写**：请求层已整体重写，**全部 22 个端点**（玩家 / 战舰 / 军团 / 排行 / 箱子 / 资源 / 词典）都已迁入新架构；
> 旧写法（`WowsHttp*Tools`，5 个类已标记 `@Deprecated`）在迁移期仍可用，Phase 3 断代清理时删除——
> 旧版用法、2.x 版本历史与「旧 → 新」映射表见 [`docs/3.0之前-用法说明.md`](docs/3.0之前-用法说明.md)。

---

## 3.0 架构（一分钟了解）

- **实例即服务器**：`WowsApi.of(server)` 创建实例，凭证表随实例走——跨服串用结构上不可能；
- **端点注册表 + 唯一管线**：所有请求经 `HttpPipeline` 统一出口（兼容性检查 → 凭证解析 → 主机策略 → 发送 → 解码 → 解析），
  URL / 参数 / 认证槽位 / 解析器 / 监控键全部来自 `Endpoint` 定义，无散落的 `URI.create`；
- **凭证一等公民**：`ApplicationId`（服务器级）/ `AccessToken`、`ShareToken`（账号级，按 accountId 精确匹配）/ 预留 `CookieCredential`；
  缺失必填凭证**发送前 fast-fail**（`MissingCredentialException`，零网络请求）；
- **视图按通道分区**：`api.vortex()` / `api.clans()` / `api.developers()` / `api.resources()`——**1 调用 = 1 请求**；
  多请求编排显式走 `aggregate` 层（`AccountAssembler`）与显式标注的方法（`ladder` 内部分页）；
- **服务器差异数据化**：`ServerProfile` 承载主机、`/mk/` 前缀（按域）、`extra` 串、排行 realm、CN 无 dev API 等事实，端点定义零分支。

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
  `api.vortex().user(accountId, ShareToken.of(accountId, "..."))`、`api.developers().user(accountId, AccessToken.of(accountId, "..."))`。

### 3. 玩家（vortex 通道）

```java
import com.shinoaki.wows.api.endpoint.RateLimitPolicy;
import com.shinoaki.wows.api.vortex.account.VortexSearchUser;
import com.shinoaki.wows.api.vortex.account.VortexUserInfo;

VortexUserInfo user = api.vortex().user(accountId);          // 玩家详情（1 请求）
List<VortexSearchUser> users = api.vortex().search("JustOn"); // 玩家搜索（1 请求）

// 国服搜索接口对高频请求返回 503——默认 STRICT 抛异常；需要容错时显式传 TOLERANT：
List<VortexSearchUser> tolerant = api.vortex().search("西行寺", RateLimitPolicy.TOLERANT);
```

### 4. 战舰

```java
import com.shinoaki.wows.api.type.WowsBattlesType;
import com.shinoaki.wows.api.vortex.VortexUserShip;
import com.shinoaki.wows.api.developers.DevelopersUserShip;
import com.shinoaki.wows.api.developers.DevelopersUserShipOper;

// vortex：某战斗类型的完整战舰战绩列表（1 请求）
VortexUserShip ships = api.vortex().ships(accountId, WowsBattlesType.PVP);
// 莱服专属类型（brawl / clan / rating* 等）仅 RU 可用，其余服调用抛 UnsupportedOnServerException

// 官方API：战舰统计（历史集合，1 请求） / 全量战绩（14 类块，1 请求） / 徽章（1 请求）
DevelopersUserShip stats = api.developers().ships(accountId);
DevelopersUserShipOper oper = api.developers().shipsOper(accountId);
Map<Long, ShipExpansion> badges = api.developers().shipBadges(accountId);  // 莱服不支持（显式拒绝）
```

### 5. 军团与排行（clans 通道）

```java
import com.shinoaki.wows.api.vortex.clan.account.VortexSearchClanUser;
import com.shinoaki.wows.api.vortex.clan.base.VortexClanInfo;
import com.shinoaki.wows.api.vortex.clan.members.VortexClanStatisticsInfo;

VortexSearchClanUser myClan = api.clans().accountClan(accountId);   // 玩家所属军团（1 请求）
VortexClanInfo clan = api.clans().clanInfo(clanId);                 // 军团详情（1 请求）
VortexClanStatisticsInfo members = api.clans().members(clanId);     // 成员战绩（默认 pvp，1 请求）
members = api.clans().members(clanId, "cvc", 35);                   // 指定类型 / 赛季
var found = api.clans().search("YU_RI");                            // 军团搜索（1 请求）

// 军团排行：season<=0 = 最新赛季；ladder 为整季拉取（内部游标分页，多请求），单页用 ladderPage
var ladder = api.clans().ladder(0);
var page = api.clans().ladderPage(0, null);            // 第一页
var next = api.clans().ladderPage(0, page.getLast().id());  // 游标翻页
```

### 6. 资源 / 箱子 / 词典（graphql，POST）

```java
import com.shinoaki.wows.api.vortex.VortexResourcesLanguage;
import com.shinoaki.wows.api.vortex.resources.box.WowsLootBox;

WowsLootBox box = api.resources().lootbox(lootboxId);                  // 箱子详情（1 请求；默认语言：莱服 ru、其余 zh-sg）
var boxes = api.resources().lootboxes(VortexResourcesLanguage.SG);     // 箱子列表（1 请求）
var items = api.resources().items(VortexResourcesLanguage.SG);         // 资源列表（1 请求）
var glossary = api.resources().glossary(VortexResourcesLanguage.SG);   // 资源索引（词典+船+成就，1 请求）

// 测试服（PT）：vortex 主机切到 PT（其余不变）
WowsApi pt = WowsApi.of(WowsServer.ASIA).pt();
```

### 7. 官方API 军团与词典

```java
var clans = api.developers().clanSearch("YU_RI");           // 军团搜索（1 请求）
var seasons = api.developers().clanSeason();                // 赛季/联赛（1 请求；语言默认 zh-cn，可传参覆盖）
var clanGlossary = api.developers().glossary();             // 军团词典（1 请求）
```

### 8. 多请求聚合（显式 opt-in）

视图层保持「1 调用 = 1 请求」；需要「玩家详情 + 所属军团 + 军团详情」三请求编排时：

```java
import com.shinoaki.wows.api.aggregate.AccountAssembler;
import com.shinoaki.wows.api.data.AccountInfo;

AccountInfo account = AccountAssembler.of(api).info(accountId);
```

语义：任一步失败整体失败（不吞异常、不降级）；玩家无军团时自动省略第 3 个请求。

### 9. 错误处理

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

### 10. 超时与代理

```java
import java.net.InetSocketAddress;
import java.net.ProxySelector;

WowsApi api = WowsApi.of(WowsServer.RU)
        .timeout(20)                                                       // 请求超时（秒）
        .proxy(ProxySelector.of(new InetSocketAddress("127.0.0.1", 7890))) // 代理；null = 直连
        .credentials(c -> c.applicationId("..."));
```

两者作用于**进程级共享的传输配置**（`HttpClientProvider` 持有的共享 `HttpClient`），在任意实例上设置一次即可，对之后的所有请求生效。

### 11. 完整示例

```java
import com.shinoaki.wows.api.WowsApi;
import com.shinoaki.wows.api.aggregate.AccountAssembler;
import com.shinoaki.wows.api.data.AccountInfo;
import com.shinoaki.wows.api.type.WowsBattlesType;
import com.shinoaki.wows.api.type.WowsServer;

public class Demo {
    public static void main(String[] args) throws Exception {
        long accountId = 2022515210L;
        long clanId = 2000016057L;
        WowsApi api = WowsApi.of(WowsServer.ASIA)
                .timeout(20)
                .credentials(c -> c
                        .applicationId(System.getenv("WOWS_APP_ID"))
                        .shareToken(accountId, System.getenv("WOWS_VORTEX_TOKEN")));

        System.out.println("vortex 玩家   = " + api.vortex().user(accountId).name());
        System.out.println("vortex 战舰   = " + api.vortex().ships(accountId, WowsBattlesType.PVP).shipMap().size() + " 艘");
        System.out.println("clans 详情    = " + api.clans().clanInfo(clanId).name());
        System.out.println("clans 排行    = " + api.clans().ladder(0).size() + " 条");
        System.out.println("dev 玩家详情  = " + api.developers().user(accountId).nickname());
        AccountInfo info = AccountAssembler.of(api).info(accountId);
        System.out.println("聚合档案      = " + info.nickname() + " / 军团=" + info.clan().tag());
    }
}
```

### 12. 当前覆盖范围

| 域 | 3.0 状态 |
|---|---|
| 玩家：vortex 搜索/详情；dev 搜索/详情/所属军团/军团详情 + 聚合 | ✅ 已迁入 |
| 战舰：vortex 战绩列表；dev 统计 / 全量战绩 / 徽章 | ✅ 已迁入 |
| 军团：vortex 详情 / 成员 / 搜索 / 所属军团 / 排行；dev 搜索 / 赛季 | ✅ 已迁入 |
| 箱子 / 资源 / 词典：箱子详情与列表、资源列表、资源索引、军团词典 | ✅ 已迁入 |
| 监控与断代清理 | Phase 3（监控切 endpoint-id 基线 + 删除旧入口） |

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

共享 `HttpClient` 由 `HttpClientProvider` 统一持有（不跟随重定向、不挂 CookieHandler——重定向与会话 cookie 由上述机制按主机自行处理），
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
  **全部 22 个端点已迁入**；错误语义强化（缺凭证 fast-fail、不支持显式拒绝、限流显式策略、排行失败不再静默吞）；
  旧入口（5 个 Tools 类）标记 `@Deprecated`，将在断代清理时删除。
- **2.x 版本变更历史**（2.0.3 同步方法 / 2.3.0 Jackson3 / 2.6.0 字段补齐 / 2.7.0 莱服分家 / 2.7.1 箱子字段）
  与旧版用法见 [`docs/3.0之前-用法说明.md`](docs/3.0之前-用法说明.md)。

## 基于此SDK开发的平台接口

`https://v3-api.wows.shinoaki.com/v3/index.html`
