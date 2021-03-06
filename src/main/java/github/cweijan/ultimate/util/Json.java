package github.cweijan.ultimate.util;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonParser.Feature;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import github.cweijan.ultimate.util.json.deserializer.LocalDateDeserializer;
import github.cweijan.ultimate.util.json.deserializer.LocalDateTimeDeserializer;
import github.cweijan.ultimate.util.json.deserializer.LocalTimeDeserializer;
import github.cweijan.ultimate.util.json.serializer.LocalDateSerializer;
import github.cweijan.ultimate.util.json.serializer.LocalDateTimeSerializer;
import github.cweijan.ultimate.util.json.serializer.LocalTimeSerializer;
import org.slf4j.Logger;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collection;
import java.util.List;

public final class Json {
    private static final ObjectMapper mapper;
    private static final ObjectMapper withEmptyMapper;
    private static final Logger logger = Log.getLogger();

    private Json() {
    }

    static {
        mapper = new ObjectMapper();
        withEmptyMapper = new ObjectMapper();
        SimpleModule dateModule = new SimpleModule();
        dateModule.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer());
        dateModule.addSerializer(LocalDate.class, new LocalDateSerializer());
        dateModule.addSerializer(LocalTime.class, new LocalTimeSerializer());
        dateModule.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer());
        dateModule.addDeserializer(LocalDate.class, new LocalDateDeserializer());
        dateModule.addDeserializer(LocalTime.class, new LocalTimeDeserializer());
        mapper.registerModule(dateModule);
        mapper.setSerializationInclusion(Include.NON_EMPTY);
        withEmptyMapper.setSerializationInclusion(Include.ALWAYS);
        mapper.enable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT);
        mapper.configure(Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
        mapper.configure(Feature.ALLOW_SINGLE_QUOTES, true);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    /**
     * 将对象转换成json
     *
     * @param originalObject 要转换的对象
     */
    public static String toJson(Object originalObject) {

        if (originalObject == null) return null;
        if (originalObject instanceof String) return String.valueOf(originalObject);

        String json = null;
        try {
            json = mapper.writeValueAsString(originalObject);
        } catch (Exception e) {
            Log.error("toJson error:", e);
        }

        return json;
    }

    /**
     * 将对象转换成json
     *
     * @param originalObject 要转换的对象
     */
    public static byte[] toJsonByte(Object originalObject) {

        if (originalObject == null) return null;

        byte[] json = null;
        try {
            json = mapper.writeValueAsBytes(originalObject);
        } catch (Exception e) {
            logger.error("toJson error:", e);
        }

        return json;
    }


    /**
     * 将对象转换成json,并包含空属性
     *
     * @param originalObject 要转换的对象
     */
    public static String toJsonWithEmpty(Object originalObject) {

        if (originalObject == null) return null;
        String json = null;
        try {
            json = withEmptyMapper.writeValueAsString(originalObject);
        } catch (Exception e) {
            logger.error("toJson error:", e);
        }

        return json;
    }


    /**
     * 根据子key获取子json
     *
     * @param json json字符串
     * @param key  json字符串子key
     * @return 子json
     */
    public static String get(String json, String key) {

        if (StringUtils.isEmpty(json) || StringUtils.isEmpty(key)) return null;

        String value;
        try {
            value = mapper.readValue(json, JsonNode.class).get(key).textValue();
        } catch (IOException var5) {
            logger.info(var5.getMessage());
            value = null;
        }

        return value;
    }

    /**
     * 将json转成List
     *
     * @param json      json字符串
     * @param valueType list泛型
     */
    public static <T> List<T> parseList(String json, Class<T> valueType) {

        return (List<T>) parseCollection(json, List.class, valueType);
    }

    /**
     * 将json转成List
     *
     * @param json      json字符串
     * @param valueType list泛型
     */
    public static <T, E extends Collection> Collection<T> parseCollection(String json, Class<E> collectionClass, Class<T> valueType) {

        if (StringUtils.isEmpty(json) || valueType == null) return null;

        JavaType javaType = mapper.getTypeFactory().constructParametricType(collectionClass, valueType);

        Collection<T> objectList;
        try {
            objectList = mapper.readValue(json, javaType);
        } catch (Exception e) {
            logger.error("parseList error:" + e.getMessage(), e);
            objectList = null;
        }

        return objectList;
    }

    /**
     * 将json转成指定的类对象
     */
    @SuppressWarnings("unchecked")
    public static <T> T parse(String json, Class<T> type) {

        if (StringUtils.isEmpty(json) || type == null) return null;
        if (type == String.class) return (T) json;

        T result;
        try {
            result = mapper.readValue(json, type);
        } catch (Exception e) {
            logger.error("parse error:" + e.getMessage(), e);
            result = null;
        }

        return result;
    }


}
