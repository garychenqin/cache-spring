package com.baidu.uuap.cache.annotation.key.impl;

import com.baidu.uuap.cache.annotation.interfaces.IMethodAttribute;
import com.baidu.uuap.cache.annotation.key.interfaces.ICacheKeyGenerator;
import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import org.aopalliance.intercept.MethodInvocation;

/**
 * 根据类名，方法名参数等方式生成Cache Key
 * Created by chenshouqin on 2018/4/9
 */
public class SimpleCacheKeyGenerator implements ICacheKeyGenerator<String> {

    private Joiner joiner = Joiner.on("#").skipNulls();

    @Override
    public String generateKey(MethodInvocation methodInvocation, IMethodAttribute attribute) {
        String className = attribute.getClassName();
        if (Strings.isNullOrEmpty(className)) {
            className = methodInvocation.getMethod().getDeclaringClass().getSimpleName();
        }
        String methodName = attribute.getMethodName();
        if (Strings.isNullOrEmpty(methodName)) {
            methodName = methodInvocation.getMethod().getName();
        }

        Class<?>[] parameterTypes = methodInvocation.getMethod().getParameterTypes();
        Object[] arguments = methodInvocation.getArguments();

        for (int i = 0; i < parameterTypes.length; i++) {
            if (!isPrimitive(parameterTypes[i])) {
                throw new UnsupportedOperationException(
                        String.format("parameterType must primitive, actual: %s", parameterTypes[i].getName()));
            }
        }

        return joiner.join(className, methodName, joiner.join(arguments));
    }

    @Override
    public String generateKey(Object... data) {
        if (null == data || data.length == 0) {
            return null;
        }
        for (Object object : data) {
            if (!isPrimitive(object.getClass())) {
                throw new UnsupportedOperationException(
                        String.format("parameterType must primitive, actual: %s", object.getClass().getName()));
            }
        }
        return joiner.join(data);
    }

    private boolean isPrimitive(Class<?> cls) {
        return cls.isPrimitive() || cls == Boolean.class || cls == Byte.class || cls == Character.class
                || cls == Short.class || cls == Integer.class || cls == Long.class || cls == Float.class
                || cls == Double.class || cls == String.class;
    }
}
