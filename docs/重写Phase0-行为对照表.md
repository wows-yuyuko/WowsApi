# 3.0 重写 · Phase 0 行为对照表

> 状态：**已执行**（Phase 0 盘点 + Phase 1/Phase 2 全部 22 个端点落地，见文末验收执行记录）。
> 基准（本机实测）：main 代码中 **29 个请求发送点**（`ApiHttp.send` / `VortexHttp.send` / `client.send` 直发），
> 聚合为 **22 个逻辑端点**（vortex 7 / clans 5 / dev 10）。
> 证据命令：`grep -cE "ApiHttp\.send\(|VortexHttp\.send\(|client\.send\(" src/main/java/**`（逐文件计数：ClanRankUtils 1 / Encyclopedia 1 / ClanTools 9 / ShipTools 5 / UserTools 9 / resources 4）。

本表是迁移验收清单：新实现逐端点对照旧实现的 **URL / 认证 / 支持面 / 异常策略**；
「迁移」列标注已在哪个阶段落地（✅ = 已完成并通过新旧逐字节对比）。

---

## 1. vortex 通道（`https://vortex.*`）

| 新 endpoint id | 旧调用点 | 方法 / 路径 | 认证 | 特殊语义（验收要点） | 迁移 |
|---|---|---|---|---|---|
| `vortex.account.search` | `WowsHttpUserTools.searchUserVortex` / `searchUserVortexCn`（2 点，同一 URL） | GET `/api/accounts/search/autocomplete/{user}/` | — | CN 高频 **503**：旧 Cn 版静默吞 → 新显式 `RateLimitPolicy`（STRICT 默认抛 / TOLERANT 返回空表+日志）；搜索词按 encodeURIComponent 编码 | ✅ |
| `vortex.account.info` | `userVortex(id)` / `userVortex(id, token)`（2 点） | GET `/api/accounts/{accountId}/` | `ac` 可选 | 配 share token 时返回含私有数据的完整档案；隐藏档案抛 `HiddenProfileException` | ✅ |
| `vortex.ship.list` | `WowsHttpShipTools.Vortex.shipList` / `shipListMap`（2 点） | GET `/api/accounts/{accountId}/ships/{battleType}/` | `ac` 可选 | 莱服专属战斗类型仅 RU：旧抛 `IllegalArgumentException` → 新 `UnsupportedOnServerException` | ✅ |
| `vortex.lootbox.detail` | `WowsLootBox.request` | GET `/api/get_lootbox/{lang}/{lootboxId}/` | — | pt 测试服变体（`isPtServer`）待定 | ✅ |
| `vortex.lootbox.list` | `WowsBox.request`（裸 `client.send`） | POST `/api/graphql/glossary/`（Lootbox schema） | — | 同 URL 多 schema：监控键按 endpoint id 自动区分（不再手写 `SchemaRecorder.key`） | ✅ |
| `vortex.resources.items` | `WowsResources.request`（裸 `client.send`） | POST 同上（Items schema） | — | 裸发绕过通道（无通道头）→ 迁移即修复 | ✅ |
| `vortex.resources.glossary` | `RequestVortexResourcesInfo.request`（裸 `client.send`） | POST 同上（getGlossData schema） | — | 词典 + 船 + 成就一次拉取 | ✅ |

## 2. clans 通道（`https://clans.*`）

| 新 endpoint id | 旧调用点 | 方法 / 路径 | 认证 | 特殊语义（验收要点） | 迁移 |
|---|---|---|---|---|---|
| `clans.account.clan` | `WowsHttpClanTools.Vortex.userSearchClanVortex` | GET `/api/accounts/{accountId}/clans/` | — | **非 API 服（CN）查无公会返回 404 → 按空对象处理**（保留旧语义并文档化） | ✅ |
| `clans.info` | `clanInfoVortex` | GET `/api/clanbase/{clanId}/claninfo/` | — | 莱服 ladder 字段是 `mk_ladder`（解析层已兼容） | ✅ |
| `clans.members` | `clanUserListInfoVortex` / `(clanId, type, season)`（2 点） | GET `/api/members/{clanId}/?battle_type=[&season=]` | — | `cvc` 支持 season 参数 | ✅ |
| `clans.search` | `searchClanVortex` | GET `/api/search/autocomplete/?search=&type=clans` | — | 查询串含字面量 `type=clans` | ✅ |
| `clans.ladder` | `ClanRankUtils`（`clan_id` 游标分页循环） | GET `/api/ladder/structure/?realm=[&clan_id=][&season=]` | — | **realm 按服**：CN/RU=global、ASIA=sg、EU=eu、NA=us；该工具自带 followRedirects 独立 client（历史原因，随迁移收敛） | ✅ |

## 3. dev 通道（`https://api.*`，前缀 `/wows/` 或 `/mk/`）

| 新 endpoint id | 旧调用点 | 方法 / 路径（命名空间内） | 认证 | 特殊语义（验收要点） | 迁移 |
|---|---|---|---|---|---|
| `dev.account.search` | `searchUserDevelopers` | GET `/account/list/?search=` | app 必填 | CN 无 dev API；search 值编码 | ✅ |
| `dev.account.info` | `userInfoDevelopers`（×2 重载）/ 聚合第 1 步 | GET `/account/info/?account_id=&extra=` | app + at 可选 | **extra 按服**：RU 无 `statistics.clan`；at 解锁 `private.*` | ✅ |
| `dev.clan.account` | `WowsHttpClanTools.Developers.userSearchClanDevelopers` / 聚合第 2 步 | GET `/clans/accountinfo/?account_id=&extra=clan` | app | **莱服仍走 `/wows/`**（无 /mk/ 版本，实测）——易错点 | ✅ |
| `dev.clan.info` | `clanInfoDevelopers` / 聚合第 3 步 | GET `/clans/info/?clan_id=&extra=members` | app | 同上：莱服仍走 `/wows/` | ✅ |
| `dev.clan.search` | `searchClanDevelopers` | GET `/clans/list/?search=` | app | 同上按现状保留 `/wows/` | ✅ |
| `dev.clan.season` | `season()` | GET `/clans/season/?language=` | app | language 旧硬编码 `zh-cn` → 新为参数 | ✅ |
| `dev.clan.glossary` | `WowsEncyclopediaTools.Developers.glossary` | GET `/clans/glossary/` | app | 莱服前缀 `/mk/` | ✅ |
| `dev.ship.stats` | `Developers.shipList` / `shipListOa` / `shipListUri`（3 点） | GET `/ships/stats/?account_id=&extra=[&access_token&in_garage=1]` | app + at 可选 | extra 来自 `WowsBattlesType.DEVELOPERS_STATS_TYPES`（pvp 除外的历史集合） | ✅ |
| `dev.ship.oper` | `shipOperList` | GET `/ships/stats/?...&extra=<全量 14 类>` | app + at 可选 | 与 stats 同路径、不同 extra；端点 id 区分（监控键不同） | ✅ |
| `dev.ship.badges` | `shipBadges` | GET `/ships/badges/?account_id=` | app | **RU 旧静默空 Map → 新显式 `UnsupportedOnServerException`**（端点声明即可拒绝） | ✅ |

---

## 4. 失败 / 空值语义对照（有意变更，逐条）

| 场景 | 旧行为 | 新行为（3.0 方案 §6） |
|---|---|---|
| 必填凭证缺失（如漏传 application_id） | 直接裸发 → 服务端报错/返回 status=error | 发送前 `MissingCredentialException`（**零网络请求**） |
| CN 调 dev 端点 | 旧库不拦截（调用方自行规避） | 发送前 `UnsupportedOnServerException` |
| 战斗类型/徽章不支持当前服 | `IllegalArgumentException` 或**静默空 Map** | 统一 `UnsupportedOnServerException` |
| CN 搜索 503 | `searchUserVortexCn` 静默吞 → 空表（分不清限流/空结果） | `RateLimitPolicy`：STRICT 抛 503 / TOLERANT 返回空表+日志（**"空结果"只表示服务器明确返回了空**） |
| 非 API 服查无公会 404 | 空对象（保住） | 同左（端点级语义，文档化：`Endpoint.withNotFoundEmpty`） |
| 军团排行 `season<=0` | 拼 `&season=`（空值）→ 服务端 **422** → 旧实现静默吞成空表（**该路径实际已坏**） | **省略 season 参数**（= 最新赛季，实测 200）；`ladder(season)` 内部分页、失败不再吞（抛 `BasicException`） |
| dev 战舰统计的 `in_garage=1` | 与 access_token 一起手拼（传 token 才有） | `CredentialParam`：跟随本次解析出的 access_token 出现（存储配置的 token 同样生效） |
| 莱服箱子语言 | 传什么都被强制成 `ru` | 同左（旧行为保留，文档化在 `ResourcesClient.lootbox`） |

## 5. URL 规范化说明

- 查询参数顺序：新实现统一为 **凭证参数在前（槽位声明顺序），端点参数随后**；旧实现各方法手拼顺序不一（如 `/clans/info/?extra=members&application_id=..&clan_id=..`）。
  服务端语义不受顺序影响；离线测试按参数集合断言，在线烟测按**解析结果逐字节**断言（已全绿）。
- 路径占位符（搜索词等）统一按 encodeURIComponent 语义编码（与旧 `HttpCodec.encodeURIComponent` 逐字一致，有交叉断言）。
- `application_id` / `access_token` / `ac` 等凭证值原样拼接（不做 URL 编码），与旧实现一致。

## 6. 验收执行记录（Phase 1 + Phase 2）

> 测试按业务域拆分为 4 个独立测试类（不集中）：**账号信息 / 账号战舰 / 公会相关 / 资源类查询**，
> 每个类默认跑「离线段」（假上游，不联网），加 `--online` 追加「在线段」（真实服务新旧对比，建议带 `-Dwows.proxy=127.0.0.1:7890`）；
> 公会类另有 `--full`（整季排行全量对比）。

| 项 | 工具 | 结果 |
|---|---|---|
| 离线等价 · 账号信息 | `RewriteAccountInfoTest` | **43/43**：URL/编码/凭证注入/聚合新旧等价/失败分支/限流容错/gzip/监控键 |
| 离线等价 · 账号战舰 | `RewriteAccountShipTest` | **20/20**：战斗类型支持面 / extra 与 in_garage / 徽章拒绝 / 监控键 |
| 离线等价 · 公会相关 | `RewriteClanTest` | **36/36**：三主机路由（打错主机=404）/ 404→空 / mk_ladder / 分页游标与空页终止 / 监控键 |
| 离线等价 · 资源类查询 | `RewriteResourceTest` | **16/16**：graphql POST 体逐字节 / 莱服语言固定 ru / PT 切换 / 监控键 |
| 在线新旧对比 · 账号信息 | `RewriteAccountInfoTest --online` | **12/12**（ASIA/RU/CN：玩家详情/搜索、dev 详情/搜索、聚合、CN 拒绝） |
| 在线新旧对比 · 账号战舰 | `RewriteAccountShipTest --online` | **9/9**（含 RU rating_solo、CN 带凭证、徽章） |
| 在线新旧对比 · 公会相关 | `RewriteClanTest --online [--full]` | **25/25**（含三服排行单页/游标页；`--full` 整季 360 条全量一致） |
| 在线新旧对比 · 资源类查询 | `RewriteResourceTest --online` | **8/8**（箱子详情/列表、资源列表、资源索引 ×2 服） |
| 既有离线自检回归 | ExceptionHierarchy / ShipInfoSerializationCompat / 三个 field_coverage / SchemaMonitor --selftest | 全绿（exit=0） |

> 在线对比的两条口径说明（均经实测确认）：
> 1. **代理**：本机直连 WG 主机不稳定（vortex 单次直连实测 13s+，逼近超时）；走本地代理 `127.0.0.1:7890` 后稳定 &lt;2s。
>    在线测试建议 `-Dwows.proxy=127.0.0.1:7890`（TestConfig 支持按服覆盖）。
> 2. **服务端非确定性**：同一资源两次响应可能键序互换（`stats`）或数组重排（getGlossData 的 `achievements`），
>    对比采用规范化口径（`testkit.CanonicalJson`，对象键序无关 + 数组多重集；失败自动重试一次）；
>    逐字节保序对比由离线固定字节回放承担。旧排行实现「吞异常静默截断」的偶发行为见 `RewriteClanTest --full` 的重试说明。
