package com.baidu.uuap.cache.annotation.interfaces;

import java.lang.reflect.Method;

import com.baidu.uuap.cache.annotation.AdviceType;

/**
 * 缓存的解析接口
 *
 * @author <a href="mailto:3562720@qq.com">xiaozhi</a>
 * @version $Revision$
 * @Date 上午11:44:56
 * @since 2013-5-10
 */
public interface ICacheAttributeSource {

    /**
     * 根据方法和类获取通知类型。
     *
     * @param method
     * @param targetClass
     * @return
     */
    AdviceType getAdviceType(Method method, Class<?> targetClass);

    /**
     * 根据方法和类获取缓存删除的接口
     *
     * @param method
     * @param targetClass
     * @return
     */
    ITriggersRemoveAttribute getTriggersRemoveAttribute(Method method, Class<?> targetClass);

    /**
     * 根据方法和类获取缓存的接口
     *
     * @param method
     * @param targetClass
     * @return
     */
    ICacheableAttribute getCacheableAttribute(Method method, Class<?> targetClass);
}
