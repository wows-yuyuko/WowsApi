package com.shinoaki.wows.api.data.ship;

/**
 * @param shots 发射数量
 * @param hits  命中数量
 * @author Xun
 * @date 2023/4/8 16:21 星期六
 */
public record HitRatio(long shots, long hits) {

    public static HitRatio empty(){
        return new HitRatio(0,0);
    }

    /**
     * 命中率
     *
     * @return 命中率
     */
    public double hitRatio() {
        if (this.hits() <= 0 || this.shots() <= 0) {
            return 0.0;
        }
        return 100.0 * ((double) this.hits() / this.shots());
    }

    /**
     * 相加
     *
     * @param history 旧的历史数据
     * @return 相加后的结果
     */
    public HitRatio addition(HitRatio history) {
        return new HitRatio(this.shots() + history.shots(), this.hits() + history.hits());
    }

    /**
     * 相减
     *
     * @param history 旧的历史数据
     * @return 相减后的结果
     */
    public HitRatio subtraction(HitRatio history) {
        return new HitRatio(this.shots() - history.shots(), this.hits() - history.hits());
    }
}
