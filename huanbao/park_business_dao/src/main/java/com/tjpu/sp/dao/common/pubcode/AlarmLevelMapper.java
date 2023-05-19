package com.tjpu.sp.dao.common.pubcode;


import com.tjpu.sp.model.common.pubcode.AlarmLevelVO;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
@Repository
public interface AlarmLevelMapper {
    int deleteByPrimaryKey(Integer pkId);

    int insert(AlarmLevelVO record);

    int insertSelective(AlarmLevelVO record);

    AlarmLevelVO selectByPrimaryKey(Integer pkId);

    int updateByPrimaryKeySelective(AlarmLevelVO record);

    int updateByPrimaryKey(AlarmLevelVO record);

    /**
     * @author: chengzq
     * @date: 2019/5/21 0021 下午 1:20
     * @Description: 获取所有报警级别码表信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     * @throws:
     */
    List<Map<String,Object>> getAlarmLevelPubCodeInfo();
}