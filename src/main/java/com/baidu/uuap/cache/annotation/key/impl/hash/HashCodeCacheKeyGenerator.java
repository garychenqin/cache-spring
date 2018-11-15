package com.baidu.uuap.cache.annotation.key.impl.hash;


/**
 * hashcode的缓存key生成器
 *
 * @author <a href="mailto:3562720@qq.com">xiaozhi</a>
 * @version $Revision$
 * @Date 下午7:18:31
 * @since 2013-5-12
 */
public class HashCodeCacheKeyGenerator extends
        AbstractHashingCacheKeyGenerator<HashCodeCacheKeyGenerator.LongGenerator, Long> {

    protected static final long INITIAL_HASH = 1;
    protected static final long MULTIPLIER = 31;

    /**
     * long生成器。
     *
     * @author <a href="mailto:3562720@qq.com">xiaozhi</a>
     * @version $Revision$
     * @Date 下午7:22:14
     * @since 2013-5-12
     */
    public static class LongGenerator {
        private long hash = INITIAL_HASH;

        private LongGenerator() {
        }
    }

    public HashCodeCacheKeyGenerator() {
    }

    public HashCodeCacheKeyGenerator(boolean includeMethod, boolean includeParameterTypes) {
        super(includeMethod, includeParameterTypes);
    }

    @Override
    public LongGenerator getGenerator(Object... data) {
        return new LongGenerator();
    }

    @Override
    public Long generateKey(LongGenerator generator) {
        return generator.hash;
    }

    @Override
    protected void append(LongGenerator generator, boolean[] a) {
        for (boolean element : a) {
            generator.hash = MULTIPLIER * generator.hash + (element ? 1231 : 1237);
        }
    }

    @Override
    protected void append(LongGenerator generator, byte[] a) {
        for (byte element : a) {
            generator.hash = MULTIPLIER * generator.hash + element;
        }
    }

    @Override
    protected void append(LongGenerator generator, char[] a) {
        for (char element : a) {
            generator.hash = MULTIPLIER * generator.hash + element;
        }
    }

    @Override
    protected void append(LongGenerator generator, double[] a) {
        for (double element : a) {
            generator.hash = MULTIPLIER * generator.hash + Double.doubleToLongBits(element);
        }
    }

    @Override
    protected void append(LongGenerator generator, float[] a) {
        for (float element : a) {
            generator.hash = MULTIPLIER * generator.hash + Float.floatToIntBits(element);
        }
    }

    @Override
    protected void append(LongGenerator generator, int[] a) {
        for (int element : a) {
            generator.hash = MULTIPLIER * generator.hash + element;
        }
    }

    @Override
    protected void append(LongGenerator generator, long[] a) {
        for (long element : a) {
            generator.hash = MULTIPLIER * generator.hash + element;
        }
    }

    @Override
    protected void append(LongGenerator generator, short[] a) {
        for (short element : a) {
            generator.hash = MULTIPLIER * generator.hash + element;
        }
    }

    @Override
    protected void appendGraphCycle(LongGenerator generator, Object o) {
        generator.hash = MULTIPLIER * generator.hash;
    }

    @Override
    protected void appendNull(LongGenerator generator) {
        generator.hash = MULTIPLIER * generator.hash;
    }

    @Override
    protected void appendHash(LongGenerator generator, Object e) {
        if (e instanceof Double) {
            generator.hash = MULTIPLIER * generator.hash + Double.doubleToLongBits(((Double) e).doubleValue());
        } else if (e instanceof Long) {
            generator.hash = MULTIPLIER * generator.hash + ((Long) e).longValue();
        } else {
            generator.hash = MULTIPLIER * generator.hash + e.hashCode();
        }
    }
}
