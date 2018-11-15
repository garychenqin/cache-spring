package com.baidu.uuap.cache.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 触发删除器
 *
 * @author <a href="mailto:3562720@qq.com">xiaozhi</a>
 * @version $Revision$
 * @Date 上午11:07:00
 * @since 2013-5-10
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface TriggersRemove {

    When when() default When.BEFORE_METHOD_INVOCATION;

    String className() default "";

    String methodName() default "";

    // 缓存的前缀（如果配置了全局的前缀，则以这个为准）
    String cachePrefix() default "";
}
