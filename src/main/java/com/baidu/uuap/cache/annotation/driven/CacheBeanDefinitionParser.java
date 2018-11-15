package com.baidu.uuap.cache.annotation.driven;

import com.baidu.uuap.cache.annotation.impl.CacheAttributeSourceImpl;
import com.baidu.uuap.cache.annotation.interceptor.CacheInterceptor;
import com.baidu.uuap.cache.annotation.interceptor.CacheStaticMethodMatcherPointcut;
import com.baidu.uuap.cache.annotation.key.impl.SimpleCacheKeyGenerator;
import com.baidu.uuap.cache.core.impl.BdrpClientImpl;
import com.baidu.uuap.cache.core.impl.GuavaJvmClientImpl;
import com.google.common.base.Strings;
import com.google.common.base.Throwables;
import org.apache.commons.lang.StringUtils;
import org.springframework.aop.config.AopNamespaceUtils;
import org.springframework.aop.support.DefaultBeanFactoryPointcutAdvisor;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.beans.factory.xml.XmlReaderContext;
import org.w3c.dom.Element;

/**
 * spring bean定义的解析器
 * Created by chenshouqin on 2017/9/25
 */
public class CacheBeanDefinitionParser implements BeanDefinitionParser {

    private static final String REDIS_CACHING_ADVISOR_BEAN_NAME =
            CacheBeanDefinitionParser.class.getPackage().getName() + ".internalRedisCachingAdvisor";

    @Override
    public BeanDefinition parse(Element element, ParserContext parserContext) {
        AopNamespaceUtils.registerAutoProxyCreatorIfNecessary(parserContext, element);
        if (!parserContext.getRegistry()
                .containsBeanDefinition(REDIS_CACHING_ADVISOR_BEAN_NAME)) {

            Object elementSource = parserContext.extractSource(element);

            // 设置jvm本地缓存
            RuntimeBeanReference jvmCacheRuntimeBeanReference =
                    setJvmCacheClient(element, parserContext);

            // 设置redis缓存客户端
            RuntimeBeanReference cacheClientRuntimeBeanReference =
                    setupCacheClient(element, parserContext);

            // 设置缓存属性
            RuntimeBeanReference cacheAttributeSourceReference =
                    setupCacheAttributeSource(element, parserContext, elementSource);

            // 设置切入点
            RuntimeBeanReference pointcutReference =
                    setupPointcut(element, parserContext, elementSource, cacheAttributeSourceReference);

            // 设置拦截器
            RuntimeBeanReference interceptorReference =
                    setupInterceptor(element, parserContext, elementSource,
                            cacheAttributeSourceReference,
                            cacheClientRuntimeBeanReference, jvmCacheRuntimeBeanReference);

            setupPointcutAdvisor(element, parserContext, elementSource, pointcutReference, interceptorReference);
        }
        return null;
    }


    /**
     * 设置jvm本地缓存
     *
     * @param element
     * @param parserContext
     * @return
     */
    private RuntimeBeanReference setJvmCacheClient(Element element,
                                                   ParserContext parserContext) {

        String jvmCacheOpenFlag = element.getAttribute("jvm-cache-open");
        boolean jvmCacheOpen = false;
        if (!Strings.isNullOrEmpty(jvmCacheOpenFlag)) {
            jvmCacheOpen = Boolean.valueOf(jvmCacheOpenFlag);
        }
        if (!jvmCacheOpen) {
            return null;
        }
        RootBeanDefinition guavaCacheClient = new RootBeanDefinition(GuavaJvmClientImpl.class);
        MutablePropertyValues propertyValues = guavaCacheClient.getPropertyValues();

        // set local cache seconds
        String jvmCacheSeconds = element.getAttribute("jvm-cache-seconds");
        if (!Strings.isNullOrEmpty(jvmCacheSeconds) && StringUtils.isNumeric(jvmCacheSeconds)) {
            propertyValues.add("jvmCacheSeconds", Integer.parseInt(jvmCacheSeconds));
        }

        // set local max cache keys count
        String maxJvmCacheKeysCount = element.getAttribute("max-jvm-cache-keys");
        if (!Strings.isNullOrEmpty(maxJvmCacheKeysCount) && StringUtils.isNumeric(maxJvmCacheKeysCount)) {
            propertyValues.add("maxJvmCacheKeysCount", Integer.parseInt(maxJvmCacheKeysCount));
        }

        XmlReaderContext readerContext = parserContext.getReaderContext();
        String cacheClientBeanName = readerContext.registerWithGeneratedName(guavaCacheClient);
        return new RuntimeBeanReference(cacheClientBeanName);

    }

    /**
     * 注册Redis的BDRP的客户端
     *
     * @param element
     * @param parserContext
     * @return
     */
    protected RuntimeBeanReference setupCacheClient(Element element,
                                                    ParserContext parserContext) {

        Class<?> cacheClientImplClazz = BdrpClientImpl.class;
        String cacheClientImplClazzName = element.getAttribute("client-class");
        if (!Strings.isNullOrEmpty(cacheClientImplClazzName)) {
            try {
                cacheClientImplClazz = Class.forName(cacheClientImplClazzName);
            } catch (ClassNotFoundException e) {
                throw Throwables.propagate(e);
            }
        }
        RootBeanDefinition cacheClient = new RootBeanDefinition(cacheClientImplClazz);
        MutablePropertyValues propertyValues = cacheClient.getPropertyValues();
        // 设置cache客户端
        String cacheClientRef = element.getAttribute("ref");
        if (StringUtils.isEmpty(cacheClientRef)) {
            throw new IllegalArgumentException("'ref' tag cannot null or empty");
        }
        propertyValues.add("cacheClient", new RuntimeBeanReference(cacheClientRef));

        XmlReaderContext readerContext = parserContext.getReaderContext();
        String cacheClientBeanName = readerContext.registerWithGeneratedName(cacheClient);

        return new RuntimeBeanReference(cacheClientBeanName);
    }

    /**
     * 设置CacheAttributeSource 这个bean
     *
     * @param element
     * @param parserContext
     * @param elementSource
     * @return
     */
    protected RuntimeBeanReference setupCacheAttributeSource(Element element,
                                                             ParserContext parserContext,
                                                             Object elementSource) {
        RuntimeBeanReference defaultCacheKeyGenerator =
                setupDefaultCacheKeyGenerators(parserContext, elementSource);

        RootBeanDefinition cacheAttributeSource = new RootBeanDefinition(CacheAttributeSourceImpl.class);
        cacheAttributeSource.setSource(elementSource);
        cacheAttributeSource.setRole(BeanDefinition.ROLE_INFRASTRUCTURE);

        // 设置默认的缓存Key生成器
        MutablePropertyValues propertyValues = cacheAttributeSource.getPropertyValues();
        propertyValues.addPropertyValue("defaultCacheKeyGenerator", defaultCacheKeyGenerator);

        // 如果设置了自定义的Key生成器，则设置默认的生成器
        String cacheKeyGeneratorRef = element.getAttribute("key-gen-ref");
        if (StringUtils.isNotEmpty(cacheKeyGeneratorRef)) {
            propertyValues.addPropertyValue("customCacheKeyGenerator", new RuntimeBeanReference(cacheKeyGeneratorRef));
        }

        XmlReaderContext readerContext = parserContext.getReaderContext();

        String cacheAttributeSourceBeanName = readerContext.registerWithGeneratedName(cacheAttributeSource);

        return new RuntimeBeanReference(cacheAttributeSourceBeanName);
    }


    /**
     * 设置默认的缓存key生成器 并且返回对象注入后的引用
     *
     * @param parserContext
     * @param elementSource
     * @return
     */
    protected RuntimeBeanReference setupDefaultCacheKeyGenerators(ParserContext parserContext,
                                                                  Object elementSource) {

        Class<?> defaultGeneratorClass = SimpleCacheKeyGenerator.class;
        String generatorName = defaultGeneratorClass.getName() + ".DEFAULT_KEY_GENERATOR_NAME";

        // 注入的主类
        RootBeanDefinition defaultKeyGenerator = new RootBeanDefinition(defaultGeneratorClass);
        defaultKeyGenerator.setSource(elementSource);
        // 这bean只能在spring内部使用 外部不可见
        defaultKeyGenerator.setRole(BeanDefinition.ROLE_INFRASTRUCTURE);
        BeanDefinitionRegistry registry = parserContext.getRegistry();
        // 注入到spring里
        registry.registerBeanDefinition(generatorName, defaultKeyGenerator);

        return new RuntimeBeanReference(generatorName);
    }

    /**
     * 设置切入点(需要依赖缓存对象)
     *
     * @param element
     * @param parserContext
     * @param elementSource
     * @param cacheAttributeSource
     * @return
     */
    protected RuntimeBeanReference setupPointcut(Element element,
                                                 ParserContext parserContext,
                                                 Object elementSource,
                                                 RuntimeBeanReference cacheAttributeSource) {
        RootBeanDefinition pointcut = new RootBeanDefinition(CacheStaticMethodMatcherPointcut.class);
        pointcut.setSource(elementSource);
        pointcut.setRole(BeanDefinition.ROLE_INFRASTRUCTURE);

        // 设置属性
        MutablePropertyValues propertyValues = pointcut.getPropertyValues();
        propertyValues.addPropertyValue("cacheAttributeSource", cacheAttributeSource);

        XmlReaderContext readerContext = parserContext.getReaderContext();
        String pointcutBeanName = readerContext.registerWithGeneratedName(pointcut);

        return new RuntimeBeanReference(pointcutBeanName);
    }

    /**
     * 设置拦截器（依赖缓存对象）
     *
     * @param element
     * @param parserContext
     * @param elementSource
     * @param cacheableAttributeSourceRuntimeReference
     * @return
     */
    protected RuntimeBeanReference setupInterceptor(Element element,
                                                    ParserContext parserContext,
                                                    Object elementSource,
                                                    RuntimeBeanReference cacheableAttributeSourceRuntimeReference,
                                                    RuntimeBeanReference cacheClientRuntimeReference,
                                                    RuntimeBeanReference jvmCacheRuntimeBeanReference) {
        RootBeanDefinition interceptor = new RootBeanDefinition(CacheInterceptor.class);
        interceptor.setSource(elementSource);
        interceptor.setRole(BeanDefinition.ROLE_INFRASTRUCTURE);

        MutablePropertyValues propertyValues = interceptor.getPropertyValues();
        propertyValues.addPropertyValue("cacheAttributeSource", cacheableAttributeSourceRuntimeReference);
        propertyValues.addPropertyValue("cacheClient", cacheClientRuntimeReference);
        if (null != jvmCacheRuntimeBeanReference) {
            propertyValues.addPropertyValue("jvmLocalCacheClient", jvmCacheRuntimeBeanReference);
        }

        String globalCacheKeyPrefix = element.getAttribute("global-key-prefix");
        if (StringUtils.isNotEmpty(globalCacheKeyPrefix)) {
            propertyValues.add("globalCacheKeyPrefix", globalCacheKeyPrefix);
        }

        XmlReaderContext readerContext = parserContext.getReaderContext();
        String interceptorBeanName = readerContext.registerWithGeneratedName(interceptor);
        return new RuntimeBeanReference(interceptorBeanName);
    }

    /**
     * 设置通知（依赖切入点，拦截器）
     *
     * @param element
     * @param parserContext
     * @param elementSource
     * @param cacheablePointcutBeanReference
     * @param cachingInterceptorBeanReference
     * @return
     */
    protected RuntimeBeanReference setupPointcutAdvisor(Element element,
                                                        ParserContext parserContext,
                                                        Object elementSource,
                                                        RuntimeBeanReference cacheablePointcutBeanReference,
                                                        RuntimeBeanReference cachingInterceptorBeanReference) {
        RootBeanDefinition pointcutAdvisor = new RootBeanDefinition(DefaultBeanFactoryPointcutAdvisor.class);
        pointcutAdvisor.setSource(elementSource);
        pointcutAdvisor.setRole(BeanDefinition.ROLE_INFRASTRUCTURE);

        MutablePropertyValues propertyValues = pointcutAdvisor.getPropertyValues();
        propertyValues.addPropertyValue("adviceBeanName", cachingInterceptorBeanReference.getBeanName());
        propertyValues.addPropertyValue("pointcut", cacheablePointcutBeanReference);
        propertyValues.addPropertyValue("order", 100);

        BeanDefinitionRegistry registry = parserContext.getRegistry();
        registry.registerBeanDefinition(REDIS_CACHING_ADVISOR_BEAN_NAME, pointcutAdvisor);
        return new RuntimeBeanReference(REDIS_CACHING_ADVISOR_BEAN_NAME);
    }
}
