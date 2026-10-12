# 3.0 重写 · Phase 0 行为对照表

> 状态：**已执行**（Phase 0 盘点 + Phase 1 account 域落地均已完成并验证，见文末）。
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
| `vortex.ship.list` | `WowsHttpShipTools.Vortex.shipList` / `shipListMap`（2 点） | GET `/api/accounts/{accountId}/ships/{battleType}/` | `ac` 可选 | 莱服专属战斗类型仅 RU：旧抛 `IllegalArgumentException` → 新 `UnsupportedOnServerException` | Phase 2 |
| `vortex.lootbox.detail` | `WowsLootBox.request` | GET `/api/get_lootbox/{lang}/{lootboxId}/` | — | pt 测试服变体（`isPtServer`）待定 | Phase 2 |
| `vortex.lootbox.list` | `WowsBox.request`（裸 `client.send`） | POST `/api/graphql/glossary/`（Lootbox schema） | — | 同 URL 多 schema：监控键按 endpoint id 自动区分（不再手写 `SchemaRecorder.key`） | Phase 2 |
| `vortex.resources.items` | `WowsResources.request`（裸 `client.send`） | POST 同上（Items schema） | — | 裸发绕过通道（无通道头）→ 迁移即修复 | Phase 2 |
| `vortex.resources.glossary` | `RequestVortexResourcesInfo.request`（裸 `client.send`） | POST 同上（getGlossData schema） | — | 词典 + 船 + 成就一次拉取 | Phase 2 |

## 2. clans 通道（`https://clans.*`）

| 新 endpoint id | 旧调用点 | 方法 / 路径 | 认证 | 特殊语义（验收要点） | 迁移 |
|---|---|---|---|---|---|
| `clans.account.clan` | `WowsHttpClanTools.Vortex.userSearchClanVortex` | GET `/api/accounts/{accountId}/clans/` | — | **非 API 服（CN）查无公会返回 404 → 按空对象处理**（保留旧语义并文档化） | Phase 2 |
| `clans.info` | `clanInfoVortex` | GET `/api/clanbase/{clanId}/claninfo/` | — | 莱服 ladder 字段是 `mk_ladder`（解析层已兼容） | Phase 2 |
| `clans.members` | `clanUserListInfoVortex` / `(clanId, type, season)`（2 点） | GET `/api/members/{clanId}/?battle_type=[&season=]` | — | `cvc` 支持 season 参数 | Phase 2 |
| `clans.search` | `searchClanVortex` | GET `/api/search/autocomplete/?search=&type=clans` | — | 查询串含字面量 `type=clans` | Phase 2 |
| `clans.ladder` | `ClanRankUtils`（`clan_id` 游标分页循环） | GET `/api/ladder/structure/?realm=[&clan_id=][&season=]` | — | **realm 按服**：CN/RU=global、ASIA=sg、EU=eu、NA=us；该工具自带 followRedirects 独立 client（历史原因，随迁移收敛） | Phase 2 |

## 3. dev 通道（`https://api.*`，前缀 `/wows/` 或 `/mk/`）

| 新 endpoint id | 旧调用点 | 方法 / 路径（命名空间内） | 认证 | 特殊语义（验收要点） | 迁移 |
|---|---|---|---|---|---|
| `dev.account.search` | `searchUserDevelopers` | GET `/account/list/?search=` | app 必填 | CN 无 dev API；search 值编码 | ✅ |
| `dev.account.info` | `userInfoDevelopers`（×2 重载）/ 聚合第 1 步 | GET `/account/info/?account_id=&extra=` | app + at 可选 | **extra 按服**：RU 无 `statistics.clan`；at 解锁 `private.*` | ✅ |
| `dev.clan.account` | `WowsHttpClanTools.Developers.userSearchClanDevelopers` / 聚合第 2 步 | GET `/clans/accountinfo/?account_id=&extra=clan` | app | **莱服仍走 `/wows/`**（无 /mk/ 版本，实测）——易错点 | ✅ |
| `dev.clan.info` | `clanInfoDevelopers` / 聚合第 3 步 | GET `/clans/info/?clan_id=&extra=members` | app | 同上：莱服仍走 `/wows/` | ✅ |
| `dev.clan.search` | `searchClanDevelopers` | GET `/clans/list/?search=` | app | 同上按现状保留 `/wows/` | Phase 2 |
| `dev.clan.season` | `season()` | GET `/clans/season/?language=` | app | language 旧硬编码 `zh-cn` → 新为参数 | Phase 2 |
| `dev.clan.glossary` | `WowsEncyclopediaTools.Developers.glossary` | GET `/clans/glossary/` | app | 莱服前缀 `/mk/` | Phase 2 |
| `dev.ship.stats` | `Developers.shipList` / `shipListOa` / `shipListUri`（3 点） | GET `/ships/stats/?account_id=&extra=[&access_token&in_garage=1]` | app + at 可选 | extra 来自 `WowsBattlesType.DEVELOPERS_STATS_TYPES`（pvp 除外的历史集合） | Phase 2 |
| `dev.ship.oper` | `shipOperList` | GET `/ships/stats/?...&extra=<全量 14 类>` | app + at 可选 | 与 stats 同路径、不同 extra；端点 id 区分（监控键不同） | Phase 2 |
| `dev.ship.badges` | `shipBadges` | GET `/ships/badges/?account_id=` | app | **RU 旧静默空 Map → 新显式 `UnsupportedOnServerException`**（端点声明即可拒绝） | Phase 2 |

---

## 4. 失败 / 空值语义对照（有意变更，逐条）

| 场景 | 旧行为 | 新行为（3.0 方案 §6） |
|---|---|---|
| 必填凭证缺失（如漏传 application_id） | 直接裸发 → 服务端报错/返回 status=error | 发送前 `MissingCredentialException`（**零网络请求**） |
| CN 调 dev 端点 | 旧库不拦截（调用方自行规避） | 发送前 `UnsupportedOnServerException` |
| 战斗类型/徽章不支持当前服 | `IllegalArgumentException` 或**静默空 Map** | 统一 `UnsupportedOnServerException` |
| CN 搜索 503 | `searchUserVortexCn` 静默吞 → 空表（分不清限流/空结果） | `RateLimitPolicy`：STRICT 抛 503 / TOLERANT 返回空表+日志（**"空结果"只表示服务器明确返回了空**） |
| 非 API 服查无公会 404 | 空对象（保住） | 同左（端点级语义，文档化） |

## 5. URL 规范化说明

- 查询参数顺序：新实现统一为 **凭证参数在前（槽位声明顺序），端点参数随后**；旧实现各方法手拼顺序不一（如 `/clans/info/?extra=members&application_id=..&clan_id=..`）。
  服务端语义不受顺序影响；离线测试按参数集合断言，在线烟测按**解析结果逐字节**断言（已全绿）。
- 路径占位符（搜索词等）统一按 encodeURIComponent 语义编码（与旧 `HttpCodec.encodeURIComponent` 逐字一致，有交叉断言）。
- `application_id` / `access_token` / `ac` 等凭证值原样拼接（不做 URL 编码），与旧实现一致。

## 6. 验收执行记录（Phase 1）

| 项 | 工具 | 结果 |
|---|---|---|
| 离线等价（假上游 + raw 回放，不打真实服务） | `RewriteAccountEquivalenceTest` | **49/49 通过**：URL/参数/编码/认证注入/聚合新旧等价/失败分支/限流容错/gzip/监控键 |
| 在线新旧对比（真实服务） | `RewriteAccountSmokeTest` | **16/16 通过**：ASIA/RU/CN 逐端点序列化逐字节一致（含 RU 3.4MB 账号详情、CN 私有凭证、3 请求聚合） |
| 既有离线自检回归 | ExceptionHierarchy / ShipInfoSerializationCompat / 三个 field_coverage / SchemaMonitor --selftest | 全绿（exit=0） |
