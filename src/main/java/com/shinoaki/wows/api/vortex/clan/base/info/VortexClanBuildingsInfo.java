package com.shinoaki.wows.api.vortex.clan.base.info;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.*;

/**
 * 公会建筑信息
 *
 * @author Xun
 * @date 2022/07/06 星期三 10:00
 */
public class VortexClanBuildingsInfo {

    /**
     * null
     */
    private Long clanId;
    /**
     * null
     */
    private Integer buildingsId;
    /**
     * 数组 list<String>
     */
    private String buildingsModifiers;
    /**
     * null
     */
    private String buildingsName;
    /**
     * null
     */
    private Integer buildingsLevel;

    public static List<VortexClanBuildingsInfo> clan(long clanId, JsonNode node) {
        List<VortexClanBuildingsInfo> infoList = new ArrayList<>();
        Iterator<Map.Entry<String, JsonNode>> fields = node.fields();
        fields.forEachRemaining(x -> {
            VortexClanBuildingsInfo info = new VortexClanBuildingsInfo();
            info.setClanId(clanId);
            info.setBuildingsName(x.getValue().get("name").asText());
            info.setBuildingsLevel(x.getValue().get("level").asInt());
            info.setBuildingsId(x.getValue().get("id").asInt());
            info.setBuildingsModifiers(x.getValue().get("modifiers").toString());
            infoList.add(info);
        });
        return infoList;
    }

    @Override
    public String toString() {
        return "VortexClanBuildingsInfo{" +
                "clanId=" + clanId +
                ", buildingsId=" + buildingsId +
                ", buildingsModifiers='" + buildingsModifiers + '\'' +
                ", buildingsName='" + buildingsName + '\'' +
                ", buildingsLevel=" + buildingsLevel +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof VortexClanBuildingsInfo that)) return false;
        return Objects.equals(getClanId(), that.getClanId()) && Objects.equals(getBuildingsId(), that.getBuildingsId()) && Objects.equals(getBuildingsModifiers(), that.getBuildingsModifiers()) && Objects.equals(getBuildingsName(), that.getBuildingsName()) && Objects.equals(getBuildingsLevel(), that.getBuildingsLevel());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getClanId(), getBuildingsId(), getBuildingsModifiers(), getBuildingsName(), getBuildingsLevel());
    }

    public Long getClanId() {
        return clanId;
    }

    public void setClanId(Long clanId) {
        this.clanId = clanId;
    }

    public Integer getBuildingsId() {
        return buildingsId;
    }

    public void setBuildingsId(Integer buildingsId) {
        this.buildingsId = buildingsId;
    }

    public String getBuildingsModifiers() {
        return buildingsModifiers;
    }

    public void setBuildingsModifiers(String buildingsModifiers) {
        this.buildingsModifiers = buildingsModifiers;
    }

    public String getBuildingsName() {
        return buildingsName;
    }

    public void setBuildingsName(String buildingsName) {
        this.buildingsName = buildingsName;
    }

    public Integer getBuildingsLevel() {
        return buildingsLevel;
    }

    public void setBuildingsLevel(Integer buildingsLevel) {
        this.buildingsLevel = buildingsLevel;
    }
}
