package com.baidu.uuap.cache.core.interfaces;

/**
 * Created by chenshouqin on 2018/3/24
 */
public interface IJvmLocalCacheClient {

    /**
     * 向缓存服务器增加一个数据，如果缓存服务器上没有增加成功，如果有了增加失败
     *
     * @param key         缓存key
     * @param value       缓存值
     * @param isSerialize 是否序列化
     * @return 是否执行成功
     */
    void put(String key, Object value, boolean isSerialize);

    /**
     * 根据key删除数据
     *
     * @param key 缓存key
     * @return 是否执行成功
     */
    void remove(String key);

    /**
     * 根据key获取数据
     *
     * @param key 缓存key
     * @return 返回数据
     */
    Object get(String key);

}
