package com.shinoaki.wows.api.utils;

import com.github.luben.zstd.Zstd;
import com.github.luben.zstd.ZstdCompressCtx;
import com.github.luben.zstd.ZstdDictCompress;
import com.github.luben.zstd.ZstdDictDecompress;
import com.shinoaki.wows.api.data.ShipInfo;
import com.shinoaki.wows.api.type.WowsBattlesType;

import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DataCompressZSTDUtils {
    private DataCompressZSTDUtils() {
    }

    public static ZstdDictCompress zstdDictCompress(File dictFile) throws IOException {
        byte[] bytes = Files.readAllBytes(dictFile.toPath());
        return new ZstdDictCompress(bytes, 0, bytes.length, 10);
    }

    public static ZstdDictDecompress zstdDictDecompress(File dictFile) throws IOException {
        byte[] bytes = Files.readAllBytes(dictFile.toPath());
        return new ZstdDictDecompress(bytes);
    }


    public static ByteArrayOutputStream encode(Map<WowsBattlesType, List<ShipInfo>> map) throws IOException {
        try (ByteArrayOutputStream byteArray = new ByteArrayOutputStream()) {
            try (ObjectOutputStream out = new ObjectOutputStream(byteArray)) {
                out.writeObject(map);
                out.flush();
            }
            //压缩
            return encode(byteArray.toByteArray());
        }
    }

    public static ByteArrayOutputStream encode(Map<WowsBattlesType, List<ShipInfo>> map, ZstdDictCompress compress) throws IOException {
        try (ByteArrayOutputStream byteArray = new ByteArrayOutputStream()) {
            try (ObjectOutputStream out = new ObjectOutputStream(byteArray)) {
                out.writeObject(map);
                out.flush();
            }
            //压缩
            return encode(byteArray.toByteArray(), compress);
        }
    }

    public static ByteArrayOutputStream encode(byte[] data, ZstdDictCompress compress) throws IOException {
        var r = new ByteArrayOutputStream();
        try (ZstdCompressCtx ctx = new ZstdCompressCtx()) {
            ctx.loadDict(compress);
            ctx.setLevel(10);
//            ctx.setWorkers(4);
            r.write(ctx.compress(data));
        }
        return r;
    }

    public static ByteArrayOutputStream encode(byte[] data) throws IOException {
        var r = new ByteArrayOutputStream();
        try (ZstdCompressCtx ctx = new ZstdCompressCtx()) {
            ctx.setLevel(10);
//            ctx.setWorkers(4);
            r.write(ctx.compress(data));
        }
        return r;
    }

    public static Map<WowsBattlesType, List<ShipInfo>> decode(ByteArrayOutputStream r, ZstdDictDecompress dictDecompress) throws IOException,
            ClassNotFoundException {
        var b = r.toByteArray();
        long size = Zstd.getFrameContentSize(b);
        byte[] decompress = Zstd.decompress(b, dictDecompress, Long.valueOf(size).intValue());
        try (ObjectInputStream inputStream = new ObjectInputStream(new ByteArrayInputStream(decompress))) {
            return castMapList(inputStream.readObject(), WowsBattlesType.class, ShipInfo.class);
        }
    }

    public static Map<WowsBattlesType, List<ShipInfo>> decode(ByteArrayOutputStream r) throws IOException, ClassNotFoundException {
        var b = r.toByteArray();
        long size = Zstd.getFrameContentSize(b);
        byte[] decompress = Zstd.decompress(b, Long.valueOf(size).intValue());
        try (ObjectInputStream inputStream = new ObjectInputStream(new ByteArrayInputStream(decompress))) {
            return castMapList(inputStream.readObject(), WowsBattlesType.class, ShipInfo.class);
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
