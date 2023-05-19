package com.tjpu.sp.dao.environmentalprotection.parkinfo.parkbigevent;

import com.tjpu.sp.model.base.parkbigevent.ParkBigEventVO;

import java.util.List;
import java.util.Map;

public interface ParkBigEventMapper {
    int deleteByPrimaryKey(String pkBigeventid);

    int insert(ParkBigEventVO record);

    int insertSelective(ParkBigEventVO record);

    ParkBigEventVO selectByPrimaryKey(String pkBigeventid);

    int updateByPrimaryKeySelective(ParkBigEventVO record);

    int updateByPrimaryKey(ParkBigEventVO record);

    /**
     * @author: zhangzc
     * @date: 2019/5/9 15:43
     * @Description: 获取大事件信息按时间倒序
     * @param:
     * @return:
     */
    List<Map<String, Object>> getDescBigEventsInTime();
}