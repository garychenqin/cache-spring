/*
 * 文件名： AbstractDeepCacheKeyGenerator.java
 * 
 * 创建日期： 2013-5-12
 *
 * Copyright(C) 2013, by xiaozhi.
 *
 * 原始作者: <a href="mailto:3562720@qq.com">xiaozhi</a>
 *
 */
package com.baidu.uuap.cache.annotation.key.impl.hash;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.baidu.uuap.cache.annotation.key.impl.CachingReflectionHelper;
import com.baidu.uuap.cache.annotation.key.interfaces.IReflectionHelper;
import org.springframework.util.ReflectionUtils;

/**
 * 缓存的深入生成器
 *
 * @author <a href="mailto:3562720@qq.com">xiaozhi</a>
 * @version $Revision$
 * @Date 下午2:54:05
 * @since 2013-5-12
 */
public abstract class AbstractDeepCacheKeyGenerator<G, T> extends AbstractCacheKeyGenerator<T> {

    /**
     * 使用反射帮助类
     */
    private IReflectionHelper reflectionHelper = new CachingReflectionHelper();

    /**
     * 是否使用反射帮助
     */
    private boolean useReflection = false;

    public AbstractDeepCacheKeyGenerator() {
        super();
    }


    public AbstractDeepCacheKeyGenerator(boolean includeMethod, boolean includeParameterTypes) {
        super(includeMethod, includeParameterTypes);
    }


    @Override
    public T generateKey(Object... data) {
        G generator = this.getGenerator(data);
        this.deepHashCode(generator, data);
        return this.generateKey(generator);
    }

    /**
     * 根据G的生成器 获取T的对象
     *
     * @param generator
     * @return
     */
    protected abstract T generateKey(G generator);

    /**
     * 根据object[]计算hash
     *
     * @param generator
     * @param a
     */
    protected void deepHashCode(G generator, Object[] a) {
        this.beginRecursion(generator, a);
        for (final Object element : a) {
            this.deepHashCode(generator, element);
        }
        this.endRecursion(generator, a);
    }

    /**
     * 根据Iterable接口计算hash
     *
     * @param generator
     * @param a
     */
    protected void deepHashCode(G generator, Iterable<?> a) {
        this.beginRecursion(generator, a);
        for (final Object element : a) {
            this.deepHashCode(generator, element);
        }
        this.endRecursion(generator, a);
    }

    /**
     * 根据Map.Entry计算hash
     *
     * @param generator
     * @param e
     */
    protected void deepHashCode(G generator, Map.Entry<?, ?> e) {
        this.beginRecursion(generator, e);
        this.deepHashCode(generator, e.getKey());
        this.deepHashCode(generator, e.getValue());
        this.endRecursion(generator, e);
    }

    /**
     * 根据单个object计算hash
     *
     * @param generator
     * @param element
     */
    protected final void deepHashCode(G generator, Object element) {
        if (null == element) {
            this.appendNull(generator);
            return;
        }
        if (!register(element)) {
            this.appendGraphCycle(generator, element);
            return;
        }

        try {
            if (element instanceof byte[]) {
                this.append(generator, (byte[]) element);
            } else if (element instanceof short[]) {
                this.append(generator, (short[]) element);
            } else if (element instanceof int[]) {
                this.append(generator, (int[]) element);
            } else if (element instanceof long[]) {
                this.append(generator, (long[]) element);
            } else if (element instanceof char[]) {
                this.append(generator, (char[]) element);
            } else if (element instanceof float[]) {
                this.append(generator, (float[]) element);
            } else if (element instanceof double[]) {
                this.append(generator, (double[]) element);
            } else if (element instanceof boolean[]) {
                this.append(generator, (boolean[]) element);
            } else if (element instanceof Object[]) {
                this.deepHashCode(generator, (Object[]) element);
            } else if (element instanceof Iterable<?>) {
                this.deepHashCode(generator, (Iterable<?>) element);
            } else if (element instanceof Map<?, ?>) {
                this.deepHashCode(generator, ((Map<?, ?>) element).entrySet());
            } else if (element instanceof Map.Entry<?, ?>) {
                this.deepHashCode(generator, (Map.Entry<?, ?>) element);
            } else if (this.useReflection) {
                this.reflectionDeepHashCode(generator, element);
            } else {
                this.append(generator, element);
            }
        } finally {
            unregister(element);
        }
    }

    /**
     * 使用反射计算hash
     *
     * @param generator
     * @param element
     */
    protected final void reflectionDeepHashCode(G generator, final Object element) {
        // 如果当前对象是属于这个类型的直接计算。
        if (element instanceof Class<?>) {
            this.append(generator, element);
            return;
        }
        // 如果当前对象有hashcode与equals 直接计算
        if (!this.shouldReflect(element)) {
            this.append(generator, element);
            return;
        }
        final List<Object> reflectiveObject = new LinkedList<Object>();
        reflectiveObject.add(element.getClass());
        try {
            // 循环当亲类的上级，然后判断不是静态和Transient的字段就进行计算hash
            for (Class<?> targetClass = element.getClass(); targetClass != null;
                 targetClass = targetClass.getSuperclass()) {
                final Field[] fields = targetClass.getDeclaredFields();
                AccessibleObject.setAccessible(fields, true);
                for (int i = 0; i < fields.length; i++) {
                    final Field field = fields[i];
                    final int modifiers = field.getModifiers();
                    if (!Modifier.isStatic(modifiers) && !Modifier.isTransient(modifiers)) {
                        final Object fieldValue = field.get(element);
                        reflectiveObject.add(fieldValue);
                    }
                }
            }
        } catch (IllegalAccessException exception) {
            ReflectionUtils.handleReflectionException(exception);
        }

        this.deepHashCode(generator, reflectiveObject);
    }

    /**
     * 是否应该使用反射
     *
     * @param element
     * @return
     */
    protected boolean shouldReflect(Object element) {
        return !this.reflectionHelper.implementsHashCode(element) || !this.reflectionHelper.implementsEquals(element);
    }

    /**
     * 可以自行扩展 在所有的计算hash之前。
     *
     * @param generator
     * @param e
     */
    protected void beginRecursion(G generator, Object e) {

    }

    /**
     * 可以自行扩展 在所有的计算hash之后
     *
     * @param generator
     * @param e
     */
    protected void endRecursion(G generator, Object e) {
    }


    /**
     * 根据数据获取 生成器对象
     *
     * @param data
     * @return
     */
    protected abstract G getGenerator(Object... data);

    /**
     * 向G的生成器增加hash
     *
     * @param generator
     */
    protected abstract void appendNull(G generator);

    /**
     * 根据对象与G 向G的生成器里计算hash
     *
     * @param generator
     * @param o
     */
    protected abstract void appendGraphCycle(G generator, Object o);

    /**
     * 根据对象与G 向G的生成器里计算hash
     *
     * @param generator
     * @param e
     */
    protected abstract void append(G generator, Object e);

    /**
     * boolean生成hash 如果实现类能处理那么最好重写这个方法，避免自动装箱问题
     *
     * @param generator
     * @param a
     */
    protected void append(G generator, boolean[] a) {
        this.beginRecursion(generator, a);
        for (final boolean element : a) {
            this.append(generator, element);
        }
        this.endRecursion(generator, a);
    }

    /**
     * byte生成hash 如果实现类能处理那么最好重写这个方法，避免自动装箱问题
     *
     * @param generator
     * @param a
     */
    protected void append(G generator, byte[] a) {
        this.beginRecursion(generator, a);
        for (final byte element : a) {
            this.append(generator, element);
        }
        this.endRecursion(generator, a);
    }

    /**
     * char生成hash 如果实现类能处理那么最好重写这个方法，避免自动装箱问题
     *
     * @param generator
     * @param a
     */
    protected void append(G generator, char[] a) {
        this.beginRecursion(generator, a);
        for (final char element : a) {
            this.append(generator, element);
        }
        this.endRecursion(generator, a);
    }

    /**
     * double生成hash 如果实现类能处理那么最好重写这个方法，避免自动装箱问题
     *
     * @param generator
     * @param a
     */
    protected void append(G generator, double[] a) {
        this.beginRecursion(generator, a);
        for (final double element : a) {
            this.append(generator, element);
        }
        this.endRecursion(generator, a);
    }

    /**
     * float生成hash 如果实现类能处理那么最好重写这个方法，避免自动装箱问题
     *
     * @param generator
     * @param a
     */
    protected void append(G generator, float[] a) {
        this.beginRecursion(generator, a);
        for (final float element : a) {
            this.append(generator, element);
        }
        this.endRecursion(generator, a);
    }

    /**
     * int生成hash 如果实现类能处理那么最好重写这个方法，避免自动装箱问题
     *
     * @param generator
     * @param a
     */
    protected void append(G generator, int[] a) {
        this.beginRecursion(generator, a);
        for (final int element : a) {
            this.append(generator, element);
        }
        this.endRecursion(generator, a);
    }

    /**
     * long生成hash，如果实现类能处理那么最好重写这个方法，避免自动装箱问题
     *
     * @param generator
     * @param a
     */
    protected void append(G generator, long[] a) {
        this.beginRecursion(generator, a);
        for (final long element : a) {
            this.append(generator, element);
        }
        this.endRecursion(generator, a);
    }

    /**
     * 根据short生成hash，如果实现类能处理这个那么应该重写这个方法，避免自动装箱问题
     *
     * @param generator
     * @param a
     */
    protected void append(G generator, short[] a) {
        this.beginRecursion(generator, a);
        for (final short element : a) {
            this.append(generator, element);
        }
        this.endRecursion(generator, a);
    }


    /**
     * @return the reflectionHelper
     */
    public IReflectionHelper getReflectionHelper() {
        return reflectionHelper;
    }

    /**
     * @param reflectionHelper the reflectionHelper to set
     */
    public void setReflectionHelper(IReflectionHelper reflectionHelper) {
        this.reflectionHelper = reflectionHelper;
    }

    /**
     * @return the useReflection
     */
    public boolean isUseReflection() {
        return useReflection;
    }

    /**
     * @param useReflection the useReflection to set
     */
    public void setUseReflection(boolean useReflection) {
        this.useReflection = useReflection;
    }


    @Override
    public String toString() {
        return this.getClass().getSimpleName() + " [" + "includeMethod="
                + isIncludeMethod() + ", " + "includeParameterTypes="
                + isIncludeParameterTypes() + ", " + "useReflection="
                + isUseReflection() + ", " + "checkforCycles="
                + isCheckforCycles() + "]";
    }
}
