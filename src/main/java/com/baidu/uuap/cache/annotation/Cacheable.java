package com.baidu.uuap.cache.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * 缓存注解<br>
 * <p>
 * 其中@Inherited注解表示：你可以进行继承。
 *
 * @author <a href="mailto:3562720@qq.com">xiaozhi</a>
 * @version $Revision$
 * @Date 上午11:57:29
 * @since 2013-5-8
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface Cacheable {

    // 是否马上缓存
    boolean cacheOpen() default true;

    // 默认缓存时间，开始本地jvm缓存时，这个参数不对本地缓存起作用
    int expireSeconds() default -1;

    // 被缓存的类名  如果你需要针对key进行删除，那么就必须写上这个类名
    String className() default "";

    // 被缓存的方法名 如果你需要针对key进行删除，那么就必须写上方法名
    String methodName() default "";

    // 缓存的前缀（如果配置了全局的前缀，则以这个为准）
    String cachePrefix() default "";

    // 当jvm缓存开启的时候设置某个方法忽略jvm缓存
    boolean excludeJvmCache() default false;

    // 当开启优先jvm本地缓存的时候value是不是进行序列化，默认为true
    boolean jvmCacheSerialize() default true;
}
