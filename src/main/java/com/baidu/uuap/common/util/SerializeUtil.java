package com.baidu.uuap.common.util;

import com.baidu.uuap.common.exception.SerializationException;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.serializer.support.DeserializingConverter;
import org.springframework.core.serializer.support.SerializingConverter;

/**
 * 序列化与反序列化工具
 *
 * @author chenshouqin 2017-09-29
 */
public class SerializeUtil {

    private static Converter<Object, byte[]> serializer = new SerializingConverter();
    private static Converter<byte[], Object> deserializer = new DeserializingConverter();

    /**
     * 反序列化
     *
     * @param bytes
     * @param <T>
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <T> T deserialize(byte[] bytes) {
        if (null == bytes || bytes.length == 0) {
            return null;
        }
        try {
            return (T) deserializer.convert(bytes);
        } catch (Exception ex) {
            throw new SerializationException("Cannot deserialize", ex);
        }
    }

    /**
     * 序列化
     *
     * @param object
     * @return
     */
    public static byte[] serialize(Object object) {
        if (null == object) {
            return new byte[0];
        }
        try {
            return serializer.convert(object);
        } catch (Exception ex) {
            throw new SerializationException("Cannot serialize", ex);
        }
    }

}
