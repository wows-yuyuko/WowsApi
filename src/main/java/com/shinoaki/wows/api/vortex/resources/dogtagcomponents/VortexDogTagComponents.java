package com.shinoaki.wows.api.vortex.resources.dogtagcomponents;

import com.shinoaki.wows.api.vortex.resources.WowsIcons;
import lombok.Data;

import java.util.List;

/**
 * @author Xun
 * @date 2023/12/14 星期四
 */
@Data
public class VortexDogTagComponents {
    private String id;
    private String type;
    private String color;
    private Boolean isColorizable;
    private Boolean showClanTag;
    private VortexClanTag clanTag;
    private WowsIcons icons;
    private List<TextureData> textureData;


}
