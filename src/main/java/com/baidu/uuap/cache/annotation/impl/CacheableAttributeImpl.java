package com.baidu.uuap.cache.annotation.impl;

import java.io.Serializable;

import com.baidu.uuap.cache.annotation.AdviceType;
import com.baidu.uuap.cache.annotation.interfaces.ICacheableAttribute;
import com.baidu.uuap.cache.annotation.key.interfaces.ICacheKeyGenerator;

/**
 * ICacheableAttribute接口实现,适配方式 主要是用来存储缓存的相关属性
 *
 * @author <a href="mailto:3562720@qq.com">xiaozhi</a>
 * @version $Revision$
 * @Date 上午11:25:38
 * @since 2013-5-10
 */
public class CacheableAttributeImpl implements ICacheableAttribute {

    private boolean cacheOpen;
    private int expireSeconds;
    private ICacheKeyGenerator<? extends Serializable> cacheKeyGenerator;
    private String className;
    private String methodName;
    private String cachePrefix;
    private boolean excludeJvmCache;
    private boolean isJvmCacheSerialize;

    public CacheableAttributeImpl(boolean cacheOpen, int expireSeconds,
                                  ICacheKeyGenerator<? extends Serializable> cacheKeyGenerator,
                                  String className, String methodName,
                                  String cachePrefix, boolean excludeJvmCache,
                                  boolean isJvmCacheSerialize) {
        this.cacheOpen = cacheOpen;
        this.expireSeconds = expireSeconds;
        this.cacheKeyGenerator = cacheKeyGenerator;
        this.className = className;
        this.methodName = methodName;
        this.cachePrefix = cachePrefix;
        this.excludeJvmCache = excludeJvmCache;
        this.isJvmCacheSerialize = isJvmCacheSerialize;
    }

    @Override
    public boolean isCacheOpen() {
        return cacheOpen;
    }

    @Override
    public int getExpireSeconds() {
        return expireSeconds;
    }

    @Override
    public AdviceType getAdviceType() {
        return AdviceType.CACHE;
    }

    @Override
    public ICacheKeyGenerator<? extends Serializable> getCacheKeyGenerator() {
        return cacheKeyGenerator;
    }

    @Override
    public String getClassName() {
        return className;
    }

    @Override
    public String getMethodName() {
        return methodName;
    }

    @Override
    public String getCachePrefix() {
        return cachePrefix;
    }

    public boolean isExcludeJvmCache() {
        return excludeJvmCache;
    }

    @Override
    public boolean isJvmCacheSerialize() {
        return isJvmCacheSerialize;
    }
}
