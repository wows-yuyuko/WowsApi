package com.shinoaki.wows.api.developers.encyclopedia.crewskills;

import java.util.List;

/**
 * 某舰种下的技能位置与强化项。
 *
 * @param column 所在列
 * @param tier   所在层级
 * @param perks  强化效果
 * @author Xun
 */
public record DevelopersSkillCustomization(int column, int tier, List<DevelopersSkillPerk> perks) {
}
