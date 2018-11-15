package com.baidu.uuap.cache.annotation.driven;

import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

/**
 * 自定义spring的xml元素类
 * Created by chenshouqin on 2017/9/25
 */
public class CacheNamespaceHandler extends NamespaceHandlerSupport {

    @Override
    public void init() {
        registerBeanDefinitionParser("annotation-driven", new CacheBeanDefinitionParser());
    }
}
