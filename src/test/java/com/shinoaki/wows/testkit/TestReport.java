package com.shinoaki.wows.testkit;

/**
 * 测试报告器：通过/失败计数、断言集与"新旧对比"统一口径。
 * <p>
 * 在线对比口径（{@link #compare}）：{@link CanonicalJson} 规范化（剔 recordTime + 对象键序无关 +
 * 数组多重集）后比较；失败自动**重试一次**（覆盖连接超时、数据在两次请求之间更新、边缘缓存差异等
 * 真实服务瞬时抖动），两次都不一致才判失败并打印首个差异位置。
 *
 * @author Xun
 */
public final class TestReport {

    private final String title;
    private int passed;
    private int failed;

    public TestReport(String title) {
        this.title = title;
    }

    public void section(String title) {
        System.out.println();
        System.out.println("== " + title + " ==");
    }

    public void ok(String name) {
        passed++;
        System.out.println("  [ok] " + name);
    }

    public void fail(String name) {
        failed++;
        System.out.println("  [FAIL] " + name);
    }

    public void expect(String name, Object expected, Object actual) {
        if (String.valueOf(expected).equals(String.valueOf(actual))) {
            ok(name);
        } else {
            fail(name + "\n         期望: " + expected + "\n         实际: " + actual);
        }
    }

    public Throwable expectThrows(String name, Class<? extends Throwable> type, ThrowingRunnable runnable) {
        try {
            runnable.run();
            fail(name + "（没有抛异常）");
            return null;
        } catch (Throwable e) {
            if (type.isInstance(e)) {
                ok(name);
                return e;
            }
            fail(name + "\n         期望: " + type.getSimpleName() + "\n         实际: " + e.getClass().getSimpleName() + " " + e.getMessage());
            return e;
        }
    }

    /**
     * 新旧实现对比（各执行一次，取 JSON 规范化后比较；不一致自动重试一次）。
     */
    public void compare(String name, ThrowingSupplier<String> oldCall, ThrowingSupplier<String> newCall) {
        Comparison first = compareOnce(oldCall, newCall);
        if (first.ok()) {
            ok(name + "（" + first.oldLength() + " 字符一致）");
            return;
        }
        //真实服务存在瞬时抖动——重试一次
        System.out.println("  [..] " + name + " 首次不一致，重试一次" + first.detail());
        Comparison second = compareOnce(oldCall, newCall);
        if (second.ok()) {
            ok(name + "（重试后一致，" + second.oldLength() + " 字符）");
        } else {
            fail(name + second.detail());
        }
    }

    /**
     * 两侧都失败且异常类型一致 = 通过（如莱服确认不提供的接口）；否则按 {@link #compare} 处理。
     */
    public void compareAllowBothFail(String name, ThrowingSupplier<String> oldCall, ThrowingSupplier<String> newCall) {
        Exception oldError = null;
        try {
            oldCall.get();
        } catch (Exception e) {
            oldError = e;
        }
        Exception newError = null;
        try {
            newCall.get();
        } catch (Exception e) {
            newError = e;
        }
        if (oldError != null && newError != null) {
            if (oldError.getClass() == newError.getClass()) {
                ok(name + "（两侧一致失败: " + oldError.getClass().getSimpleName() + "）");
            } else {
                fail(name + "（两侧失败但类型不同: " + oldError.getClass().getSimpleName() + " vs " + newError.getClass().getSimpleName() + "）");
            }
            return;
        }
        if (oldError != null) {
            fail(name + "（旧实现异常: " + oldError + "）");
            return;
        }
        if (newError != null) {
            fail(name + "（新实现异常: " + newError + "）");
            return;
        }
        compare(name, oldCall, newCall);
    }

    /**
     * 打印汇总并以失败项数作为退出码。
     */
    public void finish() {
        System.out.println();
        System.out.printf("── 汇总（%s）── 通过 %d，失败 %d%n", title, passed, failed);
        System.exit(failed == 0 ? 0 : 1);
    }

    private Comparison compareOnce(ThrowingSupplier<String> oldCall, ThrowingSupplier<String> newCall) {
        String oldValue;
        try {
            oldValue = oldCall.get();
        } catch (Exception e) {
            return new Comparison(false, "（旧实现异常: " + e + "）", 0);
        }
        String newValue;
        try {
            newValue = newCall.get();
        } catch (Exception e) {
            return new Comparison(false, "（新实现异常: " + e + "）", 0);
        }
        String canonicalOld = CanonicalJson.of(oldValue);
        String canonicalNew = CanonicalJson.of(newValue);
        if (canonicalOld.equals(canonicalNew)) {
            return new Comparison(true, null, oldValue.length());
        }
        int at = firstDiff(canonicalOld, canonicalNew);
        return new Comparison(false,
                "\n         首个差异 @" + at + "（规范化后长度 " + canonicalOld.length() + " / " + canonicalNew.length() + "）"
                        + "\n         旧: ..." + around(canonicalOld, at) + "\n         新: ..." + around(canonicalNew, at),
                oldValue.length());
    }

    private static int firstDiff(String a, String b) {
        int max = Math.min(a.length(), b.length());
        int i = 0;
        while (i < max && a.charAt(i) == b.charAt(i)) {
            i++;
        }
        return i;
    }

    private static String around(String value, int index) {
        int from = Math.max(0, index - 120);
        int to = Math.min(value.length(), index + 200);
        return value.substring(from, to);
    }

    private record Comparison(boolean ok, String detail, int oldLength) {
    }

    @FunctionalInterface
    public interface ThrowingRunnable {
        void run() throws Exception;
    }

    @FunctionalInterface
    public interface ThrowingSupplier<T> {
        T get() throws Exception;
    }
}
