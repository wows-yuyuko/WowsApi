package com.shinoaki.wows.api.vortex.resources.vehicles;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.util.Objects;

/**
 * @author Xun
 * @date 2023/12/9 星期六
 */
@Data
public class VortexVehicles {
    @JsonIgnore
    private String language;
    private Boolean isPremium;
    private Boolean isSpecial;
    private String id;
    private String title;
    private String titleShort;
    private VortexShipIcons icons;
    private VortexShipType type;
    private Integer level;
    private VortexShipNation nation;
    private String nationName;





    public boolean isPremium() {
        return isPremium;
    }

    public boolean isSpecial() {
        return isSpecial;
    }

    public String id() {
        return id;
    }

    public String title() {
        return title;
    }

    public String titleShort() {
        return titleShort;
    }

    public VortexShipIcons icons() {
        return icons;
    }

    public VortexShipType type() {
        return type;
    }

    public Integer level() {
        return level;
    }

    public VortexShipNation nation() {
        return nation;
    }

    public String nationName() {
        return nationName;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (VortexVehicles) obj;
        return this.isPremium == that.isPremium &&
               this.isSpecial == that.isSpecial &&
               Objects.equals(this.id, that.id) &&
               Objects.equals(this.title, that.title) &&
               Objects.equals(this.titleShort, that.titleShort) &&
               Objects.equals(this.icons, that.icons) &&
               Objects.equals(this.type, that.type) &&
               Objects.equals(this.level, that.level) &&
               Objects.equals(this.nation, that.nation) &&
               Objects.equals(this.nationName, that.nationName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(isPremium, isSpecial, id, title, titleShort, icons, type, level, nation, nationName);
    }

    @Override
    public String toString() {
        return "RequestVortexShipInfo[" +
               "isPremium=" + isPremium + ", " +
               "isSpecial=" + isSpecial + ", " +
               "id=" + id + ", " +
               "title=" + title + ", " +
               "titleShort=" + titleShort + ", " +
               "icons=" + icons + ", " +
               "type=" + type + ", " +
               "level=" + level + ", " +
               "nation=" + nation + ", " +
               "nationName=" + nationName + ']';
    }

}
