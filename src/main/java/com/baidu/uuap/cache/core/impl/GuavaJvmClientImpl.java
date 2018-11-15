package com.baidu.uuap.cache.core.impl;

import com.baidu.uuap.cache.core.interfaces.IJvmLocalCacheClient;
import com.baidu.uuap.common.util.SerializeUtil;
import com.google.common.base.Strings;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import java.util.concurrent.TimeUnit;

/**
 * Created by chenshouqin on 2018/3/22
 */
public class GuavaJvmClientImpl implements IJvmLocalCacheClient, InitializingBean {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private Cache<String, Object> guavaCache;
    private Integer jvmCacheSeconds = 300;
    private Integer maxJvmCacheKeysCount = 3000;

    @Override
    public void put(String key, Object value, boolean isSerialize) {
        if (Strings.isNullOrEmpty(key) || null == value) {
            return;
        }
        if (value instanceof byte[]) {
            throw new UnsupportedOperationException("localJvm cache cannot support 'byte[]' currently");
        }
        if (isSerialize) {
            guavaCache.put(key, SerializeUtil.serialize(value));
        } else {
            guavaCache.put(key, value);
        }
    }

    @Override
    public void remove(String key) {
        if (Strings.isNullOrEmpty(key)) {
            return;
        }
        guavaCache.invalidate(key);
    }

    @Override
    public Object get(String key) {
        if (Strings.isNullOrEmpty(key)) {
            return null;
        }
        Object value = guavaCache.getIfPresent(key);
        if (null == value) {
            return null;
        }
        if (value instanceof byte[]) {
            return SerializeUtil.deserialize((byte[]) value);
        }
        return value;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        logger.info("localJvm Cache flag open, building Guava local cache, maxJvmCacheKeysCount: {}, cacheSeconds: {}",
                maxJvmCacheKeysCount, jvmCacheSeconds);
        guavaCache = CacheBuilder.newBuilder()
                .expireAfterWrite(jvmCacheSeconds, TimeUnit.SECONDS)
                .maximumSize(maxJvmCacheKeysCount)
                .build();
    }

    public void setJvmCacheSeconds(Integer jvmCacheSeconds) {
        this.jvmCacheSeconds = jvmCacheSeconds;
    }

    public void setMaxJvmCacheKeysCount(Integer maxJvmCacheKeysCount) {
        this.maxJvmCacheKeysCount = maxJvmCacheKeysCount;
    }
}
