package com.tjpu.sp.common.anticontrol;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface NotResubmit {

    /**
     * 延时时间 在延时多久后可以再次提交,默认8秒
     *
     * @return 秒
     */
    int delaySeconds() default 8;
}
