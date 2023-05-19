package com.tjpu.pk.common.annotation;

import java.lang.annotation.*;

/**
 * @author: chengzq
 * @date: 2018/7/13 16:52
 * @Description: 实体注解，通过实体字段获取该注解属性值
 * @updateUser:
 * @updateDate:
 * @updateDescription:
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface FieldCustomConfig {
    String field();
    String operator() default "=";
}
