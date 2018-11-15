package com.baidu.uuap.cache.annotation.impl;

import com.baidu.uuap.cache.annotation.AdviceType;
import com.baidu.uuap.cache.annotation.Cacheable;
import com.baidu.uuap.common.util.MultiKeyConcurrentMap;
import com.baidu.uuap.cache.annotation.TriggersRemove;
import com.baidu.uuap.cache.annotation.interfaces.ICacheAttributeSource;
import com.baidu.uuap.cache.annotation.interfaces.ICacheableAttribute;
import com.baidu.uuap.cache.annotation.interfaces.IMethodAttribute;
import com.baidu.uuap.cache.annotation.interfaces.ITriggersRemoveAttribute;
import com.baidu.uuap.cache.annotation.key.interfaces.ICacheKeyGenerator;
import org.springframework.core.BridgeMethodResolver;
import org.springframework.util.ClassUtils;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * ICacheAttributeSource接口实现，缓存的集合操作在这个类里。
 *
 * @author <a href="mailto:3562720@qq.com">xiaozhi</a>
 * @version $Revision$
 * @Date 上午11:34:27
 * @since 2013-5-10
 */
public class CacheAttributeSourceImpl implements ICacheAttributeSource {

    // 装载cache的map 按照类、方式、最后是方法缓存接口
    private final MultiKeyConcurrentMap<Class<?>, Method, IMethodAttribute> attributesCache =
            new MultiKeyConcurrentMap<Class<?>, Method, IMethodAttribute>();

    // 缓存key生成器
    private ICacheKeyGenerator<? extends Serializable> defaultCacheKeyGenerator;

    // 自定义Key生成器
    private ICacheKeyGenerator<? extends Serializable> customCacheKeyGenerator;


    @Override
    public AdviceType getAdviceType(Method method, Class<?> targetClass) {
        IMethodAttribute methodAttribute = getMethodAttribute(method, targetClass);
        if (null != methodAttribute) {
            return methodAttribute.getAdviceType();
        }
        return AdviceType.NONE;
    }

    @Override
    public ITriggersRemoveAttribute getTriggersRemoveAttribute(Method method,
                                                               Class<?> targetClass) {
        IMethodAttribute methodAttribute = getMethodAttribute(method, targetClass);

        if (null != methodAttribute
                && AdviceType.REMOVE == methodAttribute.getAdviceType()) {
            return (ITriggersRemoveAttribute) methodAttribute;
        }
        return null;
    }


    @Override
    public ICacheableAttribute getCacheableAttribute(Method method, Class<?> targetClass) {

        IMethodAttribute methodAttribute = getMethodAttribute(method, targetClass);
        if (null != methodAttribute
                && AdviceType.CACHE == methodAttribute.getAdviceType()) {
            return (ICacheableAttribute) methodAttribute;
        }
        return null;
    }

    /**
     * 根据方法和类获取对应的注解封装的对象
     *
     * @param method
     * @param targetClass
     * @return
     */
    private IMethodAttribute getMethodAttribute(Method method, Class<?> targetClass) {

        // 如果能从缓存的数据中找到相应的属性则直接返回缓存的结果
        IMethodAttribute attributes = attributesCache.get(targetClass, method);
        if (null != attributes) {
            return attributes;
        }

        // 如果没有找到相应的缓存数据则获取并将结果写到缓存中
        IMethodAttribute att = computeMethodAttribute(method, targetClass);
        if (null != att) {
            IMethodAttribute existing = attributesCache.putIfAbsent(targetClass, method, att);
            if (null != existing) {
                return existing;
            }
        }
        return att;
    }

    /**
     * 根据方法和类获取对应的注解封装的对象，主要是同过获取到覆盖类的方法 或者方法本身来得到对应的封装对象
     *
     * @param method
     * @param targetClass
     * @return
     */
    private IMethodAttribute computeMethodAttribute(Method method, Class<?> targetClass) {
        if (!Modifier.isPublic(method.getModifiers())) {
            return null;
        }
        // 获取子类的重载方法或者本身的。
        Method specificMethod = ClassUtils.getMostSpecificMethod(method, targetClass);

        specificMethod = BridgeMethodResolver.findBridgedMethod(specificMethod);
        IMethodAttribute att = findMethodAttribute(specificMethod);
        if (null != att) {
            return att;
        }
        if (specificMethod != method) {
            att = findMethodAttribute(method);
            if (null != att) {
                return att;
            }
        }
        return null;
    }

    /**
     * 通过方法来获取具体是使用了 那个注解：然后返回对应的对象
     *
     * @param method
     * @return
     */
    private IMethodAttribute findMethodAttribute(Method method) {

        Cacheable cacheAbleAnnotation = method.getAnnotation(Cacheable.class);
        // 解析注解
        if (null != cacheAbleAnnotation) {
            return parseCacheableAnnotation(cacheAbleAnnotation);
        }

        TriggersRemove triggersRemove = method.getAnnotation(TriggersRemove.class);
        if (null != triggersRemove) {
            return parseTriggersRemoveAnnotation(triggersRemove);
        }
        return null;
    }

    /**
     * 根据方法与缓存注解 进行解析 转换成缓存封装的对象
     *
     * @param annotation
     * @return
     */
    protected ICacheableAttribute parseCacheableAnnotation(Cacheable annotation) {
        return new CacheableAttributeImpl(annotation.cacheOpen(),
                annotation.expireSeconds(), getCacheKeyGenerator(),
                annotation.className(), annotation.methodName(),
                annotation.cachePrefix(), annotation.excludeJvmCache(), annotation.jvmCacheSerialize());
    }

    /**
     * 根据方法与缓存注解 进行解析 转换成缓存删除的封装对象
     *
     * @param remove
     * @return
     */
    protected ITriggersRemoveAttribute parseTriggersRemoveAnnotation(TriggersRemove remove) {
        return new TriggersRemoveAttributeImpl(remove.when(), getCacheKeyGenerator(), remove.className(),
                remove.methodName(), remove.cachePrefix());
    }


    /**
     * 获取缓存生成器
     *
     * @return 如果有自定义的key生成器则返回自定义的，如果没有自定义则返回默认的Key生成器
     */
    private ICacheKeyGenerator<? extends Serializable> getCacheKeyGenerator() {
        if (null != customCacheKeyGenerator) {
            return customCacheKeyGenerator;
        }
        return defaultCacheKeyGenerator;
    }


    public ICacheKeyGenerator<? extends Serializable> getDefaultCacheKeyGenerator() {
        return defaultCacheKeyGenerator;
    }

    public void setDefaultCacheKeyGenerator(
            ICacheKeyGenerator<? extends Serializable> defaultCacheKeyGenerator) {
        this.defaultCacheKeyGenerator = defaultCacheKeyGenerator;
    }

    public ICacheKeyGenerator<? extends Serializable> getCustomCacheKeyGenerator() {
        return customCacheKeyGenerator;
    }

    public void setCustomCacheKeyGenerator(
            ICacheKeyGenerator<? extends Serializable> customCacheKeyGenerator) {
        this.customCacheKeyGenerator = customCacheKeyGenerator;
    }
}
