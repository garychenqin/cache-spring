package com.baidu.uuap.cache.annotation.key.interfaces;

/**
 * 反射帮助类
 *
 * @author <a href="mailto:3562720@qq.com">xiaozhi</a>
 * @version $Revision$
 * @Date 下午10:11:19
 * @since 2013-5-9
 */
public interface IReflectionHelper {

    /**
     * 检查这个对象是否实现了hashcode
     *
     * @param element will never be null
     */
    public boolean implementsHashCode(Object element);

    /**
     * 检查这个对象是否实现了equals
     *
     * @param element will never be null
     */
    public boolean implementsEquals(Object element);

    /**
     * 检查这个对象是否实现了tostring
     *
     * @param element will never be null
     */
    public boolean implementsToString(Object element);
}
