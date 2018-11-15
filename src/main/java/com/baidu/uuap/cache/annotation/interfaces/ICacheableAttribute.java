package com.baidu.uuap.cache.annotation.interfaces;

/**
 * 缓存接口：主要为缓存注解提供一些属性使用。
 *
 * @author <a href="mailto:3562720@qq.com">xiaozhi</a>
 * @version $Revision$
 * @Date 上午11:22:39
 * @since 2013-5-10
 */
public interface ICacheableAttribute extends IMethodAttribute {

    /**
     * 是否缓存
     *
     * @return
     */
    boolean isCacheOpen();

    /**
     * 失效时间
     *
     * @return
     */
    int getExpireSeconds();

    /**
     * 当开启jvm缓存的时候是否对值进行序列化
     *
     * @return
     */
    boolean isJvmCacheSerialize();

    /**
     * 当开启jvm缓存的时候某个方法豁免jvm缓存
     *
     * @return
     */
    boolean isExcludeJvmCache();
}
