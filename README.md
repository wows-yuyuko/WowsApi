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

`https://recent.wows.shinoaki.com:8890/v3/index.html`
# 如何使用

参考test文件夹下面的测试方法

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
