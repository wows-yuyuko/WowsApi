package com.shinoaki.wows.api.codec;

import com.shinoaki.wows.api.error.HttpStatusException;

import java.io.IOException;
import java.net.http.HttpResponse;

/**
 * @author Xun
 * @date 2023/5/22 21:44 星期一
 */
public record SyncResult(IOException io, HttpStatusException http, String data) {

    public boolean isErr() {
        return io != null || http != null;
    }

    public static SyncResult data(HttpResponse<byte[]> response) {
        try {
            return new SyncResult(null, null, new String(HttpCodec.response(response)));
        } catch (IOException e) {
            return new SyncResult(e, null, null);
        } catch (HttpStatusException e) {
            return new SyncResult(null, e, null);
        }
    }
}
