package com.baidu.uuap.cache.annotation.interfaces;

import com.baidu.uuap.cache.annotation.When;


/**
 * 触发器删除接口：为删除缓存做些条件
 *
 * @author <a href="mailto:3562720@qq.com">xiaozhi</a>
 * @version $Revision$
 * @Date 上午11:08:26
 * @since 2013-5-10
 */
public interface ITriggersRemoveAttribute extends IMethodAttribute {

    /**
     * 获取条件
     *
     * @return
     */
    When getWhen();
}
