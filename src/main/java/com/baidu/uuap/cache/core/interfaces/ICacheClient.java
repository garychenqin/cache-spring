package com.baidu.uuap.cache.core.interfaces;

/**
 * mem客户端缓存接口
 *
 * @author <a href="mailto:3562720@qq.com">xiaozhi</a>
 * @version $Revision$
 * @Date 上午12:27:12
 * @since 2013-5-13
 */
public interface ICacheClient<T> {

    /**
     * 向缓存服务器增加一个数据，如果缓存服务器上没有增加成功，如果有了增加失败
     *
     * @param key   缓存key
     * @param value 缓存值
     * @return 是否执行成功
     */
    void put(String key, Object value);

    /**
     * 向缓存服务器增加一个数据，如果缓存服务器上没有增加成功，如果有了增加失败，设置时间
     *
     * @param key           缓存key
     * @param value         缓存值
     * @param expireSeconds 时间(秒)
     * @return 是否执行成功
     */
    void put(String key, Object value, int expireSeconds);

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


    void setCacheClient(T t);
}
