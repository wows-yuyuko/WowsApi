package com.shinoaki.wows.api.aggregate;

import com.shinoaki.wows.api.WowsApi;
import com.shinoaki.wows.api.client.DevelopersClient;
import com.shinoaki.wows.api.data.AccountClanInfo;
import com.shinoaki.wows.api.data.AccountInfo;
import com.shinoaki.wows.api.developers.account.DevelopersUserInfo;
import com.shinoaki.wows.api.developers.clan.DevelopersClanInfo;
import com.shinoaki.wows.api.developers.clan.DevelopersSearchUserClan;
import com.shinoaki.wows.api.error.BasicException;

import java.util.Objects;

/**
 * 玩家综合档案编排（3.0 重写，aggregate 层——多请求编排必须显式 opt-in）。
 * <p>
 * 视图层保持"1 调用 = 1 请求"，本类把旧 {@code accountInfoDevelopers} 的编排语义固化：
 * <ol>
 *     <li>玩家详情（{@code dev.account.info}）；</li>
 *     <li>玩家所属军团（{@code dev.clan.account}）；</li>
 *     <li>若在军团中（clanId&gt;0），补拉军团详情（{@code dev.clan.info}）补全名称/tag/简介/人数。</li>
 * </ol>
 * 失败语义：任一步失败即整体失败（不吞异常、不降级）；与旧实现一致。
 *
 * @author Xun
 */
public final class AccountAssembler {

    private final DevelopersClient developers;

    private AccountAssembler(DevelopersClient developers) {
        this.developers = developers;
    }

    /**
     * 由 api 实例创建（沿用其实例凭证表与服务器档案）。
     */
    public static AccountAssembler of(WowsApi api) {
        Objects.requireNonNull(api, "api");
        return new AccountAssembler(api.developers());
    }

    /**
     * 玩家综合档案：3 个请求（详情 + 所属军团 + 军团详情；无军团时 2 个）。
     */
    public AccountInfo info(long accountId) throws BasicException {
        DevelopersUserInfo user = developers.user(accountId);
        DevelopersSearchUserClan clanRef = developers.accountClan(accountId);
        AccountClanInfo clan = AccountClanInfo.of(clanRef);
        if (clan.clanId() > 0) {
            DevelopersClanInfo clanInfo = developers.clanInfo(clan.clanId());
            clan = AccountClanInfo.of(clan, clanInfo);
        }
        return AccountInfo.of(user, clan);
    }
}
