package com.baidu.uuap.common.util;

import com.dyuproject.protostuff.LinkedBuffer;
import com.dyuproject.protostuff.ProtostuffIOUtil;
import com.dyuproject.protostuff.Schema;
import com.dyuproject.protostuff.runtime.RuntimeSchema;
import com.google.common.base.Throwables;
import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ConcurrentMap;

/**
 * ProtoStuffSerializer系列化工具类
 * Created by chenshouqin on 2018/3/24
 */
public class ProtoStuffSerializerUtil {

    private static Logger logger = LoggerFactory.getLogger(ProtoStuffSerializerUtil.class);
    private static ConcurrentMap<Class<?>, Schema<?>> cachedSchema = Maps.newConcurrentMap();


    /**
     * 对象序列化
     *
     * @param obj
     * @param <T>
     * @return
     */
    public static <T> byte[] serialize(T obj) {
        Assert.notNull(obj, "obj need serialize cannot null");
        @SuppressWarnings("unchecked")
        Schema<T> schema = (Schema<T>) getSchema(obj.getClass());
        LinkedBuffer buffer = LinkedBuffer.allocate(1024 * 1024);
        byte[] protoStuffByteArray;
        try {
            protoStuffByteArray = ProtostuffIOUtil.toByteArray(obj, schema, buffer);
        } catch (Exception e) {
            logger.error("serialize object failed, object className: {}", obj.getClass().getName(), e);
            throw Throwables.propagate(e);
        } finally {
            buffer.clear();
        }
        return protoStuffByteArray;
    }

    /**
     * 对象反序列化
     *
     * @param paramArrayOfByte
     * @param targetClass
     * @param <T>
     * @return
     */
    public static <T> T deserialize(byte[] paramArrayOfByte, Class<T> targetClass) {
        if (null == paramArrayOfByte || paramArrayOfByte.length == 0) {
            throw new IllegalArgumentException("dataAsByte need deserialize cannot null or empty");
        }
        T instance;
        try {
            instance = targetClass.newInstance();
        } catch (Exception e) {
            logger.error("deserialize byte[] failed, targetClassName: {}", targetClass.getName(), e);
            throw Throwables.propagate(e);
        }
        Schema<T> schema = getSchema(targetClass);
        ProtostuffIOUtil.mergeFrom(paramArrayOfByte, instance, schema);
        return instance;
    }

    /**
     * 序列化List列表
     *
     * @param objList
     * @param <T>
     * @return
     */
    public static <T> byte[] serializeList(List<T> objList) {
        Assert.notNull(objList, "objList need serialize cannot null");
        @SuppressWarnings("unchecked")
        Schema<T> schema = (Schema<T>) getSchema(objList.get(0).getClass());
        LinkedBuffer buffer = LinkedBuffer.allocate(1024 * 1024);
        byte[] protoStuffByteArray;
        ByteArrayOutputStream bos = null;
        try {
            bos = new ByteArrayOutputStream();
            ProtostuffIOUtil.writeListTo(bos, objList, schema, buffer);
            protoStuffByteArray = bos.toByteArray();
        } catch (Exception e) {
            logger.error("serialize objectList failed, object className: {}", objList.get(0).getClass().getName(), e);
            throw Throwables.propagate(e);
        } finally {
            buffer.clear();
            try {
                if (null != bos) {
                    bos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return protoStuffByteArray;
    }

    /**
     * 获取缓存的Schema
     *
     * @param clazz
     * @param <T>
     * @return
     */
    private static <T> Schema<T> getSchema(Class<T> clazz) {
        Schema<T> schema = (Schema<T>) cachedSchema.get(clazz);
        if (null == schema) {
            schema = RuntimeSchema.getSchema(clazz);
            if (null != schema) {
                cachedSchema.putIfAbsent(clazz, schema);
            }
        }
        return schema;
    }

}
