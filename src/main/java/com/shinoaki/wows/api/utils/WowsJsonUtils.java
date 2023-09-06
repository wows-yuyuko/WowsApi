package com.shinoaki.wows.api.utils;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.json.JsonReadFeature;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.shinoaki.wows.api.error.BasicException;

import static com.fasterxml.jackson.databind.json.JsonMapper.builder;

/**
 * @author Xun
 * @date 2023/3/18 13:51 星期六
 */
public class WowsJsonUtils {
    /**
     * JSON工具
     */
    private final ObjectMapper OBJECT_MAPPER;

    public WowsJsonUtils() {
        OBJECT_MAPPER = load(null).build();
    }


    private JsonMapper.Builder load(PropertyNamingStrategy strategy) {
        return builder().serializationInclusion(JsonInclude.Include.ALWAYS)
                .propertyNamingStrategy(strategy)
                .configure(MapperFeature.PROPAGATE_TRANSIENT_MARKER, true)
                .configure(JsonReadFeature.ALLOW_BACKSLASH_ESCAPING_ANY_CHARACTER.mappedFeature(), true)
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    /**
     * 解析JSON文件返回Java Bean
     *
     * @param json   json数据
     * @param tClass 类型
     * @param <T>    类型T
     * @return Java Bean
     */
    public <T> T parse(String json, Class<T> tClass) throws BasicException {
        try {
            return OBJECT_MAPPER.readValue(json, tClass);
        } catch (JsonProcessingException e) {
            throw new BasicException(e);
        }
    }

    /**
     * 解析JSON文件返回Java Bean
     *
     * @param json   json数据
     * @param tClass 类型
     * @param <T>    类型T
     * @return Java Bean
     */
    public <T> T parse(JsonNode json, Class<T> tClass) throws BasicException {
        try {
            return OBJECT_MAPPER.treeToValue(json, tClass);
        } catch (JsonProcessingException e) {
            throw new BasicException(e);
        }
    }

    /**
     * 解析JSON文件返回JsonNode
     *
     * @param json json数据
     * @return JsonNode
     */
    public JsonNode parse(String json) throws BasicException {
        return parse(json, JsonNode.class);
    }

    /**
     * 解析JSON文件返回Java Bean-应对Java复杂类型的Bean
     *
     * @param json json数据
     * @param type 复杂类型
     * @param <T>  类型T
     * @return Java Bean
     */
    public <T> T parse(String json, TypeReference<T> type) throws BasicException {
        try {
            return OBJECT_MAPPER.readValue(json, type);
        } catch (JsonProcessingException e) {
            throw new BasicException(e);
        }
    }
}
