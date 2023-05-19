package com.tjpu.sp.common.mongo;

/**
 * @author: chengzq
 * @date: 2018/8/27 0027 上午 11:54
 * @Description:   枚举的抽象
 * @updateUser:
 * @updateDate:
 * @updateDescription:
 * @param:
 * @throws:
 */
public interface EnumCode<K> {

    K getKey();

    String getDesc();

}