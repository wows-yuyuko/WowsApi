package com.shinoaki.wows.api.error;

import lombok.Getter;

/**
 * 用户隐藏战绩异常
 * <p>
 * 当查询的账号隐藏了战绩（hidden_profile=true）时抛出。
 * 这是受检异常（继承自{@link BasicException}），调用方必须显式捕获或向上声明，
 * 不能静默忽略；需要区分隐藏战绩场景时，可直接捕获本异常类型。
 *
 * @author Xun
 */
@Getter
public class HiddenProfileException extends BasicException {
    private final long accountId;

    public HiddenProfileException(long accountId) {
        super(HttpThrowableStatus.HIDDEN, "账号" + accountId + "隐藏了战绩!");
        this.accountId = accountId;
    }
}
