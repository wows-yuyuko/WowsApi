package com.shinoaki.wows;

import com.shinoaki.wows.api.WowsApi;
import com.shinoaki.wows.api.monitor.SchemaRecorder;
import com.shinoaki.wows.api.server.ServerProfile;
import com.shinoaki.wows.api.type.WowsServer;
import com.shinoaki.wows.api.utils.JsonUtils;
import com.shinoaki.wows.api.vortex.VortexResourcesLanguage;
import com.shinoaki.wows.api.vortex.resources.RequestVortexResourcesInfo;
import com.shinoaki.wows.api.vortex.resources.WowsResources;
import com.shinoaki.wows.api.vortex.resources.box.WowsBox;
import com.shinoaki.wows.api.vortex.resources.box.WowsLootBox;
import com.shinoaki.wows.testkit.FakeUpstream;
import com.shinoaki.wows.testkit.RecordedRequest;
import com.shinoaki.wows.testkit.TestConfig;
import com.shinoaki.wows.testkit.TestReport;

import java.net.http.HttpClient;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Function;

/**
 * 3.0 重写「资源类查询」域新旧等价测试：箱子详情（语言规则）、graphql 箱子列表/资源列表/资源索引
 * （POST 请求体逐字节）、PT 测试服主机切换、监控键。
 * <p>
 * 默认跑<b>离线段</b>（假上游回放 {@code target/schema-raw}，不打真实服务）；
 * 传 {@code --online} 追加<b>在线段</b>（真实服务新旧对比。本机直连 WG 主机不稳定，
 * 建议带 {@code -Dwows.proxy=127.0.0.1:7890}）。
 * <p>
 * 用法：{@code java -cp ... com.shinoaki.wows.RewriteResourceTest [--online]}；退出码 = 失败项数。
 *
 * @author Xun
 */
public class RewriteResourceTest {

    private static final Path RAW = Path.of("target/schema-raw");
    private static final long LOOTBOX_ASIA = TestConfig.LOOTBOX_ASIA;
    private static final long LOOTBOX_RU = TestConfig.LOOTBOX_RU;

    public static void main(String[] args) throws Exception {
        boolean online = flag(args, "--online");
        TestReport report = new TestReport("资源类查询");
        System.out.println("── 资源类查询 · 3.0 新旧等价测试 ──");
        System.out.println("模式: " + (online ? "离线（假上游）+ 在线（真实服务）" : "离线（假上游；--online 追加在线新旧对比）"));

        offline(report);
        if (online) {
            online(report);
        } else {
            System.out.println();
            System.out.println("（在线段已跳过：加 --online 跑到真实服务的新旧对比）");
        }
        report.finish();
    }

    // ================= 离线：假上游 =================

    private static void offline(TestReport report) throws Exception {
        if (!Files.isDirectory(RAW)) {
            System.out.println();
            System.out.println("!! 缺少原始响应仓库 " + RAW + "（先跑一次 SchemaMonitorTest 采集）——离线段跳过");
            return;
        }
        String lootboxAsia = read("vortex_asia_api_get_lootbox_lang_id_.json");
        String lootboxRu = read("vortex_ru_api_get_lootbox_lang_id_.json");
        String boxAsia = read("vortex_asia_api_graphql_glossary_Lootbox.json");
        String itemsRu = read("vortex_ru_api_graphql_glossary_Items.json");
        String glossAsia = read("vortex_asia_api_graphql_glossary_getGlossData.json");
        String glossRu = read("vortex_ru_api_graphql_glossary_getGlossData.json");

        try (FakeUpstream upstream = FakeUpstream.start()) {
            upstream.vortex().exact("/api/get_lootbox/zh-sg/" + LOOTBOX_ASIA + "/", lootboxAsia);
            upstream.vortex().exact("/api/get_lootbox/ru/" + LOOTBOX_RU + "/", lootboxRu);
            upstream.vortex().graphql("Lootbox", "zh-sg", boxAsia);
            upstream.vortex().graphql("Items", "ru", itemsRu);
            upstream.vortex().graphql("getGlossData", "zh-sg", glossAsia);
            upstream.vortex().graphql("getGlossData", "ru", glossRu);

            checks(report, upstream, lootboxAsia, lootboxRu, glossRu);
        }
    }

    private static void checks(TestReport report, FakeUpstream upstream, String lootboxAsia, String lootboxRu, String glossRu) throws Exception {
        var log = upstream.requests();
        ServerProfile asia = ServerProfile.of(WowsServer.ASIA).withHosts(upstream.vortexBase(), upstream.clansBase(), upstream.apiBase());
        ServerProfile ru = ServerProfile.of(WowsServer.RU).withHosts(upstream.vortexBase(), upstream.clansBase(), upstream.apiBase());
        Function<Object, String> toJson = JsonUtils.json()::toJson;
        var json = JsonUtils.json();

        // ================= A. 箱子详情 =================
        report.section("A. 箱子详情");
        {
            WowsApi api = WowsApi.of(asia);
            WowsLootBox actual = api.resources().lootbox(LOOTBOX_ASIA);
            report.expect("A1 箱子路径（默认 zh-sg）", "/api/get_lootbox/zh-sg/" + LOOTBOX_ASIA + "/", log.last().rawPath());
            report.expect("A1 结果=旧解析",
                    toJson.apply(WowsLootBox.parse(json.parse(lootboxAsia))),
                    toJson.apply(actual));
        }
        {
            // 莱服：语言被固定为 ru（即使传 SG）
            WowsApi api = WowsApi.of(ru);
            WowsLootBox actual = api.resources().lootbox(LOOTBOX_RU, VortexResourcesLanguage.SG);
            report.expect("A2 莱服语言固定 ru", "/api/get_lootbox/ru/" + LOOTBOX_RU + "/", log.last().rawPath());
            report.expect("A2 结果=旧解析（RU 独有字段）",
                    toJson.apply(WowsLootBox.parse(json.parse(lootboxRu))),
                    toJson.apply(actual));
        }

        // ================= B. graphql（POST） =================
        report.section("B. graphql：箱子列表 / 资源列表 / 资源索引");
        {
            WowsApi api = WowsApi.of(asia);
            List<WowsBox> actual = api.resources().lootboxes(VortexResourcesLanguage.SG);
            RecordedRequest req = log.last();
            report.expect("B1 箱子列表 POST", "POST", req.method());
            report.expect("B1 路径", "/api/graphql/glossary/", req.rawPath());
            report.expect("B1 Content-Type", "application/json", req.headers().getFirst("Content-Type"));
            report.expect("B1 请求体=旧模板逐字节", WowsBox.requestBody("zh-sg"), req.body());
            report.expect("B1 结果非空", "true", String.valueOf(!actual.isEmpty()));
        }
        {
            WowsApi api = WowsApi.of(ru);
            List<WowsResources> actual = api.resources().items(VortexResourcesLanguage.RU);
            report.expect("B2 Items 请求体", WowsResources.requestBody("ru"), log.last().body());
            report.expect("B2 结果非空", "true", String.valueOf(!actual.isEmpty()));
        }
        {
            WowsApi api = WowsApi.of(ru);
            RequestVortexResourcesInfo actual = api.resources().glossary(VortexResourcesLanguage.RU);
            report.expect("B3 getGlossData 请求体", RequestVortexResourcesInfo.requestBody("ru"), log.last().body());
            report.expect("B3 结果=旧解析",
                    toJson.apply(RequestVortexResourcesInfo.parse(json.parse(glossRu), "ru")),
                    toJson.apply(actual));
        }

        // ================= C. PT 与监控键 =================
        report.section("C. PT 测试服与监控键");
        {
            // PT：仅 vortex 主机切换（不联网，只验证档案）
            ServerProfile pt = ServerProfile.of(WowsServer.ASIA).withPtServer();
            report.expect("C1 PT 切换 vortex 主机", ServerProfile.of(WowsServer.ASIA).vortexPtHost(), pt.vortexHost());
            report.expect("C1 PT 不动 clans 主机", ServerProfile.of(WowsServer.ASIA).clansHost(), pt.clansHost());
        }
        {
            WowsApi api = WowsApi.of(asia);
            SchemaRecorder.enable();
            try {
                api.resources().glossary(VortexResourcesLanguage.SG);
            } finally {
                List<SchemaRecorder.Record> records = SchemaRecorder.drain();
                SchemaRecorder.disable();
                report.expect("C2 监控键=vortex.resources.glossary（graphql 自动区分）", "vortex.resources.glossary",
                        records.isEmpty() ? "(空)" : records.getFirst().schemaKey());
            }
        }
    }

    // ================= 在线：真实服务新旧对比 =================

    private static void online(TestReport report) {
        HttpClient client = TestConfig.client();
        WowsApi.of(WowsServer.ASIA).timeout(20);
        for (WowsServer server : List.of(WowsServer.ASIA, WowsServer.RU)) {
            resources(report, server);
        }
    }

    private static void resources(TestReport report, WowsServer server) {
        String label = server.getCode();
        long lootboxId = server == WowsServer.RU ? TestConfig.LOOTBOX_RU : TestConfig.LOOTBOX_ASIA;
        VortexResourcesLanguage language = server == WowsServer.RU ? VortexResourcesLanguage.RU : VortexResourcesLanguage.SG;
        report.section("在线 · " + label + " 资源/箱子/词典（语言 " + language.getLanguage() + "）");
        WowsApi api = WowsApi.of(server);
        report.compare(label + " resources.lootbox（箱子详情）",
                () -> JsonUtils.json().toJson(WowsLootBox.request(server, language, lootboxId)),
                () -> JsonUtils.json().toJson(api.resources().lootbox(lootboxId, language)));
        report.compare(label + " resources.lootboxes（箱子列表）",
                () -> JsonUtils.json().toJson(WowsBox.request(false, server, language)),
                () -> JsonUtils.json().toJson(api.resources().lootboxes(language)));
        report.compare(label + " resources.items（资源列表）",
                () -> JsonUtils.json().toJson(WowsResources.request(server, language)),
                () -> JsonUtils.json().toJson(api.resources().items(language)));
        report.compare(label + " resources.glossary（资源索引）",
                () -> JsonUtils.json().toJson(RequestVortexResourcesInfo.request(server, language)),
                () -> JsonUtils.json().toJson(api.resources().glossary(language)));
    }

    // ================= 辅助 =================

    private static String read(String name) throws java.io.IOException {
        return Files.readString(RAW.resolve(name));
    }

    private static boolean flag(String[] args, String name) {
        for (String arg : args) {
            if (arg.equalsIgnoreCase(name)) {
                return true;
            }
        }
        return false;
    }
}
