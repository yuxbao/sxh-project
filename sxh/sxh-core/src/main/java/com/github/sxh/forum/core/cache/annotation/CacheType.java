package com.github.sxh.forum.core.cache.annotation;

import com.github.sxh.forum.api.model.enums.cache.CacheTypeEnum;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author xuyifei
 * 标记缓存类型
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface CacheType {

    CacheTypeEnum value();

}
