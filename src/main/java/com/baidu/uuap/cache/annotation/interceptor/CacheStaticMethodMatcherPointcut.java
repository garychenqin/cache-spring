package com.baidu.uuap.cache.annotation.interceptor;

import com.baidu.uuap.cache.annotation.AdviceType;
import com.baidu.uuap.cache.annotation.interfaces.ICacheAttributeSource;
import org.springframework.aop.support.StaticMethodMatcherPointcut;

import java.lang.reflect.Method;

/**
 * mem方法的切入点
 *
 * @author <a href="mailto:3562720@qq.com">xiaozhi</a>
 * @version $Revision$
 * @Date 下午11:08:21
 * @since 2013-5-12
 */
public class CacheStaticMethodMatcherPointcut extends StaticMethodMatcherPointcut {


    private ICacheAttributeSource cacheAttributeSource;

    @Override
    public boolean matches(Method method, Class<?> targetClass) {
        return AdviceType.NONE != this.cacheAttributeSource.getAdviceType(method, targetClass);
    }

    public void setCacheAttributeSource(ICacheAttributeSource cacheAttributeSource) {
        this.cacheAttributeSource = cacheAttributeSource;
    }

}
