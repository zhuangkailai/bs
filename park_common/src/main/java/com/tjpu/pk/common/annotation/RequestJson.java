package com.tjpu.pk.common.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 
 * @author: lip
 * @date: 2018年8月2日 上午9:27:12
 * @Description:自定义注解，解析请求体中json字符串，根据key值获取参数值
 * @updateUser:
 * @updateDate:
 * @updateDescription:
 * @version V1.0
 *
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequestJson {
	String value();

	boolean required() default true;

	String defaultValue() default "";

}
