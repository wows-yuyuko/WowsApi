package com.shinoaki.wows;

import com.shinoaki.wows.api.error.BasicException;
import com.shinoaki.wows.api.error.HiddenProfileException;
import com.shinoaki.wows.api.error.HttpNetworkException;
import com.shinoaki.wows.api.error.HttpStatusException;
import com.shinoaki.wows.api.error.HttpThrowableStatus;
import com.shinoaki.wows.api.error.ResponseParseException;
import com.shinoaki.wows.api.utils.JsonUtils;

/**
 * 异常体系自检（离线）：网络 / 状态码 / 解析 / 业务 四类异常的可辨识性、错误码与结构化字段。
 * <p>
 * 用法：{@code java -cp ... com.shinoaki.wows.ExceptionHierarchyTest}；退出码 0 = 全部通过。
 *
 * @author Xun
 */
public class ExceptionHierarchyTest {

    private static int failed = 0;

    public static void main(String[] args) {
        section("体系与错误码");
        var net = new HttpNetworkException("连接超时");
        var status = new HttpStatusException(503, "http状态码异常 code=503");
        var parse = new ResponseParseException("响应解析失败");
        var hidden = new HiddenProfileException(2022515210L);

        expect("HttpNetworkException 是 BasicException", true, net instanceof BasicException);
        expect("HttpNetworkException.code", HttpThrowableStatus.HTTP_IO, net.getCode());
        expect("HttpStatusException 是 BasicException", true, status instanceof BasicException);
        expect("HttpStatusException.code", HttpThrowableStatus.HTTP_STATUS, status.getCode());
        expect("HttpStatusException.statusCode", 503, status.getStatusCode());
        expect("ResponseParseException 是 BasicException", true, parse instanceof BasicException);
        expect("ResponseParseException.code", HttpThrowableStatus.DATA_PARSE, parse.getCode());
        expect("HiddenProfileException 仍是独立子类", true, hidden instanceof BasicException);
        expect("HiddenProfileException.code", HttpThrowableStatus.HIDDEN, hidden.getCode());

        section("响应解析包装（parseResponse）");
        try {
            JsonUtils.json().parseResponse("<html><body>502 Bad Gateway</body></html>");
            expect("非法JSON应抛 ResponseParseException", "抛异常", "没有抛异常");
        } catch (ResponseParseException e) {
            expect("非法JSON应抛 ResponseParseException", true, true);
            expect("code 为 DATA_PARSE", HttpThrowableStatus.DATA_PARSE, e.getCode());
            expect("消息包含响应摘要", true, e.getMessage().contains("502 Bad Gateway"));
            expect("原因链保留", true, e.getCause() != null);
        }
        try {
            var node = JsonUtils.json().parseResponse("{\"status\":\"ok\",\"data\":{\"id\":1}}");
            expect("合法JSON正常解析", 1L, node.path("data").path("id").asLong(0));
        } catch (ResponseParseException e) {
            expect("合法JSON正常解析", "无异常", e.getMessage());
        }

        section("业务状态异常仍走基类（DATA_STATUS）");
        try {
            BasicException.status(JsonUtils.json().parse("{\"status\":\"error\"}"));
            expect("status≠ok 应抛 BasicException", "抛异常", "没有抛异常");
        } catch (BasicException e) {
            expect("status≠ok 应抛 BasicException", HttpThrowableStatus.DATA_STATUS, e.getCode());
            expect("且不是解析类异常", false, e instanceof ResponseParseException);
        }

        System.out.println();
        System.out.println(failed == 0 ? "结果: 异常体系自检通过" : "结果: " + failed + " 项失败");
        System.exit(failed == 0 ? 0 : 1);
    }

    private static void section(String title) {
        System.out.println();
        System.out.println("── " + title + " ──");
    }

    private static void expect(String name, Object expected, Object actual) {
        if (String.valueOf(expected).equals(String.valueOf(actual))) {
            System.out.println("  [ok] " + name + " = " + actual);
        } else {
            failed++;
            System.out.println("  [FAIL] " + name + " 期望=" + expected + " 实际=" + actual);
        }
    }
}
