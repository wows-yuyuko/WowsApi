package com.shinoaki.wows.api.data;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.shinoaki.wows.api.error.HttpStatusException;
import com.shinoaki.wows.api.error.StatusException;

import java.io.IOException;

/**
 * @author Xun
 * create or update time = 2023/8/13 18:12 星期日
 */
public record CompletableInfo<T>(Throwable throwable, HttpThrowableStatus status, T data) {

    public boolean isErr() {
        return HttpThrowableStatus.SUCCESS == status;
    }

    public static <T> CompletableInfo<T> ok(T data) {
        return new CompletableInfo<>(null, HttpThrowableStatus.SUCCESS, data);
    }

    public static <T> CompletableInfo<T> error(Exception throwable, T data) {
        HttpThrowableStatus status;
        if (throwable instanceof JsonProcessingException) {
            status = HttpThrowableStatus.DATA_PARSE;
        } else if (throwable instanceof HttpStatusException) {
            status = HttpThrowableStatus.HTTP_STATUS;
        } else if (throwable instanceof StatusException) {
            status = HttpThrowableStatus.DATA_STATUS;
        } else if (throwable instanceof IOException) {
            status = HttpThrowableStatus.HTTP_IO;
        } else {
            status = HttpThrowableStatus.ERROR;
        }
        return new CompletableInfo<>(throwable, status, data);
    }

    public static <T> CompletableInfo<T> error(Exception throwable) {
        return error(throwable, null);
    }

    public static <T, E> CompletableInfo<T> copy(CompletableInfo<E> source, T data) {
        return new CompletableInfo<>(source.throwable, source.status, data);
    }

    public static <T> CompletableInfo<T> NULL() {
        return new CompletableInfo<>(null, HttpThrowableStatus.NULL, null);
    }
}
