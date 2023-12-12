package com.shinoaki.wows.api.type;

/**
 * @author Xun
 * @date 2023/3/18 14:40 星期六
 */
public enum WowsServer {
    CN("cn", "wowsgame.cn", "国服", false),
    ASIA("asia", "worldofwarships.asia", "亚服", true),
    EU("eu", "worldofwarships.eu", "欧服", true),
    NA("na", "worldofwarships.com", "美服", true),
    RU("ru", "korabli.su", "俄服", true);

    private final String code;
    private final String url;
    private final String name;
    /**
     * 是否支持官方API请求
     */
    private final boolean api;

    WowsServer(String code, String url, String name, boolean api) {
        this.code = code;
        this.url = url;
        this.name = name;
        this.api = api;
    }

    public static WowsServer findCodeByNull(String code) {
        for (var v : WowsServer.values()) {
            if (v.getCode().equalsIgnoreCase(code)) {
                return v;
            }
        }
        return null;
    }

    public static WowsServer findCode(String code) {
        for (var v : WowsServer.values()) {
            if (v.getCode().equalsIgnoreCase(code)) {
                return v;
            }
        }
        throw new NullPointerException(code + " 匹配不到对应服务器");
    }

    public String getCode() {
        return code;
    }

    public String vortex() {
        return "https://vortex." + url;
    }

    public String clans() {
        return "https://clans." + url;
    }

    public String api() {
        return "https://api." + url;
    }

    public String getName() {
        return name;
    }

    public boolean isApi() {
        return api;
    }
}
