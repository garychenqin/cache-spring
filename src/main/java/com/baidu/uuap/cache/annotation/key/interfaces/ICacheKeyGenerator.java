package com.baidu.uuap.cache.annotation.key.interfaces;

import com.baidu.uuap.cache.annotation.interfaces.IMethodAttribute;
import org.aopalliance.intercept.MethodInvocation;

/**
 * 缓存key的生成器
 *
 * @author <a href="mailto:3562720@qq.com">xiaozhi</a>
 * @version $Revision$
 * @Date 下午12:01:39
 * @since 2013-5-8
 */
public interface ICacheKeyGenerator<T> {

    /**
     * 根据方法拦截返回缓存key的字符串
     *
     * @param methodInvocation
     * @return
     */
    T generateKey(MethodInvocation methodInvocation, IMethodAttribute attribute);

    /**
     * 根据多个值 生成key
     *
     * @param data
     * @return
     */
    T generateKey(Object... data);
}
