package com.baidu.uuap.cache.annotation.key.impl;

import java.lang.reflect.Method;

import com.baidu.uuap.cache.annotation.key.interfaces.IReflectionHelper;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.ReflectionUtils.MethodCallback;


/**
 * 反射帮助类的简单实现
 *
 * @author <a href="mailto:3562720@qq.com">xiaozhi</a>
 * @version $Revision$
 * @Date 下午11:04:51
 * @since 2013-5-9
 */
public class SimpleReflectionHelper implements IReflectionHelper {

    private static final class MutableBoolean {
        public boolean value = false;
    }

    @Override
    public boolean implementsHashCode(Object element) {
        final MutableBoolean found = new MutableBoolean();

        ReflectionUtils.doWithMethods(element.getClass(), new MethodCallback() {
            public void doWith(Method method) throws IllegalArgumentException, IllegalAccessException {
                if (found.value || method.getDeclaringClass() == Object.class) {
                    return;
                }

                if (ReflectionUtils.isHashCodeMethod(method)) {
                    found.value = true;
                }
            }
        });

        return found.value;
    }

    @Override
    public boolean implementsEquals(Object element) {
        final MutableBoolean found = new MutableBoolean();

        ReflectionUtils.doWithMethods(element.getClass(), new MethodCallback() {
            public void doWith(Method method) throws IllegalArgumentException, IllegalAccessException {
                if (found.value || method.getDeclaringClass() == Object.class) {
                    return;
                }

                if (ReflectionUtils.isEqualsMethod(method)) {
                    found.value = true;
                }
            }
        });

        return found.value;
    }

    @Override
    public boolean implementsToString(Object element) {
        final MutableBoolean found = new MutableBoolean();

        ReflectionUtils.doWithMethods(element.getClass(), new MethodCallback() {
            public void doWith(Method method) throws IllegalArgumentException, IllegalAccessException {
                // 排除是object对象的方法。
                if (found.value || method.getDeclaringClass() == Object.class) {
                    return;
                }

                if (ReflectionUtils.isToStringMethod(method)) {
                    found.value = true;
                }
            }
        });

        return found.value;
    }

}
