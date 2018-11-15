package com.baidu.uuap.cache.annotation.key.impl.hash;

import com.baidu.uuap.cache.annotation.interfaces.IMethodAttribute;
import com.baidu.uuap.cache.annotation.key.interfaces.ICacheKeyGenerator;
import com.google.common.base.Strings;
import org.aopalliance.intercept.MethodInvocation;

import java.util.IdentityHashMap;
import java.util.Map;

/**
 *  缓存key的生成器的抽象类
 *
 * @author <a href="mailto:3562720@qq.com">xiaozhi</a>
 * @version $Revision$
 * @Date 下午12:45:33
 * @since 2013-5-12
 */
public abstract class AbstractCacheKeyGenerator<T> implements ICacheKeyGenerator<T> {

    private static final ThreadLocal<Map<Object, Object>> REGISTRY = new ThreadLocal<Map<Object, Object>>() {

        /**
         * 主要是用来记录是否是同一个引用，IdentityHashMap的key与其他的map 的key不一样，
         * IdentityHashMap是比较key引用，hashmap是比较key的值。
         *
         * @return
         */
        @Override
        protected Map<Object, Object> initialValue() {
            return new IdentityHashMap<Object, Object>();
        }
    };

    /***是否做周期检查*/
    private boolean checkforCycles = true;
    /***是否也使用方法作为key*/
    private boolean includeMethod = true;
    /***是否使用参数类型做为key*/
    private boolean includeParameterTypes = true;


    public AbstractCacheKeyGenerator() {
        this(true, true);
    }

    public AbstractCacheKeyGenerator(boolean includeMethod, boolean includeParameterTypes) {
        this.includeMethod = includeMethod;
        this.includeParameterTypes = includeParameterTypes;
    }


    @Override
    public T generateKey(MethodInvocation methodInvocation, IMethodAttribute attribute) {
        String className = attribute.getClassName();
        if (Strings.isNullOrEmpty(className)) {
            className = methodInvocation.getMethod().getDeclaringClass().getName();
        }
        String methodName = attribute.getMethodName();
        if (Strings.isNullOrEmpty(methodName)) {
            methodName = methodInvocation.getMethod().getName();
        }
        Object[] arguments = methodInvocation.getArguments();
        Class<?>[] parameterTypes = methodInvocation.getMethod().getParameterTypes();

        return generateKey(className, methodName, parameterTypes, arguments);
    }

    @Override
    public abstract T generateKey(Object... data);

    /**
     * @return
     */
    public final boolean isIncludeMethod() {
        return includeMethod;
    }

    /**
     * @param includeMethod
     */
    public final void setIncludeMethod(boolean includeMethod) {
        this.includeMethod = includeMethod;
    }

    /**
     * @return
     */
    public final boolean isIncludeParameterTypes() {
        return includeParameterTypes;
    }

    /**
     * @param includeParameterTypes
     */
    public final void setIncludeParameterTypes(boolean includeParameterTypes) {
        this.includeParameterTypes = includeParameterTypes;
    }

    /**
     * @return
     */
    public final boolean isCheckforCycles() {
        return checkforCycles;
    }

    /**
     * @param checkforCycles
     */
    public final void setCheckforCycles(boolean checkforCycles) {
        this.checkforCycles = checkforCycles;
    }


    /**
     * 是否注册成功。
     *
     * @param element
     * @return
     */
    protected final boolean register(Object element) {
        if (!this.checkforCycles) {
            return true;
        }

        Map<Object, Object> registry = REGISTRY.get();
        // 如果是第一次put 那么key不存在引用 就返回null，如果存在 就更新一下新的，返回旧的对象
        return registry.put(element, element) == null;
    }

    /**
     * 使用了周期检查的 就删除缓存里的数据
     *
     * @param element
     */
    protected final void unregister(Object element) {
        if (!this.checkforCycles) {
            return;
        }

        final Map<Object, Object> registry = REGISTRY.get();
        registry.remove(element);
    }
}
