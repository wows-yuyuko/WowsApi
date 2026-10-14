package com.shinoaki.wows.api.developers.encyclopedia.modules;

import com.shinoaki.wows.api.developers.encyclopedia.shipprofile.DevelopersShipDiveBomber;
import com.shinoaki.wows.api.developers.encyclopedia.shipprofile.DevelopersShipEngineProfile;
import com.shinoaki.wows.api.developers.encyclopedia.shipprofile.DevelopersShipFighters;
import com.shinoaki.wows.api.developers.encyclopedia.shipprofile.DevelopersShipFireControl;
import com.shinoaki.wows.api.developers.encyclopedia.shipprofile.DevelopersShipFlightControl;
import com.shinoaki.wows.api.developers.encyclopedia.shipprofile.DevelopersShipHullProfile;
import com.shinoaki.wows.api.developers.encyclopedia.shipprofile.DevelopersShipSonarProfile;
import com.shinoaki.wows.api.developers.encyclopedia.shipprofile.DevelopersShipTorpedoBomber;

/**
 * 模块属性：按模块类型只有一个块有值（其余为 null）。
 * <p>
 * 与舰船参数包（shipprofile）同构的块直接复用其结构体（模块侧字段是舰船侧的子集）。
 *
 * @author Xun
 */
public record DevelopersModuleProfile(DevelopersModuleArtillery artillery, DevelopersShipDiveBomber dive_bomber,
                                      DevelopersShipEngineProfile engine, DevelopersShipFighters fighter,
                                      DevelopersShipFireControl fire_control, DevelopersShipFlightControl flight_control,
                                      DevelopersShipHullProfile hull, DevelopersShipSonarProfile submarine_sonar,
                                      DevelopersShipTorpedoBomber torpedo_bomber, DevelopersModuleTorpedoes torpedoes) {
}
