package com.tjpu.sp.common.mongo;


/**
 * @author: chengzq
 * @date: 2018/8/27 0027 上午 11:53
 * @Description: 用于排序使用
 * @updateUser:
 * @updateDate:
 * @updateDescription:
 * @param: 
 * @throws:
 */
public enum Direction implements EnumCode<Integer> {
    ASC(1, "asc"),               // 升序
    DESC(2, "desc");             // 降序

    private Integer key;
    private String desc;

    Direction(int key, String desc) {
        this.key=key;
        this.desc=desc;
    }

    @Override
    public Integer getKey() {
        return key;
    }

    @Override
    public String getDesc() {
        return desc;
    }
}