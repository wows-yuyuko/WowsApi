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
 * 玩家综合档案编排（aggregate 层，多请求必须显式 opt-in）：
 * 玩家详情 + 所属军团 + （在军团时）军团详情，共 3 请求（无军团时 2 个）；任一步失败即整体失败。
 *
 * @author Xun
 */
public final class AccountAssembler {

    private final DevelopersClient developers;

    private AccountAssembler(DevelopersClient developers) {
        this.developers = developers;
    }

    /**
     * 由 api 实例创建（沿用其凭证与服务器档案）。
     */
    public static AccountAssembler of(WowsApi api) {
        Objects.requireNonNull(api, "api");
        return new AccountAssembler(api.developers());
    }

    /**
     * 玩家综合档案。
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
