package com.shinoaki.wows.api.vortex;

import lombok.Getter;

public enum VortexResourcesLanguage {
    SG("zh-sg"),
    CN("zh-cn"),
    EN("en"),
    RU("ru");

    @Getter
    private final String language;

    VortexResourcesLanguage(String language) {
        this.language = language;
    }
}
