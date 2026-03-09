package com.shinoaki.wows.api.utils;


import lombok.Getter;
import tools.jackson.core.StreamReadConstraints;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.io.File;
import java.io.IOException;

/**
 * @author Xun
 */
@Getter
public class JsonUtils {
    private static final JsonUtils instance = new JsonUtils();

    private final JsonMapper mapper;

    private JsonUtils() {
        mapper = load().build();
    }

    public static JsonUtils json() {
        return instance;
    }


    private static JsonMapper.Builder load() {
        StreamReadConstraints streamReadConstraints = StreamReadConstraints.builder()
                .maxNumberLength(StreamReadConstraints.DEFAULT_MAX_NUM_LEN)
                .maxNestingDepth(StreamReadConstraints.DEFAULT_MAX_DEPTH)
                .maxStringLength(200_000_000).build();
        StreamReadConstraints.overrideDefaultStreamReadConstraints(streamReadConstraints);
        return JsonMapper.builder()
                .configure(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES, false)
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true);
    }


    /**
     * 序列化Java Bean为JSON数据
     *
     * @param data Java Bean
     * @param <T>  类型
     * @return JSON数据
     */
    public <T> String toJson(T data) {
        return mapper.writeValueAsString(data);
    }

    public <T> void toJson(File file, T data) throws IOException {
        mapper.writeValue(file, data);
    }

    public <T> void toJsonPrettyPrinter(File file, T data) throws IOException {
        mapper.writerWithDefaultPrettyPrinter().writeValue(file, data);
    }

    public <T> byte[] toJsonBytes(T data) {
        return mapper.writeValueAsBytes(data);
    }

    /**
     * 解析JSON文件返回Java Bean
     *
     * @param json   json数据
     * @param tClass 类型
     * @param <T>    类型T
     * @return Java Bean
     */
    public <T> T parse(String json, Class<T> tClass) {
        return mapper.readValue(json, tClass);
    }

    public <T> T parse(File file, Class<T> tClass) {
        return mapper.readValue(file, tClass);
    }

    public <T> T parse(File file, TypeReference<T> type) {
        return mapper.readValue(file, type);
    }

    public <T> T parse(byte[] file, TypeReference<T> type) {
        return mapper.readValue(file, type);
    }

    public <T> T parse(byte[] file, Class<T> tClass) {
        return mapper.readValue(file, tClass);
    }

    /**
     * 解析JSON文件返回Java Bean
     *
     * @param json   json数据
     * @param tClass 类型
     * @param <T>    类型T
     * @return Java Bean
     */
    public <T> T parse(JsonNode json, Class<T> tClass) {
        return mapper.treeToValue(json, tClass);
    }

//    /**
//     * 适用于需要校验的
//     * @param json  提交体
//     * @param tClass    类
//     * @return 结果
//     * @param <T>   序列化类
//     */
//    public <T> T parseValid(JsonNode json, Class<T> tClass) {
//        var object = mapper.treeToValue(json, tClass);
//        try (var factory = Validation.buildDefaultValidatorFactory()) {
//            var validator = factory.getValidator();
//            Set<ConstraintViolation<Object>> violations = validator.validate(object);
//            if (!violations.isEmpty()) {
////                throw new ConstraintViolationException(violations);
//                throw new ResultMessageException(RStatus.CONTENT_DENY);
//            }
//        }
//        return object;
//    }
//
//    /**
//     * 适用于需要校验的
//     * @param json  提交体
//     * @param type 类型
//     * @return 结果
//     * @param <T>   序列化类
//     */
//    public <T> T parseValid(JsonNode json, TypeReference<T> type) {
//        var object = mapper.treeToValue(json, type);
//        try (var factory = Validation.buildDefaultValidatorFactory()) {
//            var validator = factory.getValidator();
//            Set<ConstraintViolation<Object>> violations = validator.validate(object);
//            if (!violations.isEmpty()) {
////                throw new ConstraintViolationException(violations);
//                throw new ResultMessageException(RStatus.CONTENT_DENY);
//            }
//        }
//        return object;
//    }

    /**
     * 解析JSON文件返回Java Bean
     *
     * @param json json数据
     * @param type 类型
     * @param <T>  类型T
     * @return Java Bean
     */
    public <T> T parse(JsonNode json, TypeReference<T> type) {
        return mapper.treeToValue(json, type);
    }

    /**
     * 解析JSON文件返回JsonNode
     *
     * @param json json数据
     * @return JsonNode
     */
    public JsonNode parse(String json) {
        return parse(json, JsonNode.class);
    }

    public JsonNode parse(File json) throws IOException {
        return parse(json, JsonNode.class);
    }

    public JsonNode parse(byte[] json) throws IOException {
        return parse(json, JsonNode.class);
    }

    public JsonNode parseToNull(String json) {
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
    public <T> T parse(String json, TypeReference<T> type) {
        return mapper.readValue(json, type);
    }
}
