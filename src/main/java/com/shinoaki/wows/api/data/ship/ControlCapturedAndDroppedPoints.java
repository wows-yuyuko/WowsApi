package com.shinoaki.wows.api.data.ship;

import com.shinoaki.wows.api.developers.warships.type.DevelopersShipBattleType;
import com.shinoaki.wows.api.vortex.ship.VortexShipInfo;

import java.io.Serializable;

/**
 * @param teamControlCapturedPoints 团队控制占领点(机翻) 团队防御点
 * @param controlCapturedPoints     控制捕获点(机翻)   基地占领点
 * @param teamControlDroppedPoints  团队控制丢分(机翻)  团队占领点
 * @param controlDroppedPoints      控制掉点(机翻)    占领时被打掉的点数
 * @param droppedCapturePoints      掉落的占领点(机翻) 基地防御点
 * @param capturePoints
 * @author Xun
 * @date 2023/4/9 15:27 星期日
 */
public record ControlCapturedAndDroppedPoints(long teamControlCapturedPoints, long controlCapturedPoints,
                                              long teamControlDroppedPoints,
                                              long controlDroppedPoints, long droppedCapturePoints,
                                              long capturePoints) implements Serializable {

    public static ControlCapturedAndDroppedPoints empty() {
        return new ControlCapturedAndDroppedPoints(0, 0, 0, 0, 0, 0);
    }

    public static ControlCapturedAndDroppedPoints to(VortexShipInfo info) {
        return new ControlCapturedAndDroppedPoints(info.team_control_captured_points(),
                info.control_captured_points(),
                info.team_control_dropped_points(),
                info.control_dropped_points(),
                info.dropped_capture_points(),
                info.capture_points());
    }

    public static ControlCapturedAndDroppedPoints to(DevelopersShipBattleType info) {
        return new ControlCapturedAndDroppedPoints(info.team_capture_points(),
                info.capture_points(),
                info.team_dropped_capture_points(),
                info.dropped_capture_points(),
                0,
                0);
    }

    /**
     * 相加
     *
     * @param history 旧的历史数据
     * @return 相加后的结果
     */
    public ControlCapturedAndDroppedPoints addition(ControlCapturedAndDroppedPoints history) {
        return new ControlCapturedAndDroppedPoints(
                this.teamControlCapturedPoints() + history.teamControlCapturedPoints(),
                this.controlCapturedPoints() + history.controlCapturedPoints(),
                this.teamControlDroppedPoints() + history.teamControlDroppedPoints(),
                this.controlDroppedPoints() + history.controlDroppedPoints(),
                this.droppedCapturePoints() + history.droppedCapturePoints(),
                this.capturePoints() + history.capturePoints());
    }

    /**
     * 相减
     *
     * @param history 旧的历史数据
     * @return 相减后的结果
     */
    public ControlCapturedAndDroppedPoints subtraction(ControlCapturedAndDroppedPoints history) {
        return new ControlCapturedAndDroppedPoints(
                this.teamControlCapturedPoints() - history.teamControlCapturedPoints(),
                this.controlCapturedPoints() - history.controlCapturedPoints(),
                this.teamControlDroppedPoints() - history.teamControlDroppedPoints(),
                this.controlDroppedPoints() - history.controlDroppedPoints(),
                this.droppedCapturePoints() - history.droppedCapturePoints(),
                this.capturePoints() - history.capturePoints());
    }

    /**
     * 占领贡献
     * <p>
     * 注意：贡献率 = 个人占领点 / 团队占领点，与 {@link com.shinoaki.wows.api.vortex.ship.VortexShipInfo#avgContributionToCapture()} 保持一致；
     * 分母为0时返回0.0（原实现分子分母颠倒且除零会产生Infinity，请勿改回）。
     *
     * @return 进攻贡献率
     */
    public double gameContributionToCapture() {
        if (this.controlCapturedPoints() <= 0 || this.teamControlCapturedPoints() <= 0) {
            return 0.0;
        }
        return ((double) this.controlCapturedPoints() / this.teamControlCapturedPoints()) * 100.0;
    }

    /**
     * 防御贡献
     * <p>
     * 注意：贡献率 = 个人防御点 / 团队防御点，与 {@link com.shinoaki.wows.api.vortex.ship.VortexShipInfo#avgContributionToDefense()} 保持一致；
     * 分母为0时返回0.0。
     *
     * @return 防御贡献率
     */
    public double gameContributionToDefense() {
        if (this.controlDroppedPoints() <= 0 || this.teamControlDroppedPoints() <= 0) {
            return 0.0;
        }
        return ((double) this.controlDroppedPoints() / this.teamControlDroppedPoints()) * 100.0;
    }
}
