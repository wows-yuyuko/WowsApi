package com.shinoaki.wows.api.data;

public record DogTag(
        long texture_id,
        long symbol_id,
        long border_color_id,
        long background_color_id,
        long background_id
) {

    public static DogTag empty() {
        return new DogTag(-1, -1, -1, -1, -1);
    }
}
