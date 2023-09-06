package com.shinoaki.wows.api.error;

import com.fasterxml.jackson.core.JsonProcessingException;

/**
 * @author Xun
 * create or update time = 2023/8/13 18:12 星期日
 */
public record CompletableInfo<T>(BasicException throwable, T data) {

    public boolean isErr() {
        return throwable != null;
    }

    public static <T> CompletableInfo<T> ok(T data) {
        return new CompletableInfo<>(null, data);
    }

    public static <T> CompletableInfo<T> error(BasicException throwable, T data) {
        return new CompletableInfo<>(throwable, data);
    }


    public static <T> CompletableInfo<T> error(BasicException throwable) {
        return new CompletableInfo<>(throwable, null);
    }

    public static <T> CompletableInfo<T> error(JsonProcessingException throwable) {
        return new CompletableInfo<>(new BasicException(throwable), null);
    }


    public static <T, E> CompletableInfo<T> copy(CompletableInfo<E> source, T data) {
        return new CompletableInfo<>(source.throwable, data);
    }
}
