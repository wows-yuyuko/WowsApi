# 新版本基于JDK21

# WowsApi 战舰世界public/vortex-api接口开发包

# Maven坐标 

目前只发布在GitHub这边,支持JDK21+

# 2.0.3版本起，增加同步方法执行

# 2.3.0版本起 Jackson升级到Jackson3 代码不在向下兼容

```xml

<dependency>
    <groupId>com.shinoaki.wows.api</groupId>
    <artifactId>wows-api</artifactId>
    <version>Latest Version</version>
</dependency>
```

# 基于此SDK开发的平台接口
`https://v3-api.wows.shinoaki.com:8443/v3/index.html`

# 如何使用

参考test文件夹下面的测试方法

# 请求通道

请求发送按业务分为两个通道(同位于 `com.shinoaki.wows.api.codec` 包),后续站点策略变动只需改动对应通道:

- `ApiHttp` — 官方开发者API(`server.api()` 域名,即 `/wows/**` 与莱服 `/mk/**`)。
- `VortexHttp` — vortex接口(`vortex.*`/`clans.*` 域名)。

两个通道都经由共用的低层传输(`HttpTransport`,公开类)发送。**Cookie 管理 + 重定向跟随按主机开启**,默认仅注册 `api.korabli.su`(莱服官方API,存在 `307 + Set-Cookie: bp_chl=...`  ):

- 注册主机:无 cookie 请求自动附带/保存 cookie 并跟随重定向(最多2次重放),cookie 进程内共享、按域名隔离、线程安全;
- 并发冷启动单飞:同一主机无有效 cookie 时只允许一个线程发起 握手,其余线程复用其 cookie,避免并发 307 风暴;
- 429 限流(该 WAF 对并发突发会返回 429):自动按 `Retry-After`/指数退避等待后重试,最多 3 次(常数 `HttpTransport` 内可调);
- 其它主机(vortex/clans、以及 ASIA/EU/NA/CN 官方API):走普通快速通道,单次发送直接返回,**零额外开销**;
- 未来若某服务器出现同样防护,一行开启:`HttpTransport.enableCookieRedirect("主机")`;不需要默认莱服处理时可用 `HttpTransport.disableCookieRedirect("api.korabli.su")`;
- 调用方即使使用默认的 `HttpClient.newBuilder().build()`(不跟随重定向、无 cookie handler)也能正常工作,无需自行配置。

通道差异集中在请求头/UA 与后续各自的策略演进( 处理、鉴权等)。各 Tools 的 `Developers`(官方API)与 `Vortex` 方法已分别接入对应通道;旧的 `HttpCodec.request/send` 作为兼容入口保留,同样按上述主机规则处理。

# 关于异步

本SDK**只提供同步方法**（直接返回结果，异常以`BasicException`抛出），不再提供`xxxAsync`异步方法。

如果需要异步调用，建议在调用方自行包装，使用**线程池**或**JDK21虚拟线程**：

线程池示例：

```java
ExecutorService executor = Executors.newFixedThreadPool(8);
CompletableFuture<VortexUserInfo> future = CompletableFuture.supplyAsync(() -> {
    try {
        return tools.userVortex(accountId);
    } catch (BasicException e) {
        throw new RuntimeException(e);
    }
}, executor);
```

虚拟线程示例（JDK21+）：

```java
try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
    var future = executor.submit(() -> tools.userVortex(accountId));
    var data = future.get();
}
```
