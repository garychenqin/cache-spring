package com.baidu.uuap.cache.annotation;

/**
 * 执行条件
 *
 * @author <a href="mailto:3562720@qq.com">xiaozhi</a>
 * @version $Revision$
 * @Date 上午11:05:15
 * @since 2013-5-10
 */
public enum When {
    // 在方法之前
    BEFORE_METHOD_INVOCATION,
    // 在方法之后
    AFTER_METHOD_INVOCATION
}
