package com.shinoaki.wows.testkit;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 假上游收到的请求日志（三主机共用一份，按到达顺序）。
 *
 * @author Xun
 */
public final class RequestLog {

    private final List<RecordedRequest> records = new CopyOnWriteArrayList<>();

    public void add(RecordedRequest request) {
        records.add(request);
    }

    public void clear() {
        records.clear();
    }

    public int size() {
        return records.size();
    }

    public RecordedRequest last() {
        return records.get(records.size() - 1);
    }

    public RecordedRequest get(int index) {
        return records.get(index);
    }
}
