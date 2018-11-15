package com.baidu.uuap.cache.annotation.impl;

import java.io.Serializable;

import com.baidu.uuap.cache.annotation.AdviceType;
import com.baidu.uuap.cache.annotation.When;
import com.baidu.uuap.cache.annotation.interfaces.ITriggersRemoveAttribute;
import com.baidu.uuap.cache.annotation.key.interfaces.ICacheKeyGenerator;

/**
 * 触发器删除，主要是用来存储触发器删除动作使用到的属性
 *
 * @author <a href="mailto:3562720@qq.com">xiaozhi</a>
 * @version $Revision$
 * @Date 上午11:21:30
 * @since 2013-5-10
 */
public class TriggersRemoveAttributeImpl implements ITriggersRemoveAttribute {

    private ICacheKeyGenerator<? extends Serializable> cacheKeyGenerator;
    private When when;
    private String className;
    private String methodName;
    private String cachePrefix;

    public TriggersRemoveAttributeImpl(When when, ICacheKeyGenerator<? extends Serializable> cacheKeyGenerator,
                                       String className, String methodName, String cachePrefix) {
        this.cacheKeyGenerator = cacheKeyGenerator;
        this.when = when;
        this.className = className;
        this.methodName = methodName;
        this.cachePrefix = cachePrefix;
    }

    @Override
    public AdviceType getAdviceType() {
        return AdviceType.REMOVE;
    }

    @Override
    public ICacheKeyGenerator<? extends Serializable> getCacheKeyGenerator() {
        return this.cacheKeyGenerator;
    }

    @Override
    public When getWhen() {
        return this.when;
    }

    @Override
    public String getClassName() {
        return className;
    }

    @Override
    public String getMethodName() {
        return methodName;
    }

    @Override
    public String getCachePrefix() {
        return this.cachePrefix;
    }
}
