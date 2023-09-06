package com.shinoaki.wows.api.vortex.clan.base.info;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * null
 *
 * @author Xun
 * @date 2022/07/06 星期三 09:47
 */
@Data
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
}
