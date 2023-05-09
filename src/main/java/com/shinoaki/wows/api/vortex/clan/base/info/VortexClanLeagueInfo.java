package com.shinoaki.wows.api.vortex.clan.base.info;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * null
 *
 * @author Xun
 * @date 2022/07/06 星期三 09:47
 */
public class VortexClanLeagueInfo {

    /**
     * 公会ID
     */
    private long clanId;
    /**
     * realm信息
     */
    private String realm;
    /**
     * 最高连胜次数
     */
    private int longestWinningStreak;

    /**
     * 当前连胜
     */
    private int currentWinningStreak;
    /**
     * 第几段
     */
    private int division;
    /**
     * 联盟等级0表示紫会
     */
    private int league;
    /**
     * 胜利场次
     */
    private int winsCount;
    /**
     * 团队
     */
    private int teamNumber;
    /**
     * 最后的战斗时间
     */
    private String lastWinAt;
    /**
     * 第几赛季
     */
    private int seasonNumber;
    /**
     * 首次公开评级
     */
    private int initialPublicRating;
    /**
     * 战斗场次
     */
    private int battlesCount;
    /**
     * 当前分数
     */
    private int divisionRating;
    /**
     * 晋级所需分数
     */
    private int divisionRatingMax;
    /**
     * null
     */
    private int id;
    /**
     * 是否合格?0否 1是
     */
    private int isQualified;
    /**
     * 最高评级
     */
    private int maxPublicRating;
    /**
     * 公开评级
     */
    private int publicRating;
    /**
     * 是否最好的评级0否 1是
     */
    private int isBestSeasonRating;
    /**
     * 最高段位
     */
    private int maxPositionDivision;
    /**
     * 最高分数
     */
    private int maxPositionDivisionRating;
    /**
     * 最高公开分数
     */
    private int maxPositionPublicRating;

    /**
     * 当前联盟
     */
    private int maxPositionLeague;
    /**
     * 状态
     */
    private String status;
    /**
     * 是否保级赛0否 1是
     */
    private int stageStatus;
    /**
     * 必须胜利次数
     */
    private int stageVictoriesRequired;
    /**
     * 目标划分等级
     */
    private int stageTargetDivisionRating;
    /**
     * 目标联赛
     */
    private int stageTargetLeague;
    /**
     * 战斗结果id
     */
    private int stageBattleResultId;
    /**
     * null
     */
    private int stageId;
    /**
     * 类型
     */
    private int stageType;
    /**
     * 目标段位
     */
    private int stageTargetDivision;
    /**
     * 总场次
     */
    private int stageBattles;
    /**
     * 进度 是个字符串数组
     */
    private String stageProgressList;
    /**
     * null
     */
    private String stageTarget;
    /**
     * 目标公开评级
     */
    private int stageTargetPublicRating;

    public static List<VortexClanLeagueInfo> clan(long clanId, JsonNode node) {
        List<VortexClanLeagueInfo> infoList = new ArrayList<>();
        for (var data : node) {
            VortexClanLeagueInfo info = new VortexClanLeagueInfo();
            info.setClanId(clanId);
            info.setLongestWinningStreak(data.get("longest_winning_streak").asInt());
            info.setDivision(data.get("division").asInt());
            info.setRealm(data.get("realm").asText());
            info.setWinsCount(data.get("wins_count").asInt());
            info.setTeamNumber(data.get("team_number").asInt());
            info.setCurrentWinningStreak(data.get("current_winning_streak").asInt());
            info.setLastWinAt(data.get("last_win_at").asText());
            info.setSeasonNumber(data.get("season_number").asInt());
            info.setInitialPublicRating(data.get("initial_public_rating").asInt());
            info.setBattlesCount(data.get("battles_count").asInt());
            info.setDivisionRating(data.get("division_rating").asInt());
            info.setDivisionRatingMax(data.get("division_rating_max").asInt());
            info.setId(data.get("id").asInt());
            info.setIsQualified(data.get("is_qualified").asBoolean() ? 1 : 0);
            info.setLeague(data.get("league").asInt());
            info.setMaxPublicRating(data.get("max_public_rating").asInt());
            info.setIsBestSeasonRating(data.get("is_best_season_rating").asBoolean() ? 1 : 0);
            JsonNode maxPosition = data.get("max_position");
            info.setMaxPositionDivision(maxPosition.get("division").asInt());
            info.setMaxPositionDivisionRating(maxPosition.get("division_rating").asInt());
            info.setMaxPositionPublicRating(maxPosition.get("public_rating").asInt());
            info.setMaxPositionLeague(maxPosition.get("league").asInt());
            info.setStatus(data.get("status").asText());
            JsonNode stage = data.get("stage");
            if (stage == null || stage.isNull()) {
                info.setStageStatus(0);
            } else {
                info.setStageStatus(1);
                info.setStageVictoriesRequired(stage.get("victories_required").asInt());
                info.setStageTargetDivisionRating(stage.get("target_division_rating").asInt());
                info.setStageTargetDivision(stage.get("target_division").asInt());
                info.setStageTargetLeague(stage.get("target_league").asInt());
                info.setStageBattles(stage.get("battle_result_id").asInt());
                info.setStageId(stage.get("id").asInt());
                info.setStageType(stage.get("type").asInt());
                info.setStageBattles(stage.get("battles").asInt());
                info.setStageProgressList(stage.get("progress").toString());
                info.setStageTarget(stage.get("target").asText());
                info.setStageTargetPublicRating(stage.get("target_public_rating").asInt());
            }
            infoList.add(info);
        }
        return infoList;
    }

    @Override
    public String toString() {
        return "VortexClanLeagueInfo{" +
                "clanId=" + clanId +
                ", realm='" + realm + '\'' +
                ", longestWinningStreak=" + longestWinningStreak +
                ", currentWinningStreak=" + currentWinningStreak +
                ", division=" + division +
                ", league=" + league +
                ", winsCount=" + winsCount +
                ", teamNumber=" + teamNumber +
                ", lastWinAt='" + lastWinAt + '\'' +
                ", seasonNumber=" + seasonNumber +
                ", initialPublicRating=" + initialPublicRating +
                ", battlesCount=" + battlesCount +
                ", divisionRating=" + divisionRating +
                ", divisionRatingMax=" + divisionRatingMax +
                ", id=" + id +
                ", isQualified=" + isQualified +
                ", maxPublicRating=" + maxPublicRating +
                ", publicRating=" + publicRating +
                ", isBestSeasonRating=" + isBestSeasonRating +
                ", maxPositionDivision=" + maxPositionDivision +
                ", maxPositionDivisionRating=" + maxPositionDivisionRating +
                ", maxPositionPublicRating=" + maxPositionPublicRating +
                ", maxPositionLeague=" + maxPositionLeague +
                ", status='" + status + '\'' +
                ", stageStatus=" + stageStatus +
                ", stageVictoriesRequired=" + stageVictoriesRequired +
                ", stageTargetDivisionRating=" + stageTargetDivisionRating +
                ", stageTargetLeague=" + stageTargetLeague +
                ", stageBattleResultId=" + stageBattleResultId +
                ", stageId=" + stageId +
                ", stageType=" + stageType +
                ", stageTargetDivision=" + stageTargetDivision +
                ", stageBattles=" + stageBattles +
                ", stageProgressList='" + stageProgressList + '\'' +
                ", stageTarget='" + stageTarget + '\'' +
                ", stageTargetPublicRating=" + stageTargetPublicRating +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof VortexClanLeagueInfo that)) return false;
        return getClanId() == that.getClanId() && getLongestWinningStreak() == that.getLongestWinningStreak() && getCurrentWinningStreak() == that.getCurrentWinningStreak() && getDivision() == that.getDivision() && getLeague() == that.getLeague() && getWinsCount() == that.getWinsCount() && getTeamNumber() == that.getTeamNumber() && getSeasonNumber() == that.getSeasonNumber() && getInitialPublicRating() == that.getInitialPublicRating() && getBattlesCount() == that.getBattlesCount() && getDivisionRating() == that.getDivisionRating() && getDivisionRatingMax() == that.getDivisionRatingMax() && getId() == that.getId() && getIsQualified() == that.getIsQualified() && getMaxPublicRating() == that.getMaxPublicRating() && getPublicRating() == that.getPublicRating() && getIsBestSeasonRating() == that.getIsBestSeasonRating() && getMaxPositionDivision() == that.getMaxPositionDivision() && getMaxPositionDivisionRating() == that.getMaxPositionDivisionRating() && getMaxPositionPublicRating() == that.getMaxPositionPublicRating() && getMaxPositionLeague() == that.getMaxPositionLeague() && getStageStatus() == that.getStageStatus() && getStageVictoriesRequired() == that.getStageVictoriesRequired() && getStageTargetDivisionRating() == that.getStageTargetDivisionRating() && getStageTargetLeague() == that.getStageTargetLeague() && getStageBattleResultId() == that.getStageBattleResultId() && getStageId() == that.getStageId() && getStageType() == that.getStageType() && getStageTargetDivision() == that.getStageTargetDivision() && getStageBattles() == that.getStageBattles() && getStageTargetPublicRating() == that.getStageTargetPublicRating() && Objects.equals(getRealm(), that.getRealm()) && Objects.equals(getLastWinAt(), that.getLastWinAt()) && Objects.equals(getStatus(), that.getStatus()) && Objects.equals(getStageProgressList(), that.getStageProgressList()) && Objects.equals(getStageTarget(), that.getStageTarget());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getClanId(), getRealm(), getLongestWinningStreak(), getCurrentWinningStreak(), getDivision(), getLeague(), getWinsCount(), getTeamNumber(), getLastWinAt(), getSeasonNumber(), getInitialPublicRating(), getBattlesCount(), getDivisionRating(), getDivisionRatingMax(), getId(), getIsQualified(), getMaxPublicRating(), getPublicRating(), getIsBestSeasonRating(), getMaxPositionDivision(), getMaxPositionDivisionRating(), getMaxPositionPublicRating(), getMaxPositionLeague(), getStatus(), getStageStatus(), getStageVictoriesRequired(), getStageTargetDivisionRating(), getStageTargetLeague(), getStageBattleResultId(), getStageId(), getStageType(), getStageTargetDivision(), getStageBattles(), getStageProgressList(), getStageTarget(), getStageTargetPublicRating());
    }

    public long getClanId() {
        return clanId;
    }

    public void setClanId(long clanId) {
        this.clanId = clanId;
    }

    public String getRealm() {
        return realm;
    }

    public void setRealm(String realm) {
        this.realm = realm;
    }

    public int getLongestWinningStreak() {
        return longestWinningStreak;
    }

    public void setLongestWinningStreak(int longestWinningStreak) {
        this.longestWinningStreak = longestWinningStreak;
    }

    public int getCurrentWinningStreak() {
        return currentWinningStreak;
    }

    public void setCurrentWinningStreak(int currentWinningStreak) {
        this.currentWinningStreak = currentWinningStreak;
    }

    public int getDivision() {
        return division;
    }

    public void setDivision(int division) {
        this.division = division;
    }

    public int getLeague() {
        return league;
    }

    public void setLeague(int league) {
        this.league = league;
    }

    public int getWinsCount() {
        return winsCount;
    }

    public void setWinsCount(int winsCount) {
        this.winsCount = winsCount;
    }

    public int getTeamNumber() {
        return teamNumber;
    }

    public void setTeamNumber(int teamNumber) {
        this.teamNumber = teamNumber;
    }

    public String getLastWinAt() {
        return lastWinAt;
    }

    public void setLastWinAt(String lastWinAt) {
        this.lastWinAt = lastWinAt;
    }

    public int getSeasonNumber() {
        return seasonNumber;
    }

    public void setSeasonNumber(int seasonNumber) {
        this.seasonNumber = seasonNumber;
    }

    public int getInitialPublicRating() {
        return initialPublicRating;
    }

    public void setInitialPublicRating(int initialPublicRating) {
        this.initialPublicRating = initialPublicRating;
    }

    public int getBattlesCount() {
        return battlesCount;
    }

    public void setBattlesCount(int battlesCount) {
        this.battlesCount = battlesCount;
    }

    public int getDivisionRating() {
        return divisionRating;
    }

    public void setDivisionRating(int divisionRating) {
        this.divisionRating = divisionRating;
    }

    public int getDivisionRatingMax() {
        return divisionRatingMax;
    }

    public void setDivisionRatingMax(int divisionRatingMax) {
        this.divisionRatingMax = divisionRatingMax;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getIsQualified() {
        return isQualified;
    }

    public void setIsQualified(int isQualified) {
        this.isQualified = isQualified;
    }

    public int getMaxPublicRating() {
        return maxPublicRating;
    }

    public void setMaxPublicRating(int maxPublicRating) {
        this.maxPublicRating = maxPublicRating;
    }

    public int getPublicRating() {
        return publicRating;
    }

    public void setPublicRating(int publicRating) {
        this.publicRating = publicRating;
    }

    public int getIsBestSeasonRating() {
        return isBestSeasonRating;
    }

    public void setIsBestSeasonRating(int isBestSeasonRating) {
        this.isBestSeasonRating = isBestSeasonRating;
    }

    public int getMaxPositionDivision() {
        return maxPositionDivision;
    }

    public void setMaxPositionDivision(int maxPositionDivision) {
        this.maxPositionDivision = maxPositionDivision;
    }

    public int getMaxPositionDivisionRating() {
        return maxPositionDivisionRating;
    }

    public void setMaxPositionDivisionRating(int maxPositionDivisionRating) {
        this.maxPositionDivisionRating = maxPositionDivisionRating;
    }

    public int getMaxPositionPublicRating() {
        return maxPositionPublicRating;
    }

    public void setMaxPositionPublicRating(int maxPositionPublicRating) {
        this.maxPositionPublicRating = maxPositionPublicRating;
    }

    public int getMaxPositionLeague() {
        return maxPositionLeague;
    }

    public void setMaxPositionLeague(int maxPositionLeague) {
        this.maxPositionLeague = maxPositionLeague;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getStageStatus() {
        return stageStatus;
    }

    public void setStageStatus(int stageStatus) {
        this.stageStatus = stageStatus;
    }

    public int getStageVictoriesRequired() {
        return stageVictoriesRequired;
    }

    public void setStageVictoriesRequired(int stageVictoriesRequired) {
        this.stageVictoriesRequired = stageVictoriesRequired;
    }

    public int getStageTargetDivisionRating() {
        return stageTargetDivisionRating;
    }

    public void setStageTargetDivisionRating(int stageTargetDivisionRating) {
        this.stageTargetDivisionRating = stageTargetDivisionRating;
    }

    public int getStageTargetLeague() {
        return stageTargetLeague;
    }

    public void setStageTargetLeague(int stageTargetLeague) {
        this.stageTargetLeague = stageTargetLeague;
    }

    public int getStageBattleResultId() {
        return stageBattleResultId;
    }

    public void setStageBattleResultId(int stageBattleResultId) {
        this.stageBattleResultId = stageBattleResultId;
    }

    public int getStageId() {
        return stageId;
    }

    public void setStageId(int stageId) {
        this.stageId = stageId;
    }

    public int getStageType() {
        return stageType;
    }

    public void setStageType(int stageType) {
        this.stageType = stageType;
    }

    public int getStageTargetDivision() {
        return stageTargetDivision;
    }

    public void setStageTargetDivision(int stageTargetDivision) {
        this.stageTargetDivision = stageTargetDivision;
    }

    public int getStageBattles() {
        return stageBattles;
    }

    public void setStageBattles(int stageBattles) {
        this.stageBattles = stageBattles;
    }

    public String getStageProgressList() {
        return stageProgressList;
    }

    public void setStageProgressList(String stageProgressList) {
        this.stageProgressList = stageProgressList;
    }

    public String getStageTarget() {
        return stageTarget;
    }

    public void setStageTarget(String stageTarget) {
        this.stageTarget = stageTarget;
    }

    public int getStageTargetPublicRating() {
        return stageTargetPublicRating;
    }

    public void setStageTargetPublicRating(int stageTargetPublicRating) {
        this.stageTargetPublicRating = stageTargetPublicRating;
    }
}
