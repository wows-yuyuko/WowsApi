package com.shinoaki.wows.api.error;

import com.shinoaki.wows.api.type.WowsServer;

/**
 * 端点/参数在当前服务器不支持：统一替代旧实现的 {@code IllegalArgumentException} 与"静默空表"；
 * CN 调用 dev 端点也走本异常（发送前 fast-fail）。
 *
 * @author Xun
 */
public class UnsupportedOnServerException extends BasicException {

    private final WowsServer server;
    private final String subject;

    /**
     * @param subject 不支持的主体描述，如 {@code 端点 dev.account.info} 或 {@code 战斗类型 BRAWL}
     * @param server  目标服务器
     * @param detail  补充说明（如该类型支持的服务器列表）；可为 null
     */
    public UnsupportedOnServerException(String subject, WowsServer server, String detail) {
        super(HttpThrowableStatus.UNSUPPORTED_ON_SERVER,
                subject + " 在 " + server.getCode() + " 不支持" + (detail == null || detail.isBlank() ? "" : "（" + detail + "）"));
        this.server = server;
        this.subject = subject;
    }

    public UnsupportedOnServerException(String subject, WowsServer server) {
        this(subject, server, null);
    }

    public WowsServer getServer() {
        return server;
    }

    public String getSubject() {
        return subject;
    }
}
