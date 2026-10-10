package com.shinoaki.wows.api.vortex.resources.glossary;

import lombok.Data;
import tools.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.List;

/**
 * 成就(含勋章)，对应 getGlossData 的 {@code achievements[]}。
 *
 * @author Xun
 */
@Data
public class VortexAchievement {
    private String id;
    private String title;
    private String description;
    private String type;
    private String typeTitle;
    private boolean enabled;
    private boolean multiple;
    private boolean hidden;
    private boolean isProgress;
    private int maxProgress;
    private VortexAchievementIcons icons;
    /**
     * 适用战斗类型ID
     */
    private List<String> battleTypes;
    private VortexAchievementGrouping grouping;
    /**
     * 战斗限制(服务端多数情况为 null，保留原始节点)
     */
    private JsonNode battleRestriction;
    /**
     * 获取限制(服务端多数情况为 null，保留原始节点)
     */
    private JsonNode receivingRestriction;

    public static List<VortexAchievement> parseList(JsonNode node) {
        List<VortexAchievement> list = new ArrayList<>();
        for (JsonNode achievement : node) {
            VortexAchievement data = new VortexAchievement();
            data.setId(achievement.path("id").asString(""));
            data.setTitle(achievement.path("title").asString(""));
            data.setDescription(achievement.path("description").asString(""));
            data.setType(achievement.path("type").asString(""));
            data.setTypeTitle(achievement.path("typeTitle").asString(""));
            data.setEnabled(achievement.path("enabled").asBoolean(false));
            data.setMultiple(achievement.path("multiple").asBoolean(false));
            data.setHidden(achievement.path("hidden").asBoolean(false));
            data.setProgress(achievement.path("isProgress").asBoolean(false));
            data.setMaxProgress(achievement.path("maxProgress").asInt(0));
            data.setIcons(VortexAchievementIcons.parse(achievement.path("icons")));
            List<String> battleTypes = new ArrayList<>();
            for (JsonNode battleType : achievement.path("battleTypes")) {
                battleTypes.add(battleType.path("id").asString(""));
            }
            data.setBattleTypes(battleTypes);
            data.setGrouping(VortexAchievementGrouping.parse(achievement.path("grouping")));
            data.setBattleRestriction(achievement.path("battleRestriction"));
            data.setReceivingRestriction(achievement.path("receivingRestriction"));
            list.add(data);
        }
        return list;
    }
}
