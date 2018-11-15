package com.baidu.uuap.cache.annotation.key.impl;

import java.lang.reflect.Method;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

import com.baidu.uuap.cache.annotation.key.interfaces.IReflectionHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.ReflectionUtils.MethodCallback;

import com.google.common.collect.MapMaker;

/**
 * 缓存帮助类接口实现
 *
 * @author <a href="mailto:3562720@qq.com">xiaozhi</a>
 * @version $Revision$
 * @Date 上午11:47:28
 * @since 2013-5-12
 */
public class CachingReflectionHelper implements IReflectionHelper {

    private final Logger loggger = LoggerFactory.getLogger(CachingReflectionHelper.class);

    private enum ImplementsMethod {
        HASH_CODE,
        EQUALS,
        TO_STRING;
    }

    private final Map<Class<?>, Set<ImplementsMethod>> implementsCache = new MapMaker().weakKeys().makeMap();

    /**
     * 获取缓存
     *
     * @return
     */
    private Map<Class<?>, Set<ImplementsMethod>> getCache() {
        return this.implementsCache;
    }


    @Override
    public boolean implementsHashCode(Object element) {
        return this.doesImplement(element.getClass(), ImplementsMethod.HASH_CODE);
    }


    @Override
    public boolean implementsEquals(Object element) {
        return this.doesImplement(element.getClass(), ImplementsMethod.EQUALS);
    }


    @Override
    public boolean implementsToString(Object element) {
        return this.doesImplement(element.getClass(), ImplementsMethod.TO_STRING);
    }


    public void clearCache() {
        if (loggger.isInfoEnabled()) {
            loggger.info(this.getClass().getName() + " clearCache");
        }
        this.getCache().clear();
    }

    /**
     * 根据类与ImplementsMethod类型判断是否存在 toString、hashCode、equals方法。
     *
     * @param elementClass
     * @param method
     * @return
     */
    private boolean doesImplement(final Class<?> elementClass, ImplementsMethod method) {
        Map<Class<?>, Set<ImplementsMethod>> cache = this.getCache();
        Set<ImplementsMethod> methodCache = cache.get(elementClass);

        if (methodCache == null) {
            // 创建一个空的并且具有ImplementsMethod类型的set集合
            methodCache = EnumSet.noneOf(ImplementsMethod.class);
            cache.put(elementClass, methodCache);

            final Set<ImplementsMethod> implementsSet = methodCache;
            ReflectionUtils.doWithMethods(elementClass, new MethodCallback() {
                public void doWith(Method method) throws IllegalArgumentException, IllegalAccessException {
                    if (implementsSet.size() == 3 || method.getDeclaringClass() == Object.class) {
                        return;
                    }

                    if (ReflectionUtils.isEqualsMethod(method)) {
                        implementsSet.add(ImplementsMethod.EQUALS);
                    } else if (ReflectionUtils.isHashCodeMethod(method)) {
                        implementsSet.add(ImplementsMethod.HASH_CODE);
                    } else if (ReflectionUtils.isToStringMethod(method)) {
                        implementsSet.add(ImplementsMethod.TO_STRING);
                    }
                }
            });
        }
        return methodCache.contains(method);
    }
}
