package com.baidu.uuap.common.util;

import java.io.Serializable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 多key存储的Map
 *
 * @author <a href="mailto:3562720@qq.com">xiaozhi</a>
 * @version $Revision$
 * @Date 上午10:35:28
 * @since 2013-5-10
 */
public final class MultiKeyConcurrentMap<K1, K2, V> implements Serializable {

    private static final long serialVersionUID = 6779771730940219973L;

    private final ConcurrentMap<K1, ConcurrentMap<K2, V>> map = new ConcurrentHashMap<K1, ConcurrentMap<K2, V>>();

    /**
     * 检查map是否存在primaryKey、subKey
     *
     * @param primaryKey
     * @param subKey
     * @return 联合Key是否存在
     */
    public boolean containsKey(K1 primaryKey, K2 subKey) {
        ConcurrentMap<K2, V> subMap = map.get(primaryKey);
        return null != subMap && subMap.containsKey(subKey);
    }

    /**
     * 装载subKey到primaryKey，然后把对应的value装载到subKey
     *
     * @param primaryKey
     * @param subKey
     * @param value
     * @return
     */
    public V put(K1 primaryKey, K2 subKey, V value) {
        ConcurrentMap<K2, V> subMap = this.getOrCreateSubMap(primaryKey);
        return subMap.put(subKey, value);
    }

    /**
     * 如果primaryKey、subKey不在了创建新的并返回，如果存在就返回现有的value
     *
     * @param primaryKey
     * @param subKey
     * @param value
     * @return
     */
    public V putIfAbsent(K1 primaryKey, K2 subKey, V value) {
        ConcurrentMap<K2, V> subMap = getOrCreateSubMap(primaryKey);
        return subMap.putIfAbsent(subKey, value);
    }

    /**
     * 根据primaryKey、subKey获取value
     *
     * @param primaryKey
     * @param subKey
     * @return
     */
    public V get(K1 primaryKey, K2 subKey) {
        final ConcurrentMap<K2, V> subMap = this.map.get(primaryKey);
        if (null == subMap) {
            return null;
        }
        return subMap.get(subKey);
    }

    /**
     * 根据primaryKey获取或者创建一个subKey的map,如果不存在则创建一个新的
     *
     * @param primaryKey
     * @return
     */
    private ConcurrentMap<K2, V> getOrCreateSubMap(K1 primaryKey) {
        ConcurrentMap<K2, V> subMap = this.map.get(primaryKey);
        if (null == subMap) {
            subMap = new ConcurrentHashMap<K2, V>();
            ConcurrentMap<K2, V> existingSubMap = map.putIfAbsent(primaryKey, subMap);
            if (null != existingSubMap) {
                subMap = existingSubMap;
            }
        }
        return subMap;
    }
}
