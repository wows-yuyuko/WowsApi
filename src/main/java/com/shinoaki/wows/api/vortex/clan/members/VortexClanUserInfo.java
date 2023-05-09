package com.shinoaki.wows.api.vortex.clan.members;


import com.fasterxml.jackson.databind.JsonNode;
import com.shinoaki.wows.api.type.WowsServer;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author Xun
 * @date 2022/07/21 星期四
 */
public class VortexClanUserInfo {
    private long accountId;
    private String nickName;
    private WowsServer wowsServer;
    private long lastBattleTime;
    private String roleName;
    private int roleCode;
    private int daysInClan;

    public static List<VortexClanUserInfo> to(WowsServer server, JsonNode body) {
        if (body.get("status").asText("error").contentEquals("ok")) {
            List<VortexClanUserInfo> list = new ArrayList<>();
            JsonNode items = body.get("items");
            for (JsonNode js : items) {
                VortexClanUserInfo info = new VortexClanUserInfo();
                info.setAccountId(js.get("id").asLong());
                info.setWowsServer(server);
                info.setNickName(js.get("name").asText());
                info.setLastBattleTime(js.get("last_battle_time").asInt());
                info.setDaysInClan(js.get("days_in_clan").asInt());
                JsonNode role = js.get("role");
                info.setRoleName(role.get("name").asText());
                info.setRoleCode(role.get("order").asInt());
                list.add(info);
            }
            return list;
        }
        return null;
    }

    @Override
    public String toString() {
        return "ClanUserInfo{" + "accountId=" + accountId + ", nickName='" + nickName + '\'' + ", wowsServer=" + wowsServer + ", lastBattleTime=" + lastBattleTime + ", roleName='" + roleName + '\'' + ", roleCode=" + roleCode + ", daysInClan=" + daysInClan + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof VortexClanUserInfo that)) return false;
        return getAccountId() == that.getAccountId() && getLastBattleTime() == that.getLastBattleTime() && getRoleCode() == that.getRoleCode() && getDaysInClan() == that.getDaysInClan() && Objects.equals(getNickName(), that.getNickName()) && getWowsServer() == that.getWowsServer() && Objects.equals(getRoleName(), that.getRoleName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getAccountId(), getNickName(), getWowsServer(), getLastBattleTime(), getRoleName(), getRoleCode(), getDaysInClan());
    }

    public long getAccountId() {
        return accountId;
    }

    public void setAccountId(long accountId) {
        this.accountId = accountId;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public WowsServer getWowsServer() {
        return wowsServer;
    }

    public void setWowsServer(WowsServer wowsServer) {
        this.wowsServer = wowsServer;
    }

    public long getLastBattleTime() {
        return lastBattleTime;
    }

    public void setLastBattleTime(long lastBattleTime) {
        this.lastBattleTime = lastBattleTime;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public int getRoleCode() {
        return roleCode;
    }

    public void setRoleCode(int roleCode) {
        this.roleCode = roleCode;
    }

    public int getDaysInClan() {
        return daysInClan;
    }

    public void setDaysInClan(int daysInClan) {
        this.daysInClan = daysInClan;
    }
}
