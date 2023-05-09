package com.shinoaki.wows.api.utils;

import java.util.*;

/**
 * @author Xun
 * @date 2023/04/25 星期二
 */
public class WorkUtils {
    private WorkUtils() {
    }

    public static <T> List<List<T>> work(List<T> infoList, int workChunkTarget) {
        List<List<T>> workList = new ArrayList<>();
        Deque<Spliterator<T>> spliterators = new ArrayDeque<>();
        spliterators.add(infoList.stream().parallel().spliterator());
        while (!spliterators.isEmpty()) {
            Spliterator<T> spliterator = spliterators.pop();
            Spliterator<T> prefix;
            while (spliterator.estimateSize() > workChunkTarget && (prefix = spliterator.trySplit()) != null) {
                spliterators.push(spliterator);
                spliterator = prefix;
            }
            List<T> works = new ArrayList<>();
            spliterator.forEachRemaining(works::add);
            workList.add(works);
        }
        return workList;
    }
}
