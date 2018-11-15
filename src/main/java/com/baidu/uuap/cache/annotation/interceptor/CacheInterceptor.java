package com.baidu.uuap.cache.annotation.interceptor;

import com.baidu.uuap.cache.annotation.AdviceType;
import com.baidu.uuap.cache.annotation.When;
import com.baidu.uuap.cache.annotation.interfaces.ICacheAttributeSource;
import com.baidu.uuap.cache.annotation.interfaces.ICacheableAttribute;
import com.baidu.uuap.cache.annotation.interfaces.IMethodAttribute;
import com.baidu.uuap.cache.annotation.interfaces.ITriggersRemoveAttribute;
import com.baidu.uuap.cache.core.interfaces.ICacheClient;
import com.baidu.uuap.cache.core.interfaces.IJvmLocalCacheClient;
import com.baidu.uuap.common.util.Config;
import com.google.common.base.Strings;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Set;

/**
 * redis缓存拦截器
 *
 * @author <a href="mailto:3562720@qq.com">xiaozhi</a>
 * @version $Revision$
 * @Date 下午11:16:19
 * @since 2013-5-12
 */
public class CacheInterceptor implements MethodInterceptor {

    private final Logger logger = LoggerFactory.getLogger(CacheInterceptor.class);

    private ICacheAttributeSource cacheAttributeSource;
    private ICacheClient cacheClient;
    private String globalCacheKeyPrefix;
    private IJvmLocalCacheClient jvmLocalCacheClient;

    @Override
    public Object invoke(MethodInvocation methodInvocation) throws Throwable {

        Method method = methodInvocation.getMethod();
        Class<?> targetClass = (null != methodInvocation.getThis() ? methodInvocation.getThis().getClass() : null);
        AdviceType adviceType = cacheAttributeSource.getAdviceType(method, targetClass);

        switch (adviceType) {
            case CACHE: {
                ICacheableAttribute cacheableAttribute =
                        cacheAttributeSource.getCacheableAttribute(method, targetClass);

                // 如果缓存对象没有那么直接执行目标方法，然后给一个提示表示没有缓存必要了。
                if (null == cacheableAttribute) {
                    return methodInvocation.proceed();
                }
                return invokeCacheable(methodInvocation, cacheableAttribute);

            }

            case REMOVE: {
                ITriggersRemoveAttribute triggersRemoveAttribute =
                        cacheAttributeSource.getTriggersRemoveAttribute(method, targetClass);

                if (null == triggersRemoveAttribute) {
                    return methodInvocation.proceed();
                }

                return invokeTriggersRemove(methodInvocation, triggersRemoveAttribute);
            }

            default: {
                logger.trace("The method {} is not advised {}.", methodInvocation, adviceType);
                return methodInvocation.proceed();
            }
        }
    }

    /**
     * 对带有Cacheable注解的方法首先查缓存，没有才会去反射调用方法，并将结果重新放回缓存
     *
     * @param methodInvocation
     * @param cacheableAttribute
     * @return
     * @throws Throwable
     */
    private Object invokeCacheable(MethodInvocation methodInvocation,
                                   ICacheableAttribute cacheableAttribute) throws Throwable {
        String cacheKey = createCacheKey(methodInvocation, cacheableAttribute);
        logger.info("Cacheable annotation triggered, className: {}, methodName: {}, generated cacheKey: {}",
                new Object[]{methodInvocation.getMethod().getDeclaringClass().getName(),
                        methodInvocation.getMethod().getName(), cacheKey});

        long cacheBegin = System.currentTimeMillis();
        // 从cache中获取缓存
        Object data = null;
        boolean jvmCacheHit = false;
        boolean redisCacheHit = false;

        if (cacheableAttribute.isCacheOpen()) {
            if (null != jvmLocalCacheClient && !cacheableAttribute.isExcludeJvmCache()) {
                data = jvmLocalCacheClient.get(cacheKey);
                if (null != data) {
                    jvmCacheHit = true;
                }
            }
            if (null == data) {
                data = cacheClient.get(cacheKey);
                if (null != data) {
                    redisCacheHit = true;
                }
            }
        }
        long cacheCost = System.currentTimeMillis() - cacheBegin;

        if (null != data) {
            logger.info("cache hit, return value from cache, cacheKey: {}, "
                            + "jvmCacheHit: {}, redisCacheHit: {}, cacheCostTimeMills: {}ms",
                    new Object[]{cacheKey, jvmCacheHit, redisCacheHit, cacheCost});
            if (null != jvmLocalCacheClient && redisCacheHit && !cacheableAttribute.isExcludeJvmCache()) {
                jvmLocalCacheClient.put(cacheKey, data, cacheableAttribute.isJvmCacheSerialize());
            }
            return data;
        } else {
            logger.info("cache not hit, invoke method, cacheKey: {}, cacheCostTimeMills: {}ms", cacheKey, cacheCost);
            long begin = System.currentTimeMillis();
            Object object = methodInvocation.proceed();

            if (object instanceof Object[] && ((Object[]) object).length > Config.MAX_CACHE_SIZE) {
                logger.warn("invoke method get Object[] and length > {}, cache ignore, "
                                + "className: {}, methodName: {}, cacheKey: {}, invokeMethodCost: {}ms",
                        new Object[]{Config.MAX_CACHE_SIZE,
                                methodInvocation.getMethod().getDeclaringClass().getName(),
                                methodInvocation.getMethod().getName(), cacheKey, System.currentTimeMillis() - begin});
                return object;
            } else if (object instanceof List && ((List) object).size() > Config.MAX_CACHE_SIZE) {
                logger.warn("invoke method get List and length > {}, cache ignore, "
                                + "className: {}, methodName: {}, cacheKey: {}, invokeMethodCost: {}ms",
                        new Object[]{Config.MAX_CACHE_SIZE,
                                methodInvocation.getMethod().getDeclaringClass().getName(),
                                methodInvocation.getMethod().getName(), cacheKey, System.currentTimeMillis() - begin});
                return object;
            } else if (object instanceof Set && ((Set) object).size() > Config.MAX_CACHE_SIZE) {
                logger.warn("invoke method get Set and length > {}, cache ignore, "
                                + "className: {}, methodName: {}, cacheKey: {}, invokeMethodCost: {}ms",
                        new Object[]{Config.MAX_CACHE_SIZE,
                                methodInvocation.getMethod().getDeclaringClass().getName(),
                                methodInvocation.getMethod().getName(), cacheKey, System.currentTimeMillis() - begin});
                return object;
            }

            if (null != object && cacheableAttribute.isCacheOpen()) {
                logger.info("invoke method finished, cacheKey: {}, invokeMethodCost: {}ms",
                        cacheKey, System.currentTimeMillis() - begin);
                if (null != jvmLocalCacheClient && !cacheableAttribute.isExcludeJvmCache()) {
                    jvmLocalCacheClient.put(cacheKey, object, cacheableAttribute.isJvmCacheSerialize());
                }
                cacheClient.put(cacheKey, object, cacheableAttribute.getExpireSeconds());
            }
            return object;
        }
    }

    /**
     * 触发移除缓存
     *
     * @param methodInvocation
     * @param triggersRemoveAttribute
     * @return
     * @throws Throwable
     */
    private Object invokeTriggersRemove(MethodInvocation methodInvocation,
                                        ITriggersRemoveAttribute triggersRemoveAttribute) throws Throwable {

        // 在方法之前执行移除命令
        if (When.BEFORE_METHOD_INVOCATION.equals(triggersRemoveAttribute.getWhen())) {
            invokeCacheRemove(methodInvocation, triggersRemoveAttribute);
            return methodInvocation.proceed();
        }
        // 在方法之后执行移除命令
        Object methodInvocationResult = methodInvocation.proceed();
        invokeCacheRemove(methodInvocation, triggersRemoveAttribute);
        return methodInvocationResult;
    }

    /**
     * 缓存删除
     *
     * @param methodInvocation
     * @param triggersRemoveAttribute
     */
    private void invokeCacheRemove(MethodInvocation methodInvocation,
                                   ITriggersRemoveAttribute triggersRemoveAttribute) {

        String cacheKey = createCacheKey(methodInvocation, triggersRemoveAttribute);
        logger.info("cacheRemove annotation triggered, className: {}, methodName: {}, when: {}, remove cacheKey: {}",
                new Object[]{methodInvocation.getMethod().getDeclaringClass().getName(),
                        methodInvocation.getMethod().getName(), triggersRemoveAttribute.getWhen().name(), cacheKey});

        if (null != jvmLocalCacheClient) {
            jvmLocalCacheClient.remove(cacheKey);
        }
        cacheClient.remove(cacheKey);
    }

    /**
     * 创建缓存的Key
     *
     * @param methodInvocation
     * @param attribute
     * @return
     */
    private String createCacheKey(MethodInvocation methodInvocation, IMethodAttribute attribute) {
        String rawKey = String.valueOf(attribute.getCacheKeyGenerator().generateKey(methodInvocation, attribute));
        // 给cacheKey封装前缀
        if (StringUtils.isNotEmpty(attribute.getCachePrefix())) {
            return wrapKey(attribute.getCachePrefix(), rawKey);
        } else if (StringUtils.isNotEmpty(globalCacheKeyPrefix)) {
            return wrapKey(globalCacheKeyPrefix, rawKey);
        } else {
            return rawKey;
        }
    }

    /**
     * 封装原始的Key，加前缀
     *
     * @param key 原始的Key
     * @return
     */
    private String wrapKey(String cachePrefix, String key) {
        if (Strings.isNullOrEmpty(cachePrefix)) {
            return key;
        }
        return new StringBuilder(cachePrefix).append("-").append(key).toString();
    }

    public void setCacheAttributeSource(ICacheAttributeSource cacheableAttributeSource) {
        this.cacheAttributeSource = cacheableAttributeSource;
    }

    public void setCacheClient(ICacheClient cacheClient) {
        this.cacheClient = cacheClient;
    }

    public void setGlobalCacheKeyPrefix(String globalCacheKeyPrefix) {
        if (StringUtils.isEmpty(globalCacheKeyPrefix)) {
            globalCacheKeyPrefix = StringUtils.EMPTY;
        }
        this.globalCacheKeyPrefix = globalCacheKeyPrefix;
    }

    public void setJvmLocalCacheClient(IJvmLocalCacheClient jvmLocalCacheClient) {
        this.jvmLocalCacheClient = jvmLocalCacheClient;
    }
}
