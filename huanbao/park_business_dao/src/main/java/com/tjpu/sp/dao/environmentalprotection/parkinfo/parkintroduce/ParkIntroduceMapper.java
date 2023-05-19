package com.tjpu.sp.dao.environmentalprotection.parkinfo.parkintroduce;

import com.tjpu.sp.model.base.parkintroduce.ParkIntroduceVO;

import java.util.Map;

public interface ParkIntroduceMapper {
    int deleteByPrimaryKey(String pkParkintroduceid);

    int insert(ParkIntroduceVO record);

    int insertSelective(ParkIntroduceVO record);

    ParkIntroduceVO selectByPrimaryKey(String pkParkintroduceid);

    int updateByPrimaryKeySelective(ParkIntroduceVO record);

    int updateByPrimaryKey(ParkIntroduceVO record);

    /**
     * @author: zhangzc
     * @date: 2019/5/9 14:17
     * @Description: 获取最新一条园区介绍信息
     * @param:
     * @return:
     */
    Map<String,Object> getLastParkIntroduceInfo();
}