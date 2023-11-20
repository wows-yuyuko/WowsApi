package com.shinoaki.wows.api.utils;

import com.shinoaki.wows.api.data.ShipInfo;
import com.shinoaki.wows.api.type.WowsBattlesType;
import org.tukaani.xz.LZMA2Options;
import org.tukaani.xz.XZInputStream;
import org.tukaani.xz.XZOutputStream;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 使用xz压缩
 *
 * @author Xun
 */
public class DataCompressXZUtils {
    private DataCompressXZUtils() {

    }

    public static ByteArrayOutputStream encode(Map<WowsBattlesType, List<ShipInfo>> map) throws IOException {
        ByteArrayOutputStream r = new ByteArrayOutputStream();
        try (ByteArrayOutputStream byteArray = new ByteArrayOutputStream()) {
            try (ObjectOutputStream out = new ObjectOutputStream(byteArray)) {
                out.writeObject(map);
                out.flush();
            }
            //压缩
            LZMA2Options options = new LZMA2Options();
            options.setPreset(6);
            try (XZOutputStream out = new XZOutputStream(r, options)) {
                out.write(byteArray.toByteArray());
                out.flush();
            }
        }
        return r;
    }

    public static Map<WowsBattlesType, List<ShipInfo>> decode(ByteArrayOutputStream r) throws IOException, ClassNotFoundException {
        try (XZInputStream in = new XZInputStream(new ByteArrayInputStream(r.toByteArray()))) {
            try (ObjectInputStream inputStream = new ObjectInputStream(in)) {
                return castMapList(inputStream.readObject(), WowsBattlesType.class, ShipInfo.class);
            }
        }
    }

    public static <K, V> Map<K, List<V>> castMapList(Object object, Class<K> kClass, Class<V> vClass) {
        Map<K, List<V>> kvMap = new HashMap<>();
        if (object instanceof Map<?, ?> map) {
            for (var entry : map.entrySet()) {
                kvMap.put(kClass.cast(entry.getKey()), castList(entry.getValue(), vClass));
            }
        }
        return kvMap;
    }

    public static <T> List<T> castList(Object object, Class<T> tClass) {
        List<T> data = new ArrayList<>();
        if (object instanceof List<?> list) {
            for (var l : list) {
                data.add(tClass.cast(l));
            }
        }
        return data;
    }
}
