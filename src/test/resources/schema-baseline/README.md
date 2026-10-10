# 接口结构基线（Schema Baseline）

这里存放「接口字段漂移监视」的基线快照：每个接口一个 JSON，记录归一化后的字段路径与其 JSON 类型。
基线入库 git，作用是把接口形状变成可 review 的契约：接口新增/删除字段时，diff 里能直接看到。

## 怎么看

```json
{
  "schemaKey" : "vortex:asia:/api/accounts/{id}/",
  "capturedAt" : "2026-10-10T09:12:33.123Z",
  "fields" : {
    "data.*.created_at" : "number",
    "data.*.name" : "string"
  }
}
```

- `schemaKey` = 通道:服别:路径模板[?归一后的查询参数]；玩家ID/语言/令牌这类每次都变的内容已归一，不会污染 baseline。
- 路径里的 `*` 是动态 key（账号ID、船ID 等数据主键），`[]` 表示数组（不记下标），数组元素取并集。
- `null` 只记录、不告警（服务端 null 抖动频繁）；只有「字段消失」与「类型变化」才算疑似破坏性变更。

## 怎么更新

```powershell
# 在线探测 + 与基线比对（只读，发现 removed/typeChanged 时退出码 1）
& .workbuddy/build.ps1 -SkipMain -SkipTest -Run com.shinoaki.wows.SchemaMonitorTest
# 确认变化符合预期后写回基线（同时清理改名后的过期基线）
& .workbuddy/build.ps1 -SkipMain -SkipTest -Run com.shinoaki.wows.SchemaMonitorTest -RunArgs @('--update')
```

开发者API（官方接口）探测需要令牌：设置环境变量 `WOWS_APP_ID`，未设置时自动跳过，vortex 接口不受影响。
`target/schema-raw/` 存放采集到的原始响应，可用 `--replay` 在无网络环境下重跑整条链路。
