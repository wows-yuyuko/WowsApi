package com.shinoaki.wows.api.vortex.clan.base;

import com.fasterxml.jackson.databind.JsonNode;
import com.shinoaki.wows.api.type.WowsServer;
import com.shinoaki.wows.api.utils.DateUtils;
import com.shinoaki.wows.api.vortex.clan.base.info.VortexClanBuildingsInfo;
import com.shinoaki.wows.api.vortex.clan.base.info.VortexClanLeagueInfo;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

/**
 * @author Xun
 * @date 2023/4/10 16:25 星期一
 */
public class VortexClanInfo {
    private long clanId;
    /**
     * 服务器
     */
    private WowsServer wowsServer;
    /**
     * 显示tag
     */
    private String tag;
    /**
     * 公会名称
     */
    private String name;
    /**
     * 介绍
     */
    private String description;
    /**
     * 公会成员数量
     */
    private int membersCount;
    /**
     * 创建者名称
     */
    private String creatorName;
    /**
     * 创建者ID
     */
    private long creatorAccountId;
    /**
     * 管理员名称
     */
    private String leaderName;
    /**
     * 管理员ID
     */
    private long leaderAccountId;

    /**
     * 公会标签颜色
     */
    private String colorRgb;
    /**
     * 公会创建时间
     */
    private long createdAt;
    /**
     * 资料更新时间
     */
    private long updatedAt;

    private List<VortexClanLeagueInfo> clanLeagueInfoList;
    private List<VortexClanBuildingsInfo> clanBuildingsInfoList;

    public static VortexClanInfo to(WowsServer server, long clanId, JsonNode body) {
        JsonNode clanview = body.get("clanview");
        if (clanview != null) {
            JsonNode clan = clanview.get("clan");
            VortexClanInfo info = new VortexClanInfo();
            info.setClanId(clanId);
            info.setWowsServer(server);
            info.setClanBuildingsInfoList(VortexClanBuildingsInfo.clan(clanId, clanview.get("buildings")));
            info.setClanLeagueInfoList(VortexClanLeagueInfo.clan(clanId, clanview.get("wows_ladder").get("ratings")));
            info.setTag(clan.get("tag").asText());
            info.setName(clan.get("name").asText());
            info.setDescription(clan.get("description").asText());
            info.setMembersCount(clan.get("members_count").asInt());
            info.setColorRgb(clan.get("color").asText());
            info.setCreatedAt(DateUtils.toTimeMillis(LocalDateTime.parse(clan.get("created_at").asText(), DateTimeFormatter.ISO_DATE_TIME)));
            return info;
        }
        return null;
    }

    @Override
    public String toString() {
        return "VortexClanInfo{" +
                "clanId=" + clanId +
                ", wowsServer=" + wowsServer +
                ", tag='" + tag + '\'' +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", membersCount=" + membersCount +
                ", creatorName='" + creatorName + '\'' +
                ", creatorAccountId=" + creatorAccountId +
                ", leaderName='" + leaderName + '\'' +
                ", leaderAccountId=" + leaderAccountId +
                ", colorRgb='" + colorRgb + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                ", clanLeagueInfoList=" + clanLeagueInfoList +
                ", clanBuildingsInfoList=" + clanBuildingsInfoList +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof VortexClanInfo that)) return false;
        return getClanId() == that.getClanId() && getMembersCount() == that.getMembersCount() && getCreatorAccountId() == that.getCreatorAccountId() && getLeaderAccountId() == that.getLeaderAccountId() && getCreatedAt() == that.getCreatedAt() && getUpdatedAt() == that.getUpdatedAt() && getWowsServer() == that.getWowsServer() && Objects.equals(getTag(), that.getTag()) && Objects.equals(getName(), that.getName()) && Objects.equals(getDescription(), that.getDescription()) && Objects.equals(getCreatorName(), that.getCreatorName()) && Objects.equals(getLeaderName(), that.getLeaderName()) && Objects.equals(getColorRgb(), that.getColorRgb()) && Objects.equals(getClanLeagueInfoList(), that.getClanLeagueInfoList()) && Objects.equals(getClanBuildingsInfoList(), that.getClanBuildingsInfoList());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getClanId(), getWowsServer(), getTag(), getName(), getDescription(), getMembersCount(), getCreatorName(), getCreatorAccountId(),
                getLeaderName(), getLeaderAccountId(), getColorRgb(), getCreatedAt(), getUpdatedAt(), getClanLeagueInfoList(), getClanBuildingsInfoList());
    }

    public long getClanId() {
        return clanId;
    }

    public void setClanId(long clanId) {
        this.clanId = clanId;
    }

    public WowsServer getWowsServer() {
        return wowsServer;
    }

    public void setWowsServer(WowsServer wowsServer) {
        this.wowsServer = wowsServer;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getMembersCount() {
        return membersCount;
    }

    public void setMembersCount(int membersCount) {
        this.membersCount = membersCount;
    }

    public String getCreatorName() {
        return creatorName;
    }

    public void setCreatorName(String creatorName) {
        this.creatorName = creatorName;
    }

    public long getCreatorAccountId() {
        return creatorAccountId;
    }

    public void setCreatorAccountId(long creatorAccountId) {
        this.creatorAccountId = creatorAccountId;
    }

    public String getLeaderName() {
        return leaderName;
    }

    public void setLeaderName(String leaderName) {
        this.leaderName = leaderName;
    }

    public long getLeaderAccountId() {
        return leaderAccountId;
    }

    public void setLeaderAccountId(long leaderAccountId) {
        this.leaderAccountId = leaderAccountId;
    }

    public String getColorRgb() {
        return colorRgb;
    }

    public void setColorRgb(String colorRgb) {
        this.colorRgb = colorRgb;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(long updatedAt) {
        this.updatedAt = updatedAt;
    }

    public List<VortexClanLeagueInfo> getClanLeagueInfoList() {
        return clanLeagueInfoList;
    }

    public void setClanLeagueInfoList(List<VortexClanLeagueInfo> clanLeagueInfoList) {
        this.clanLeagueInfoList = clanLeagueInfoList;
    }

    public List<VortexClanBuildingsInfo> getClanBuildingsInfoList() {
        return clanBuildingsInfoList;
    }

    public void setClanBuildingsInfoList(List<VortexClanBuildingsInfo> clanBuildingsInfoList) {
        this.clanBuildingsInfoList = clanBuildingsInfoList;
    }
}
