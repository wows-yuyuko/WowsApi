package com.shinoaki.wows;

import com.github.luben.zstd.ZstdDictCompress;
import com.github.luben.zstd.ZstdDictDecompress;
import com.shinoaki.wows.api.codec.http.WowsHttpShipTools;
import com.shinoaki.wows.api.data.ShipInfo;
import com.shinoaki.wows.api.type.WowsBattlesType;
import com.shinoaki.wows.api.type.WowsServer;
import com.shinoaki.wows.api.utils.DataCompressXZUtils;
import com.shinoaki.wows.api.utils.DataCompressZSTDUtils;

import java.io.File;
import java.io.IOException;
import java.net.http.HttpClient;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;

public class ZstdDemo {
    private static final String TOKEN = "907d9c6bfc0d896a2c156e57194a97cf";
    private static final HttpClient client = HttpClient.newBuilder()
            .build();

    public static void main(String[] args) throws Exception {
        var tools = new WowsHttpShipTools(client, WowsServer.ASIA, 2022515210L);
        var shipInfoMap = tools.developers(TOKEN).shipList().toShipInfoMap();
        xz(shipInfoMap);
        System.out.println("====================================================");
        zstdDict(shipInfoMap);
        System.out.println("====================================================");
        zstd(shipInfoMap);
    }

    public static void zstd(Map<WowsBattlesType, List<ShipInfo>> data) throws IOException, ClassNotFoundException {
        long s = System.currentTimeMillis();
        var byteArray = DataCompressZSTDUtils.encode(data);
        Files.write(new File("2022515210ship.zstd1").toPath(), byteArray.toByteArray());
        System.out.println("zstd压缩耗时:" + (System.currentTimeMillis() - s));
        long a = System.currentTimeMillis();
        DataCompressZSTDUtils.decode(byteArray);
        System.out.println("zstd解压耗时:" + (System.currentTimeMillis() - a));
    }

    public static void zstdDict(Map<WowsBattlesType, List<ShipInfo>> data) throws IOException, ClassNotFoundException {
        var dictFile = new File("ship.dict.zstd");
        ZstdDictCompress dictCompress = DataCompressZSTDUtils.zstdDictCompress(dictFile);
        ZstdDictDecompress dictDecompress = DataCompressZSTDUtils.zstdDictDecompress(dictFile);
        long s = System.currentTimeMillis();
        var byteArray = DataCompressZSTDUtils.encode(data, dictCompress);
        Files.write(new File("2022515210ship.zstd2").toPath(), byteArray.toByteArray());
        System.out.println("zstd字典压缩耗时:" + (System.currentTimeMillis() - s));
        long a = System.currentTimeMillis();
        DataCompressZSTDUtils.decode(byteArray, dictDecompress);
        System.out.println("zstd字典解压耗时:" + (System.currentTimeMillis() - a));
    }

    public static void xz(Map<WowsBattlesType, List<ShipInfo>> data) throws IOException, ClassNotFoundException {
        long s = System.currentTimeMillis();
        var byteArray = DataCompressXZUtils.encode(data);
        Files.write(new File("2022515210ship.xz").toPath(), byteArray.toByteArray());
        System.out.println("xz压缩耗时:" + (System.currentTimeMillis() - s));
        long a = System.currentTimeMillis();
        DataCompressXZUtils.decode(byteArray);
        System.out.println("xz解压耗时:" + (System.currentTimeMillis() - a));
    }
}
