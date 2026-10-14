package com.shinoaki.wows.old;

import com.shinoaki.wows.api.WowsApi;
import com.shinoaki.wows.api.server.ServerProfile;
import com.shinoaki.wows.api.type.WowsServer;
import com.shinoaki.wows.api.utils.JsonUtils;
import com.shinoaki.wows.api.vortex.VortexResourcesLanguage;
import com.shinoaki.wows.api.vortex.resources.RequestVortexResourcesInfo;
import com.shinoaki.wows.api.vortex.resources.WowsResources;
import com.shinoaki.wows.api.vortex.resources.box.WowsBox;
import com.shinoaki.wows.api.vortex.resources.box.WowsLootBox;
import com.shinoaki.wows.testkit.TestConfig;
import com.shinoaki.wows.testkit.TestReport;

import java.util.List;

/**
 * 「资源类查询」新旧等价测试：真实服务上跑新旧对比（箱子详情/列表、资源列表、资源索引），
 * 另有两条不联网的 PT 主机切换断言。
 * <p>
 * 用法：{@code java -cp ... com.shinoaki.wows.old.RewriteResourceTest}（建议带 {@code -Dwows.proxy=127.0.0.1:7890}）；
 * 退出码 = 失败项数。
 *
 * @author Xun
 */
public class RewriteResourceTest {

    public static void main(String[] args) {
        TestReport report = new TestReport("资源类查询");
        System.out.println("── 资源类查询 · 新旧等价测试（在线） ──");
        local(report);
        online(report);
        report.finish();
    }

    // ================= 本地（无网络）：PT 主机切换 =================

    private static void local(TestReport report) {
        report.section("本地 · PT 主机切换（无网络）");
        ServerProfile base = ServerProfile.of(WowsServer.ASIA);
        ServerProfile pt = base.withPtServer();
        report.expect("PT 切换 vortex 主机", base.vortexPtHost(), pt.vortexHost());
        report.expect("PT 不动 clans 主机", base.clansHost(), pt.clansHost());
    }

    // ================= 在线：真实服务新旧对比 =================

    private static void online(TestReport report) {
        TestConfig.client(); // 先挂代理再取共享 client
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
}
