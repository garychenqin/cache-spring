package com.baidu.uuap.cache.annotation.key.impl.hash;

/**
 * hash缓存key生成器的抽象类
 *
 * @author <a href="mailto:3562720@qq.com">xiaozhi</a>
 * @version $Revision$
 * @Date 下午6:38:51
 * @since 2013-5-12
 */
public abstract class AbstractHashingCacheKeyGenerator<G, T> extends AbstractDeepCacheKeyGenerator<G, T> {

    public AbstractHashingCacheKeyGenerator() {
    }

    public AbstractHashingCacheKeyGenerator(boolean includeMethod, boolean includeParameterTypes) {
        super(includeMethod, includeParameterTypes);
    }

    /**
     * 根据class计算hash
     *
     * @param generator
     * @param e
     */
    protected void appendClass(G generator, Class<?> e) {
        this.append(generator, ((Class<?>) e).getName());
    }

    /**
     * 根据枚举计算hash
     *
     * @param generator
     * @param e
     */
    protected void appendEnum(G generator, Enum<?> e) {
        if (e.getClass().isAnonymousClass()) {
            this.deepHashCode(generator, new Object[]{e.getClass().getEnclosingClass(), e.name()});
        } else {
            this.deepHashCode(generator, new Object[]{e.getClass(), e.name()});
        }
    }

    @Override
    protected final void append(G generator, Object e) {
        if (e instanceof Class<?>) {
            this.appendClass(generator, (Class<?>) e);
        } else if (e instanceof Enum<?>) {
            this.appendEnum(generator, (Enum<?>) e);
        } else {
            this.appendHash(generator, e);
        }
    }

    @Override
    protected boolean shouldReflect(Object element) {
        return !this.getReflectionHelper().implementsHashCode(element);
    }

    /**
     * 根据对象进行hash计算
     *
     * @param generator
     * @param e
     */
    protected abstract void appendHash(G generator, Object e);
}
