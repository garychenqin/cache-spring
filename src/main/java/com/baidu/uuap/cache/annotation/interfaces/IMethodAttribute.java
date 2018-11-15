/*
 * 文件名： IMethodAttribute.java
 * 
 * 创建日期： 2013-5-10
 *
 * Copyright(C) 2013, by xiaozhi.
 *
 * 原始作者: <a href="mailto:3562720@qq.com">xiaozhi</a>
 *
 */
package com.baidu.uuap.cache.annotation.interfaces;

import java.io.Serializable;

import com.baidu.uuap.cache.annotation.AdviceType;
import com.baidu.uuap.cache.annotation.key.interfaces.ICacheKeyGenerator;


/**
 * 方法接口：主要用为方法做缓存要用到的常用属性
 *
 * @author <a href="mailto:3562720@qq.com">xiaozhi</a>
 * @version $Revision$
 * @Date 上午10:24:48
 * @since 2013-5-10
 */
public interface IMethodAttribute {

    /**
     * 获取通知类型
     *
     * @return
     */
    AdviceType getAdviceType();

    /**
     * 获取缓存key生成器
     *
     * @return
     */
    ICacheKeyGenerator<? extends Serializable> getCacheKeyGenerator();

    /**
     * 获取类名
     *
     * @return
     */
    String getClassName();

    /**
     * 获取方法名
     *
     * @return
     */
    String getMethodName();

    /**
     * 获取缓存前缀
     *
     * @return
     */
    String getCachePrefix();
}
